package org.apgrp10.gwent.server;

import com.sun.net.httpserver.HttpServer;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Callback;
import org.apgrp10.gwent.utils.Random;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Email2FAUtils {
	private static final Map<String, User.RegisterInfo> registerQueue = new HashMap<>();
	private static final List<LoginInfo> loginQueue = new ArrayList<>();
	private static final String HTTP_SERVER_ADDR = "37.152.178.57", EMAIL_SERVER_ADDR = "localhost";
	private static final int HTTP_SERVER_PORT = 2222, EMAIL_SERVER_PORT = 41567;
	private static final int LOGIN_CODE_LENGTH = 6, LOGIN_CODE_EXPIRY = 300_000;
	private static Callback<User.RegisterInfo> registerCallback;

	public static void setRegisterCallback(Callback<User.RegisterInfo> callback) {
		registerCallback = callback;
	}

	static {
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(HTTP_SERVER_PORT), 0);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to create HTTP server", e);
			System.exit(1);
		}
		server.createContext("/", httpExchange -> {
			int responseCode = Response.NOT_FOUND;
			String response = """
					<html>
						<head> <title>Invalid URL</title> </head>
						<body>
							<h1>Invalid URL</h1>
							<p>Invalid URL. Please use the correct URL.</p>
						</body>
					</html>
					""";
			String path = httpExchange.getRequestURI().toString();
			if (path.startsWith("/verify/")) {
				String uuid = path.substring(8);
				User.RegisterInfo userInfo = registerQueue.get(uuid);
				registerQueue.remove(uuid);
				if (userInfo == null) {
					response = """
							<html>
							<head> <title>Invalid verification link</title> </head>
								<body>
									<h1>Invalid verification link</h1>
								</body>
							</html>
							""";
					responseCode = Response.BAD_REQUEST;  // Bad request
				} else {
					registerCallback.call(userInfo);  // Register the user
					response = """
							<html>
							<head> <title>Email verified</title> </head>
								<body>
									<h1>Email verified successfully!</h1>
								</body>
							</html>
							""";
					responseCode = Response.OK;  // OK
				}
			}
			httpExchange.sendResponseHeaders(responseCode, response.getBytes().length);
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});
		server.setExecutor(null);
		server.start();
	}

	public static void sendLoginCodeAndAddToQueue(String email, Client client, long userId) throws Exception {
		long base = (long) Math.pow(10, LOGIN_CODE_LENGTH - 1);
		String code = String.valueOf(Random.nextLong(base, base * 10));
		String emailContent = """
					<html>
						<body>
							Your login code is:
							<h1> <u><b>%s</b></u> </h1>
						</body>
					</html>
				""".formatted(code);
		sendEmail(email, "Login code", emailContent);
		loginQueue.add(new LoginInfo(code, client, userId, System.currentTimeMillis()));
	}

	public static boolean verifyLoginCode(Client client, String code, long userId) {
		for (LoginInfo loginInfo : loginQueue)
			if (loginInfo.userId == userId && loginInfo.sentCode.equals(code) && loginInfo.client == client) {
				if (System.currentTimeMillis() - loginInfo.sendTime < LOGIN_CODE_EXPIRY) {
					loginQueue.remove(loginInfo);
					return true;
				}
				break;
			}
		return false;
	}

	public static void sendRegMailAndAddToQueue(User.RegisterInfo userInfo) throws Exception {
		String uuid = java.util.UUID.randomUUID().toString();
		String verificationLink = "http://" + HTTP_SERVER_ADDR + ":" + HTTP_SERVER_PORT + "/verify/" + uuid;
		String emailContent = """
				<html>
					<body>
						Please click on the following link to verify your email:
						<b> <a href="%s">%s</a> </b>
					</body>
				</html>
				""".formatted(verificationLink, verificationLink);
		sendEmail(userInfo.email(), "Verify your email", emailContent);
		registerQueue.put(uuid, userInfo);
	}

	private static void sendEmail(String recipient, String subject, String content) throws Exception {
		URL url = new URL("http://" + EMAIL_SERVER_ADDR + ":" + EMAIL_SERVER_PORT + "/send_email");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		String urlParameters = "recipient=" + recipient + "&subject=" + subject + "&content=" + content;
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		try (OutputStream os = connection.getOutputStream()) {
			os.write(postData);
		}
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			throw new Exception("Failed to send email.");
	}

	private record LoginInfo(String sentCode, Client client, long userId, long sendTime) {}
}
