package org.apgrp10.gwent.model.net;

import org.apgrp10.gwent.utils.Requests;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public abstract class NetNode implements Closeable {
	private static final ArrayList<NetNode> nodes = new ArrayList<>();
	private static final Timer timer = new Timer("NetNode Connection Checker Timer", true);
	
	static {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				for (NetNode node : new ArrayList<>(nodes))
					node.checkConnectionTask.run();
			}
		}, 500, 2000);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (NetNode node : new ArrayList<>(nodes)) {
				node.close();
			}
		}));
	}
	
	protected final Socket socket;
	protected final DataInputStream inputStream;
	protected final DataOutputStream outputStream;
	private final ArrayList<ConnectionListener> listeners = new ArrayList<>();
	private ConnectionListener defaultListener;
	private boolean lastConnectionState;
	private final Runnable checkConnectionTask = new Runnable() {
		@Override
		public void run() {
			boolean alive = isAlive();
			if (alive != lastConnectionState) {
				lastConnectionState = alive;
				for (ConnectionListener listener : listeners) {
					if (alive) listener.onConnectionEstablished();
					else listener.onConnectionLost();
				}
			}
		}
	};
	private int timeout = 5000;
	
	public NetNode(Socket socket) throws IOException {
		this.socket = socket;
		try {
			inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create input/output streams for device", e);
		}
		if (!isAlive()) throw new IOException("The socket is not alive in the constructor of NetNode");
		socket.setSoTimeout(timeout);
		nodes.add(this);
	}
	
	public void setDefaultListener(ConnectionListener listener) {
		defaultListener = listener;
		if (!listeners.contains(listener)) listeners.add(listener);
	}
	
	public void addListener(ConnectionListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}
	
	public void clearListeners() {
		listeners.clear();
		if (defaultListener != null) listeners.add(defaultListener);
	}
	
	@Override
	public void close() {
		nodes.remove(this);
		if (socket == null || socket.isClosed() || !socket.isConnected())
			return;
		try {
			socket.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to close device", e);
		}
	}
	
	public DataInputStream in() {
		return inputStream;
	}
	
	public DataOutputStream out() {
		return outputStream;
	}
	
	public Socket socket() {
		return socket;
	}
	
	public void send(byte[] data) {
		if (!isAlive()) throw new RuntimeException("The socket is not alive in the send method of NetNode");
		try {
			outputStream.writeInt(data.length);
			outputStream.write(data);
			outputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to send data", e);
		}
	}
	
	public byte[] receive() throws IOException, InputNotAvailableException {
		if (!isAlive()) throw new IOException("The socket is not alive in the receive method of NetNode");
		try {
			int bytes;
			do {
				inputStream.mark(4);
				if (inputStream.available() < 4)
					throw new InputNotAvailableException();
				bytes = inputStream.readInt();
			} while (bytes == 0);
			if (inputStream.available() < bytes) {
				inputStream.reset();
				throw new InputNotAvailableException();
			}
			byte[] data = new byte[bytes];
			inputStream.readFully(data);
			return data;
		} catch (IOException e) {
			throw new RuntimeException("Failed to receive data", e);
		}
	}
	
//	public Response sendRequestAndWaitForResponse(Request request) throws Exception {
//
//	}
	
	public boolean isAlive() {
		if (socket == null || socket.isClosed() || !socket.isConnected())
			return false;
		try {
			// Send a heartbeat
			outputStream.writeInt(0);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public interface ConnectionListener {
		void onConnectionEstablished();
		
		void onConnectionLost();
	}
}
