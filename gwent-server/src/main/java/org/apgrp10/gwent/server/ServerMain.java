package org.apgrp10.gwent.server;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.RSA;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Locale;

public class ServerMain {
	
	public static final String SERVER_FOLDER = System.getProperty("user.home") + "/gwent-data/";
	public static final int PORT = 12345;
	public static final KeyPair keyPair = RSA.generateKeyPair();
	
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
		
		while (true) {
			try {
				ANSI.log("Listening at port :" + PORT + " - Connected Clients: " + Client.connectedClients(), ANSI.CYAN, false);
				Client client = Client.acceptConnection(serverSocket);
				ANSI.log("Client Connected: " + client.getIdString(), ANSI.LCYAN.bd(), false);
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to accept client connection", e);
			}
		}
	}
}
