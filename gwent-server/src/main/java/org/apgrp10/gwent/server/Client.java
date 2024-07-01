package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.NetNode;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends NetNode {
	private static final ArrayList<Client> clients = new ArrayList<>();
	private User loggedInUser; // may be null if not logged in
	
	public Client(Socket socket) throws IOException {
		super(socket);
		clients.add(this);
		setDefaultListener(new ConnectionListener() {
			@Override
			public void onConnectionEstablished() {
				loggedInUser = null;
				ANSI.log("Client connected : " + socket.getInetAddress(), ANSI.CYAN, false);
			}
			
			@Override
			public void onConnectionLost() {
				clients.remove(Client.this);
				ANSI.log("Client disconnected : " + socket.getInetAddress(), ANSI.CYAN, false);
			}
		});
	}
	
	public static ArrayList<Client> connectedClients() {
		return clients;
	}
	
	
}
