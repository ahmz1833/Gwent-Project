package org.apgrp10.gwent.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ServerMain {
	public static final String SERVER_FOLDER = System.getProperty("user.home") + "/gwent-data/";
	public static final int PORT = 12345;
	private static Client fastPlayed;
	private static Object lock = new Object();

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

		ANSI.log("Listening at port: " + PORT, ANSI.CYAN, false);
		while (true) {
			try {
				Socket socket = serverSocket.accept();

				Client client = new Client(socket);
				client.setListener("fastPlay", req -> {
					synchronized (lock) {
						JsonObject json = new JsonObject();
						if (fastPlayed == null) {
							json.add("player", new JsonPrimitive(0));
							client.sendResponse(new Response(req.getId(), 200, json));
							fastPlayed = client;
						} else {
							json.add("player", new JsonPrimitive(1));
							client.sendResponse(new Response(req.getId(), 200, json));
							TaskManager.submit(new GameTask(fastPlayed, client));
							fastPlayed = null;
						}
						client.setListener("fastPlay", null);
					}
				});
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
