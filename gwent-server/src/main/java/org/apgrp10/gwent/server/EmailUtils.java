package org.apgrp10.gwent.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apgrp10.gwent.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class EmailUtils {
	private static final HashMap<String, User.UserInfo> queue = new HashMap<>();
	private static final String HTTP_SERVER_ADDR = "37.152.178.57", EMAIL_SERVER_ADDR = "localhost";
	private static final int HTTP_SERVER_PORT = 2222, EMAIL_SERVER_PORT = 41567;
	private static HttpServer server = null;

	public static void sendMailAndAddToQueue(User.UserInfo userInfo) throws Exception {
		String uuid = java.util.UUID.randomUUID().toString();
		String emailContent = "Please click on the following link to verify your email: \n" +
		                      "http://" + HTTP_SERVER_ADDR + ":" + HTTP_SERVER_PORT + "/verify/" + uuid;
		sendEmail(userInfo.email(), "Verify your email", emailContent);
		queue.put("/verify/" + uuid, userInfo);
	}

	public static void main(String[] args) throws IOException {
		server = HttpServer.create(new InetSocketAddress(HTTP_SERVER_PORT), 0);
		server.createContext("/", new MyHandler());

		// شروع به کار سرور
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	public static void sendEmail(String recipient, String subject, String content) throws Exception {
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

	public interface RegisterCallback {
		void register();
	}

	// هندلر برای مدیریت درخواست‌ها
	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod();
			String path = exchange.getRequestURI().toString();

			// چاپ اطلاعات درخواست
			System.out.println("Received request: " + method + " " + path);

			// پاسخ ساده به درخواست
			String response = "This is the response to your request.";
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
