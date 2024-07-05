package org.apgrp10.gwent.client;

import javafx.application.Platform;
import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server {
	public static final String SERVER_IP = "37.152.178.57";
	public static final int SERVER_PORT = 12345;
	private static PacketHandler packetHandler;
	private static final List<Runnable> onClose = new ArrayList<>();
	private static boolean running;
	private static long lastPing = System.currentTimeMillis();
	private static boolean lastPingReceived = true;

	private Server() {}

	public static boolean isConnected() {
		return packetHandler != null && !packetHandler.getNetNode().isClosed();
	}

	public static boolean connect() {
		try {
			if (isConnected())
				return true;

			Socket socket = new Socket(SERVER_IP, SERVER_PORT);
			packetHandler = new PacketHandler(socket);
			packetHandler.getNetNode().addOnClose(() -> {
				ANSI.log("Connection to server lost.", ANSI.LRED, false);

				for (Runnable fn : new ArrayList<>(onClose))
					fn.run();
			});
			packetHandler.ping(() -> ANSI.log("Connected to server.", ANSI.LGREEN, false));
			return true;
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to connect to server", e);
			return false;
		}
	}

	public static void disconnect() {
		if (packetHandler == null) return;
		packetHandler.getNetNode().close();
	}

	public static void send(Request req) {
		if (!isConnected()) return;
		send(req, res -> {});
	}

	public static void send(Request req, Consumer<Response> onReceive) {
		if (!isConnected()) return;
		packetHandler.send(req, onReceive);
	}

	public static void send(Response res) {
		if (!isConnected()) return;
		packetHandler.send(res);
	}

	public static void setListener(String action, Function<Request, Response> onReceive) {
		if (!isConnected()) return;
		packetHandler.setListener(action, onReceive);
	}

	public static Runnable addOnClose(Runnable fn) {
		onClose.add(fn);
		return fn;
	}

	public static void removeOnClose(Runnable fn) {
		onClose.remove(fn);
	}

	private static void fxLoop() {
		if (!running) return;
		Platform.runLater(Server::fxLoop);

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

	public static void run() {
		if (!running) {
			running = true;
			fxLoop();
		}
	}

	public static void stop() {
		running = false;
	}
}
