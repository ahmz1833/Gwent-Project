package org.apgrp10.gwent.client;

import org.apgrp10.gwent.model.net.InputNotAvailableException;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.SecurityUtils;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

public class ClientMain {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		Server.connect();
		
		new Thread(() -> {
			while (true) {
				try {
//					while (Server.getInstance().isAlive() && Server.getInstance().in().available() > 0) {
					System.out.println("Received: " + new String(Server.getInstance().receive()));
					System.out.println();
//					}
				} catch (InputNotAvailableException e) {
					// do nothing
				} catch (Exception e) {
					ANSI.logError(System.err, "Failed to receive message from server", e);
				}
			}
		}).start();
		
		
		// for test, sending each 1000 ms a message to the server
//		new Thread(() -> {
//			while (true) {
//				try {
//					Thread.sleep(1000);
//					Server.getInstance().send("Hello from client");
//				} catch (Exception e) {
//					ANSI.logError(System.err, "Failed to send message to server", e);
//				}
//			}
//		}).start();
		
		Gwent.main(args);
	}
	
	
	static class Server extends NetNode {
		public static final String SERVER_IP = "37.152.181.45";
		public static final int SERVER_PORT = 12345;
		private static Server instance;
		
		private Server(Socket socket) throws IOException {
			super(socket);
		}
		
		public static Server getInstance() {
			if (instance == null) connect();
			return instance;
		}
		
		public static void connect() {
			try {
				if (instance != null && instance.isAlive()) return;
				Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				instance = new Server(socket);
				instance.setDefaultListener(new ConnectionListener() {
					@Override
					public void onConnectionEstablished() {
						ANSI.log("Connected to server.", ANSI.LGREEN, false);
					}
					
					@Override
					public void onConnectionLost() {
						ANSI.log("Connection to server lost.", ANSI.LRED, false);
					}
				});
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to connect to server", e);
			}
		}
	}
}
