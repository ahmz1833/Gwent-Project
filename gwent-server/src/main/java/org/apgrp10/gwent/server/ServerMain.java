package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ServerMain {
	public static final String SERVER_FOLDER = System.getProperty("user.home") + "/gwent-data/";
	protected static final String SECRET_KEY = "AP_server@apgrp10/2024";
	public static int PORT = 12345;

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		try{
			Email2FAManager.HTTP_SERVER_ADDR = args[0].split(":")[0];
			PORT = Integer.parseInt(args[0].split(":")[1]);
			Email2FAManager.HTTP_SERVER_PORT = PORT + 1;
			Email2FAManager.EMAIL_SERVER_ADDR = args[1].split(":")[0];
			Email2FAManager.EMAIL_SERVER_PORT = Integer.parseInt(args[1].split(":")[1]);
		} catch (Exception e)
		{
			Email2FAManager.HTTP_SERVER_ADDR = "localhost";
			Email2FAManager.HTTP_SERVER_PORT = PORT + 1;
			Email2FAManager.EMAIL_SERVER_ADDR = "localhost";
		}

		// Create the server folder if it doesn't exist
		Path path = Paths.get(SERVER_FOLDER);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
				ANSI.log("Server directory created at: " + SERVER_FOLDER, ANSI.LYELLOW.bd(), false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Start the server
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			ANSI.log("Server started on port " + PORT, ANSI.LYELLOW.bd(), false);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to start server on port " + PORT, e);
			System.exit(1);
		}

		// Initialize the thread pool
		TaskManager.init(10);

		Email2FAManager.setRegisterCallback(userInfo -> {
			ANSI.log("Email verified: " + userInfo, ANSI.LGREEN, false);
			if (UserManager.isUsernameTaken(userInfo.username())) try {
				// Update Email Address
				UserManager.updateEmail(userInfo.id(), userInfo.email());
			} catch (Exception e) {
				ANSI.logError(System.err, "Failed to update email address in database", e);
			}
			else try {
				// Add User to Database
				UserManager.addUser(userInfo);
			} catch (Exception e) {
				ANSI.logError(System.err, "Failed to add user to database", e);
			}
		});

		ANSI.log("Listening at port: " + PORT, ANSI.CYAN, false);
		while (true) {
			try {
				Socket socket = serverSocket.accept();

				Client client = new Client(socket);
				//List all static methods in Requests class

				Method[] methods = Requests.class.getMethods();
				for (Method method : methods) {
					// Check if the method is a static method
					if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC)) continue;

					// Check if the method has the correct signature
					if (method.getParameterCount() != 2) continue;
					if (method.getParameterTypes()[0] != Client.class) continue;
					if (method.getParameterTypes()[1] != Request.class) continue;
					if (method.getReturnType() != Response.class) continue;

					// Check if the method has the Authorizations annotation
					Requests.Authorizations auth = method.getAnnotation(Requests.Authorizations.class);

					// Add the method as a listener
					client.setListener(method.getName(), req -> {
						try {
							if (auth != null) {
								switch (auth.value()) {
									case ALL:
										break;
									case LOGGED_IN:
										if (client.loggedInUser() == null)
											return req.response(Response.UNAUTHORIZED);
										break;
									case NOT_LOGGED_IN:
										if (client.loggedInUser() != null) {
											ANSI.log("In Handling Request Method : " + method.getName(), ANSI.LRED, false);
											ANSI.log("User already logged in : " + client.loggedInUser().username(), ANSI.LRED, false);
											return req.response(Response.BAD_REQUEST);
										}
										break;
								}
							}
							return (Response) method.invoke(null, client, req);
						} catch (InvocationTargetException e) {
							ANSI.logError(System.err, "Failed to invoke method " + method.getName(), e.getCause());
							return ANSI.createErrorResponse(req, "Failed to invoke method " + method.getName(), e.getCause());
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					});
				}

				TaskManager.submit(client);
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to accept client connection", e);
			}
		}
	}
}
