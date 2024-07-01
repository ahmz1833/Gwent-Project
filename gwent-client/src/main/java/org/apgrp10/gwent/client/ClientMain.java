package org.apgrp10.gwent.client;

import org.apgrp10.gwent.model.net.AsyncReader;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.model.net.Packet;
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
import java.util.List;
import java.util.Locale;

public class ClientMain {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		if (!Server.connect())
			System.exit(0);
		Server.getInstance().addOnClose(() -> System.exit(0));
		Server.getInstance().setOnReceive(bytes -> {
			System.out.println("Received: " + new String(bytes));
			System.out.println();
		});

		new Thread(() -> {
			while (true) {
				Server.getInstance().run();
				try { Thread.sleep(10); } catch (Exception e) { }
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

		private Server(Socket socket) {
			super(socket);
		}

		public static Server getInstance() {
			if (instance == null) connect();
			return instance;
		}

		public static boolean connect() {
			try {
				if (instance != null && !instance.isClosed()) return true;
				Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				instance = new Server(socket);
				instance.addOnClose(() -> {
					ANSI.log("Connection to server lost.", ANSI.LRED, false);
				});

				ANSI.log("Connected to server.", ANSI.LGREEN, false);
				return true;
			} catch (IOException e) {
				ANSI.logError(System.err, "Failed to connect to server", e);
				return false;
			}
		}
	}
}
