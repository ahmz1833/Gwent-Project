package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.server.db.UserDatabase;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;

import java.io.IOException;
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
	public static final int PORT = 12345;
	protected static final String SECRET_KEY = "AP_server@apgrp10/2024";
	private static final Object lock = new Object();
	private static Client fastPlayed;

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);

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

		Email2FAUtils.setRegisterCallback(userInfo -> {
			ANSI.log("Registeration email verified: " + userInfo, ANSI.LGREEN, false);
			// Add user to database
			try {
				UserDatabase.getInstance().addUser(userInfo);
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

					ANSI.log("Method: " + method.getName(), ANSI.LGREEN, false);

					// Check if the method has the Authorizations annotation
					Requests.Authorizations auth = method.getAnnotation(Requests.Authorizations.class);

					ANSI.log("Auth: " + auth, ANSI.LGREEN, false);

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
										if (client.loggedInUser() != null)
											return req.response(Response.BAD_REQUEST);
										break;
								}
							}
							return (Response) method.invoke(null, client, req);
						} catch (Exception e) {
							ANSI.logError(System.err, "Failed to invoke method", e);
							return req.response(Response.INTERNAL_SERVER_ERROR);
						}
					});
				}

//				client.setListener("fastPlay", req -> {
//					synchronized (lock) {
//						if (fastPlayed == null) {
//							fastPlayed = client;
//							return req.response(Response.OK, MGson.makeJsonObject("player", 0));
//						} else {
//							TaskManager.submit(new GameTask(fastPlayed, client));
//							fastPlayed = null;
//							client.setListener("fastPlay", null);
//							return req.response(Response.OK, MGson.makeJsonObject("player", 1));
//						}
//					}
//				});

				client.getNetNode().addOnClose(() -> {
					synchronized (lock) {
						if (fastPlayed == client)
							fastPlayed = null;
					}
				});


				TaskManager.submit(client);
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to accept client connection", e);
			}
		}
	}
}
