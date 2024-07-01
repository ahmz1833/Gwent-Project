package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.model.net.Packet;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client implements Task {
	private User loggedInUser; // may be null if not logged in
	private Runnable onDestruction;
	private NetNode node;
	private List<Runnable> commandQueue = new ArrayList<>();

	public Client(Socket socket) throws IOException {
		node = new NetNode(socket, this::parsePacket);
		node.setDefaultListener(new NetNode.ConnectionListener() {
			@Override
			public void onConnectionEstablished() {
				loggedInUser = null;
				ANSI.log("Client connected : " + socket.getInetAddress(), ANSI.CYAN, false);
			}
			
			@Override
			public void onConnectionLost() {
				destruct();
				ANSI.log("Client disconnected : " + socket.getInetAddress(), ANSI.CYAN, false);
			}
		});
	}
	
	public NetNode getNetNode() { return node; }

	private void parsePacket(byte data[]) {
		Packet packet;
		try {
			packet = Packet.parse(new String(data));
		} catch (Exception e) {
			// TODO: proper error handling
			destruct();
			return;
		}
		// TODO: handle packet
	}

	private boolean destructed;
	private void destruct() {
		destructed = true;
		if (onDestruction != null)
			onDestruction.run();
	}
	
	public void setOnDestruction(Runnable fn) {
		onDestruction = fn;
	}

	public void addCommand(Runnable cmd) {
		synchronized (commandQueue) {
			commandQueue.add(cmd);
		}
	}

	@Override
	public boolean isDone() {
		return destructed;
	}

	@Override
	public void run() {
		node.run();
		List<Runnable> copy;
		synchronized (commandQueue) {
			copy = new ArrayList<>(commandQueue);
			commandQueue.clear();
		}
		for (Runnable fn : copy)
			fn.run();
	}

	public void send(byte b[]) {
		addCommand(() -> {
			boolean res = node.send(b);
			if (!res)
				destruct();
		});
	}
}
