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

import javafx.application.Platform;

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

	public void sendRequest(Request req, Callback<Response> onReceive) { packetHandler.sendRequest(req, onReceive); }
	public void sendRequest(Request req) { sendRequest(req, res -> {}); }
	public void sendResponse(Response res) { packetHandler.sendResponse(res); }
	public void setListener(String action, Callback<Request> onReceive) { packetHandler.setListener(action, onReceive); }

	public Runnable addOnClose(Runnable fn) {
		onClose.add(fn);
		return fn;
	}
	public void removeOnClose(Runnable fn) {
		onClose.remove(fn);
	}

	public static boolean isConnected() {
		return instance != null && !instance.packetHandler.getNetNode().isClosed();
	}

	public static boolean connect() {
		try {
			if (isConnected())
				return true;

			Socket socket = new Socket(SERVER_IP, SERVER_PORT);
			instance = new Server(socket);
			instance.packetHandler.getNetNode().addOnClose(() -> {
				ANSI.log("Connection to server lost.", ANSI.LRED, false);

				for (Runnable fn : new ArrayList<>(instance.onClose))
					fn.run();
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
		instance.packetHandler.getNetNode().close();
	}

	private boolean running;
	private long lastPing = System.currentTimeMillis();
	private boolean lastPingReceived = true;

	private void fxLoop() {
		if (!running)
			return;
		Platform.runLater(this::fxLoop);

		if (packetHandler.getNetNode().isClosed())
			running = false;

		long time = System.currentTimeMillis();
		if (time - lastPing >= 5000) {
			if (!lastPingReceived) {
				disconnect();
				running = false;
				return;
			}
			lastPing = time;
			lastPingReceived = false;
			packetHandler.ping(() -> lastPingReceived = true);
		}

		packetHandler.run();
	}

	public void run() { 
		if (!running) {
			running = true;
			fxLoop();
		}
	}

	public void stop() {
		running = false;
	}
}
