package org.apgrp10.gwent.server;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.SubList;

import java.util.ArrayList;

public class HandlingThread extends Thread {
	private final SubList<Client> assignedClients;
	
	public HandlingThread(ArrayList<Client> allClients) {
		assignedClients = new SubList<>(allClients);
	}
	
	public void addClient(Client client) {
		assignedClients.add(client);
	}
	
	@Override
	public void run() {
		while (true) {
			for (Client client : assignedClients) {
				
				// for test, sending each 1000 ms a message to the server
				try {
					Thread.sleep(1000);
					client.send(("Hello from \nserver\n khobi? " + System.currentTimeMillis() + "\nnaa???  ").getBytes());
				} catch (Exception e) {
					ANSI.logError(System.err, "Failed to send message to client", e);
				}
			}
		}
	}
}