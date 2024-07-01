package org.apgrp10.gwent.server;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;
import org.apgrp10.gwent.utils.SecurityUtils;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
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
		HandlingThread[] threads = new HandlingThread[10];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new HandlingThread(Client.connectedClients());
			threads[i].start();
		}
		
		while (true) {
			try {
				ANSI.log("Listening at port :" + PORT + " - Connected Clients: " + Client.connectedClients().size(), ANSI.CYAN, false);
				Socket socket = serverSocket.accept();
				assignRandomThread(new Client(socket), threads);
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to accept client connection", e);
			}
		}
	}
	
	private static void assignRandomThread(Client client, HandlingThread[] threads) {
		int index = Random.generateRandomNumber(0, threads.length - 1);
		threads[index].addClient(client);
	}
}
