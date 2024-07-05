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

public class Email2FAUtils {
	private static final Map<String, User.RegisterInfo> registerQueue = new HashMap<>();
	private static final List<LoginInfo> loginQueue = new ArrayList<>();
	private static final String HTTP_SERVER_ADDR = "37.152.178.57", EMAIL_SERVER_ADDR = "localhost";
	private static final int HTTP_SERVER_PORT = 2222, EMAIL_SERVER_PORT = 41567;
	private static final int LOGIN_CODE_LENGTH = 6, LOGIN_CODE_EXPIRY = 300_000;
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
					registerCallback.accept(userInfo);  // Register the user
					// a pretty html with beautiful colors
					response = """
								 <!DOCTYPE html>
							     <html lang="en">
							     <head>
							         <meta charset="UTF-8">
							         <meta name="viewport" content="width=device-width, initial-scale=1.0">
							         <title>Email Verified</title>
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
							                 width: 100%;
							                 max-width: 400px;
							                 margin: 0 auto;
							                 box-sizing: border-box;
							             }
							             h1 {
							                 color: #D2691E;
							                 font-size: 24px;
							             }
							             p {
							                 font-size: 18px;
							                 margin-top: 10px;
							             }
							             @media (max-width: 600px) {
							                 h1 {
							                     font-size: 20px;
							                 }
							                 p {
							                     font-size: 16px;
							                 }
							                 .card {
							                     padding: 15px;
							                 }
							             }
							         </style>
							     </head>
							     <body>
							         <div class="card">
							             <h1>Registration Completed!</h1>
							             <p>Your registration is now complete. You can now login to your account.</p>
							         </div>
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

	public static void setRegisterCallback(Consumer<User.RegisterInfo> callback) {
		registerCallback = callback;
	}

	public static void sendLoginCodeAndAddToQueue(String email, Client client, long userId) throws Exception {
		long base = (long) Math.pow(10, LOGIN_CODE_LENGTH - 1);
		String code = String.valueOf(Random.nextLong(base, base * 10));
		String emailContent = """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <title>Login Code</title>
				    <style>
				        body {
				            font-family: 'Arial', sans-serif;
				            background-color: #FFEDD5;
				            color: #5D3A00;
				            padding: 20px;
				        }
				        .content {
				            background-color: #FFDAB9;
				            border: 2px solid #FFA07A;
				            border-radius: 10px;
				            padding: 20px;
				            text-align: center;
				        }
				        h1 {
				            color: #D2691E;
				            font-size: 24px;
				            margin-top: 10px;
				        }
				        p {
				            font-size: 18px;
				        }
				    </style>
				</head>
				<body>
				    <div class="content">
				        <p>Your login code is:</p>
				        <h1><u><b>%s</b></u></h1>
				    </div>
				</body>
				</html>
				""".formatted(code);
		sendEmail(email, "Login code", emailContent);
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
		String verificationLink = "http://" + HTTP_SERVER_ADDR + ":" + HTTP_SERVER_PORT + "/verify/" + uuid;
		String emailContent = """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				    <meta charset="UTF-8">
				    <title>Email Verification</title>
				    <style>
				        body {
				            font-family: 'Arial', sans-serif;
				            background-color: #FFEDD5;
				            color: #5D3A00;
				            padding: 20px;
				        }
				        .content {
				            background-color: #FFDAB9;
				            border: 2px solid #FFA07A;
				            border-radius: 10px;
				            padding: 20px;
				            text-align: center;
				        }
				        a {
				            color: #D2691E;
				            text-decoration: none;
				            font-weight: bold;
				        }
				        a:hover {
				            text-decoration: underline;
				        }
				        p {
				            font-size: 18px;
				        }
				    </style>
				</head>
				<body>
				    <div class="content">
				        <p>Please click on the following link to verify your email:</p>
				        <b><a href="%s">%s</a></b>
				    </div>
				</body>
				</html>
				""".formatted(verificationLink, verificationLink);
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

	private record LoginInfo(String sentCode, Client client, long userId, long sendTime) {}
}
