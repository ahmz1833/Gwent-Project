package org.apgrp10.gwent.client;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server {
	public static final String SERVER_IP = "37.152.178.57";
	public static final int SERVER_PORT = 12345;
	private static final List<Runnable> onClose = new ArrayList<>();
	private static final ArrayList<ChangeListener<? super Boolean>> changeListeners = new ArrayList<>();
	private static final ArrayList<InvalidationListener> invalidationListeners = new ArrayList<>();
	private static PacketHandler packetHandler;
	public static ReadOnlyBooleanProperty connectionProperty = new ReadOnlyBooleanProperty() {
		@Override
		public Object getBean() {return Server.class;}

		@Override
		public String getName() {return "connectionProperty";}

		@Override
		public boolean get() {return isConnected();}

		@Override
		public void addListener(ChangeListener<? super Boolean> changeListener) {changeListeners.add(changeListener);}

		@Override
		public void removeListener(ChangeListener<? super Boolean> changeListener) {changeListeners.remove(changeListener);}

		@Override
		public void addListener(InvalidationListener invalidationListener) {invalidationListeners.add(invalidationListener);}

		@Override
		public void removeListener(InvalidationListener invalidationListener) {invalidationListeners.remove(invalidationListener);}
	};
	private static boolean running;
	private static long lastPing = System.currentTimeMillis();
	private static boolean lastPingReceived = true;

	private Server() {}

	public static boolean isConnected() {
		return packetHandler != null && !packetHandler.getNetNode().isClosed();
	}

	public static void connect() {
		try {
			if (isConnected()) return;
			ANSI.log("Trying to connect to server", ANSI.LYELLOW, false);
			Socket socket = new Socket(SERVER_IP, SERVER_PORT);
			packetHandler = new PacketHandler(socket);
			lastPingReceived = true; // reset this for establishing new connection
			packetHandler.getNetNode().addOnClose(() -> {
				packetHandler = null;
				invalidationListeners.forEach(invalidationListener -> invalidationListener.invalidated(connectionProperty));
				changeListeners.forEach(changeListener -> changeListener.changed(connectionProperty, true, false));
			});
			packetHandler.ping(() -> {
				lastPingReceived = true;
				invalidationListeners.forEach(invalidationListener -> invalidationListener.invalidated(connectionProperty));
				changeListeners.forEach(changeListener -> changeListener.changed(connectionProperty, false, true));
			});
		} catch (IOException e) {
			ANSI.log("Failed to connect to server.", ANSI.LRED, false);
			Platform.runLater(() -> {
				invalidationListeners.forEach(invalidationListener -> invalidationListener.invalidated(connectionProperty));
				changeListeners.forEach(changeListener -> changeListener.changed(connectionProperty, true, false));
			});
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

	public static Response sendAndWait(Request req) {
		if (!isConnected()) return null;
		AtomicBoolean done = new AtomicBoolean(false);
		Response[] res = new Response[1];
		packetHandler.send(req, response -> {
			res[0] = response;
			done.set(true);
		});
		while (!done.get()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return res[0];
	}

	public static void send(Response res) {
		if (!isConnected()) return;
		packetHandler.send(res);
	}

	public static void setListener(String action, Function<Request, Response> onReceive) {
		if (!isConnected()) return;
		packetHandler.setListener(action, onReceive);
	}

	private static void fxLoop() {
		if (!running) return;
		Platform.runLater(Server::fxLoop);

		if (!isConnected()) {
			running = false;
			return;
		}

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
