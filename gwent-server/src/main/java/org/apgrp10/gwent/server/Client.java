package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.Packet;
import org.apgrp10.gwent.utils.ANSI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Client {
	
	private static final HashMap<String, Client> clients = new HashMap<>();
	private final String id;
	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	
	public static int connectedClients() {
		return (int) clients.values().stream().filter(Client::isAlive).count();
	}
	
	private boolean isAlive() {
		try {
			outputStream.writeByte(0);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	private Client(String id, Socket socket) {
		this.id = id;
		setSocket(socket);
		clients.put(id, this);
	}
	
	public static Client acceptConnection(ServerSocket serverSocket) throws IOException {
		Socket socket = serverSocket.accept();
		byte[] identity = new byte[8];
		socket.getInputStream().read(identity, 0, 8);  // Identity is a string of 8 characters
		String id = new String(identity);
		if (clients.containsKey(id)) {
			Client client = clients.get(id);
			client.setSocket(socket);
			return client;
		}
		return new Client(id, socket);
	}
	
	private void setSocket(Socket socket) {
		this.socket = socket;
		try {
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to create input/output streams for client", e);
		}
	}
	
	public String getIdString() {
		return id;
	}
	
	public void close() {
		try {
			socket.close();
			clients.remove(id);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to close client socket", e);
		}
	}
	
//	public void send(Packet packet) {
//		if(isAlive())
//		try {
//			packet.write(outputStream);
//		} catch (IOException e) {
//			ANSI.logError(System.err, "Failed to send message to client", e);
//		}
//	}
//
//	public Packet receive() {
////		try {
//////			return inputStream.readUTF();
////		} catch (IOException e) {
////			ANSI.logError(System.err, "Failed to receive message from client", e);
////			return null;
////		}
//	}
}
