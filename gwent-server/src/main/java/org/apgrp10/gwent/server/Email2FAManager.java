package org.apgrp10.gwent.server;

import com.sun.net.httpserver.HttpServer;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Email2FAManager {
	private static final int HTTP_SERVER_PORT = ServerMain.SERVER_PORT + 1;
	private static final Map<String, User.RegisterInfo> registerQueue = new HashMap<>();
	private static final List<LoginInfo> loginQueue = new ArrayList<>();
	private static final int LOGIN_CODE_LENGTH = 6, LOGIN_CODE_EXPIRY = 300_000;
	public static String EMAIL_SERVER_ADDR = "localhost";
	public static int EMAIL_SERVER_PORT = 41567;
	private static Consumer<User.RegisterInfo> registerCallback;

	static {
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(HTTP_SERVER_PORT), 0);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to create HTTP server", e);
			System.exit(1);
		}
		server.createContext("/", httpExchange -> {
			String response = EMAIL_HTML("Invalid URL", "<h1>Invalid URL</h1>");
			int responseCode = Response.NOT_FOUND;
			String path = httpExchange.getRequestURI().getPath();
			if (path.startsWith("/verify/")) {
				String uuid = path.substring(8);
				User.RegisterInfo userInfo = registerQueue.get(uuid);
				registerQueue.remove(uuid);
				if (userInfo == null) {
					response = WEBPAGE_HTML("Invalid Verification Link", """
							<h1>Invalid Verification Link</h1>
							<p>The verification link you have used is invalid or has expired.</p>
							""");
					responseCode = Response.BAD_REQUEST;  // Bad request
				} else {
					registerCallback.accept(userInfo);  // Register the user
					response = WEBPAGE_HTML("Email Verified", """
							<h1>Verification Completed %s!</h1>
							<p>Email Verification of account with user name '%s' is now complete. You can now login to your account.</p>
							""".formatted(userInfo.nickname(), userInfo.username()));
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

	public static void setRegisterCallback(Consumer<User.RegisterInfo> callback) {
		registerCallback = callback;
	}

	public static void sendLoginCodeAndAddToQueue(String email, Client client, long userId) throws Exception {
		long base = (long) Math.pow(10, LOGIN_CODE_LENGTH - 1);
		String code = String.valueOf(Random.nextLong(base, base * 10));
		String emailContent = EMAIL_HTML("Login Code", """
				<p>Your verification code is:</p>
				<h1><u><b>%s</b></u></h1>
				""".formatted(code));
		sendEmail(email, "Verification code", emailContent);
		loginQueue.add(new LoginInfo(code, client, userId, System.currentTimeMillis()));
	}

	public static boolean verifyLoginCode(Client client, String code, long userId) {
		for (LoginInfo loginInfo : new ArrayList<>(loginQueue))
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
		String verificationLink = "http://" + ServerMain.SERVER_ADDR + ":" + HTTP_SERVER_PORT + "/verify/" + uuid;
		String emailContent = EMAIL_HTML("Email Verification", """
				<p>Hello %s (%s),</p>
				<p>Please click on the following link to verify your email:</p>
				<b><a href="%s">%s</a></b>
				""".formatted(userInfo.nickname(), userInfo.username(), verificationLink, verificationLink));
		sendEmail(userInfo.email(), "Verify your email", emailContent);
		registerQueue.put(uuid, userInfo);
	}

	private static void sendEmail(String recipient, String subject, String content) throws Exception {
		HttpClient client = HttpClient.newHttpClient();

		String url = "http://" + EMAIL_SERVER_ADDR + ":" + EMAIL_SERVER_PORT + "/send_email";
		String urlParameters = buildUrlParameters(Map.of(
				"recipient", recipient,
				"subject", subject,
				"content", content
		));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(urlParameters))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new Exception("Failed to send email.");
		}
	}

	private static String buildUrlParameters(Map<String, String> params) {
		return params.entrySet()
				.stream()
				.map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) +
				              "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));
	}

	private static String EMAIL_HTML(String title, String content) {
		return """
					<!DOCTYPE html>
					<html lang="en">
					<head>
					<meta charset="UTF-8">
					<title>%s</title>
					<style>
						body {font-family: 'Arial', sans-serif;background-color: #FFEDD5;color: #5D3A00;padding: 20px;}
						.content {background-color: #FFDAB9;border: 2px solid #FFA07A;border-radius: 10px;padding: 20px;text-align: center;}
						a {color: #D2691E;text-decoration: none;font-weight: bold;}
						a:hover {text-decoration: underline;}
						h1 {color: #D2691E;font-size: 24px;margin-top: 10px;}
						p {font-size: 18px;}
					</style>
					</head>
					<body>
						<div class="content">
							%s
						</div>
					</body>
					</html>
				""".formatted(title, content);
	}

	private static String WEBPAGE_HTML(String title, String body) {
		return """
				<!DOCTYPE html>
				<html lang="en">
					<head>
					    <meta charset="UTF-8">
					    <meta name="viewport" content="width=device-width, initial-scale=1.0">
					    <title>%s</title>
					    <style>
				            body {
								font-family: 'Arial', sans-serif;
								background-color: #FFEDD5;
								color: #5D3A00;
								display: flex;
								justify-content: center;
								align-items: center;
								height: 100vh;
								margin: 0;
								padding: 10px;
								box-sizing: border-box;
							}
					        .card {
					            background-color: #FFDAB9;
					            border: 2px solid #FFA07A;
					            border-radius: 10px;
					            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
					            padding: 20px;
					            text-align: center;
					            width: 100%%;
					            max-width: 400px;
					            margin: 0 auto;
					            box-sizing: border-box;
					        }
					        h1 { color: #D2691E; font-size: 24px; }
					        p { font-size: 18px; margin-top: 10px; }
					        @media (max-width: 600px) {
					            h1 { font-size: 20px; }
					            p { font-size: 16px; }
					            .card { padding: 15px; }
					        }
					    </style>
					</head>
					<body>
					    <div class="card">
					        %s
					    </div>
					</body>
				</html>
				""".formatted(title, body);
	}

	private record LoginInfo(String sentCode, Client client, long userId, long sendTime) {}
}
