package org.apgrp10.gwent.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Callback;

public class Server {
	public static final String SERVER_IP = "37.152.178.57";
	public static final int SERVER_PORT = 12345;
	private static Server instance;
	private PacketHandler packetHandler;
	private List<Runnable> onClose = new ArrayList<>();

	public static Server instance() {
		if (instance == null)
			connect();
		return instance;
	}

	private Server(Socket socket) {
		packetHandler = new PacketHandler(socket);
	}

	public void sendRequest(Request req, Callback<Response> onReceive) {
		synchronized (packetHandler) { packetHandler.sendRequest(req, onReceive); }
	}
	public void sendRequest(Request req) { sendRequest(req, res -> {}); }

	public void sendResponse(Response res) {
		synchronized (packetHandler) { packetHandler.sendResponse(res); }
	}

	public void setListener(String action, Callback<Request> onReceive) {
		synchronized (packetHandler) { packetHandler.setListener(action, onReceive); }
	}

	public Runnable addOnClose(Runnable fn) {
		synchronized (onClose) { onClose.add(fn); }
		return fn;
	}

	public void removeOnClose(Runnable fn) {
		synchronized (onClose) { onClose.remove(fn); }
	}

	public static boolean connect() {
		try {
			if (instance != null && !instance.packetHandler.getNetNode().isClosed())
				return true;

			Socket socket = new Socket(SERVER_IP, SERVER_PORT);
			instance = new Server(socket);
			instance.packetHandler.getNetNode().addOnClose(() -> {
				ANSI.log("Connection to server lost.", ANSI.LRED, false);

				synchronized (instance.onClose) {
					for (Runnable fn : instance.onClose)
						fn.run();
				}
			});

			ANSI.log("Connected to server.", ANSI.LGREEN, false);
			return true;
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to connect to server", e);
			return false;
		}
	}

	public static void disconnect() {
		if (instance == null)
			return;
		synchronized (instance.packetHandler) {
			instance.packetHandler.getNetNode().close();
		}
	}

	private Thread thread;

	public void startThread() {
		if (thread != null)
			return;

		thread = new Thread() {
			private long lastPing = System.currentTimeMillis();
			private boolean lastPingReceived = true;

			@Override
			public void run() {
				while (true) {
					synchronized (packetHandler) { 
						if (packetHandler.getNetNode().isClosed())
							break;

						long time = System.currentTimeMillis();
						if (time - lastPing >= 5000) {
							if (!lastPingReceived) {
								disconnect();
								break;
							}
							lastPing = time;
							lastPingReceived = false;
							packetHandler.ping(() -> lastPingReceived = true);
						}

						packetHandler.run();
					}

					try { Thread.sleep(10); } catch (Exception e) {}
				}
			}
		};

		thread.setDaemon(true);
		thread.start();
	}
}
