package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.Function;

public class Client extends Task {
	private final PacketHandler packetHandler;
	private User loggedInUser; // may be null if not logged in
	private boolean destructed;
	private long lastPing;
	private boolean lastPingReceived;

	public Client(Socket socket) throws IOException {
		packetHandler = new PacketHandler(socket);
		packetHandler.addOnClose(this::destruct);
		lastPing = System.currentTimeMillis();
		lastPingReceived = true;
		ANSI.log("Client connected : " + socket.getInetAddress(), ANSI.CYAN, false);
	}

	public NetNode getNetNode() {return packetHandler.getNetNode();}

	private void destruct() {
		if (destructed)
			return;
		destructed = true;
		getNetNode().close();
		ANSI.log("Client disconnected : " + getNetNode().socket().getInetAddress(), ANSI.CYAN, false);
	}

	@Override
	public boolean isDone() {
		return destructed;
	}

	@Override
	public void iterate() {
		long time = System.currentTimeMillis();
		if (time - lastPing >= 5000) {
			if (!lastPingReceived) {
				destruct();
				return;
			}
			lastPing = time;
			lastPingReceived = false;
			packetHandler.ping(() -> lastPingReceived = true);
		}
		packetHandler.run();
	}

	public void send(Request req, Consumer<Response> onReceive) {
		addCommand(() -> packetHandler.send(req, onReceive));
	}

	public void send(Request req) {send(req, res -> {});}

	public void send(Response res) {
		addCommand(() -> packetHandler.send(res));
	}

	public void setListener(String action, Function<Request, Response> onReceive) {
		addCommand(() -> packetHandler.setListener(action, onReceive));
	}

	public User loggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(User user) {
		loggedInUser = user;
	}

	public AuthLevel getAuthLevel() {
		return loggedInUser == null ? AuthLevel.NOT_LOGGED_IN : AuthLevel.LOGGED_IN;
	}

	public enum AuthLevel {NONE, LOGGED_IN, NOT_LOGGED_IN, ALL}
}
