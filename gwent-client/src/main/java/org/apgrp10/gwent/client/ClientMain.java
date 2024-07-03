package org.apgrp10.gwent.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

public class ClientMain {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		if (!Server.connect())
			System.exit(0);
		Server.getInstance().getPacketHandler().getNetNode().addOnClose(() -> System.exit(0));
		new Thread(() -> {
			while (true) {
				Server.getInstance().getPacketHandler().sendRequest(new Request("hello"), res -> {
					System.out.println(res.getBody().get("msg").getAsString());
				});
				try {Thread.sleep(2000);} catch (Exception e) {}
				Server.getInstance().getPacketHandler().run();
			}
		}).start();

		// Gwent.main(args);
	}


	// TODO: this is just for testing. we will need a more sophisticated class
	static class Server {
		public static final String SERVER_IP = "127.0.0.1";
		public static final int SERVER_PORT = 12345;
		private static Server instance;
		private PacketHandler packetHandler;

		public PacketHandler getPacketHandler() {
			return packetHandler;
		}

		private Server(Socket socket) {
			packetHandler = new PacketHandler(socket);
		}

		public static Server getInstance() {
			if (instance == null) connect();
			return instance;
		}

		public static boolean connect() {
			try {
				if (instance != null && !instance.packetHandler.getNetNode().isClosed()) return true;
				Socket socket = new Socket(SERVER_IP, SERVER_PORT);
				instance = new Server(socket);
				instance.packetHandler.getNetNode().addOnClose(() -> {
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
