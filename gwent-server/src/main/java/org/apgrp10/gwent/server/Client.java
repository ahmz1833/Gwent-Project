package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.model.net.Packet;
import org.apgrp10.gwent.model.net.PacketHandler;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Callback;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client extends Task {
	private User loggedInUser; // may be null if not logged in
	private boolean destructed;
	private final PacketHandler packetHandler;
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
			packetHandler.ping(() -> { lastPingReceived = true; });
		}
		packetHandler.run();
	}

	public void sendRequest(Request req, Callback<Response> onReceive) {
		addCommand(() -> {
			packetHandler.sendRequest(req, onReceive);
		});
	}
	public void sendRequest(Request req) { sendRequest(req, res -> {}); }

	public void sendResponse(Response res) {
		addCommand(() -> {
			packetHandler.sendResponse(res);
		});
	}

	public void setListener(String action, Callback<Request> onReceive) {
		addCommand(() -> {
			packetHandler.setListener(action, onReceive);
		});
	}
}
