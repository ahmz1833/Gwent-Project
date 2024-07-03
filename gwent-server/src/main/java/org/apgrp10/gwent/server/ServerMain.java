package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
//			serverSocket = SecurityUtils.getSSLServerSocketFactory().createServerSocket(PORT);
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
				// for test
				client.setListener("hello", (Request req) -> {
					JsonObject body = new JsonObject();
					body.add("msg", new JsonPrimitive(
						"""
						According to all known laws of aviation, there is no way a bee should be able to fly.
						Its wings are too small to get its fat little body off the ground.
						The bee, of course, flies anyway because bees don't care what humans think is impossible.
						Yellow, black. Yellow, black. Yellow, black. Yellow, black.
						Ooh, black and yellow!
						Let's shake it up a little.
						Barry! Breakfast is ready!
						Coming!
						"""
					));
					client.getPacketHandler().sendResponse(new Response(
						req.getId(),
						200,
						body
					));
				});
				TaskManager.submit(client);
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to accept client connection", e);
			}
		}
	}
}
