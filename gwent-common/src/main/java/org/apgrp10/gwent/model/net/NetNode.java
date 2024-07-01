package org.apgrp10.gwent.model.net;

import org.apgrp10.gwent.utils.Requests;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NetNode implements Closeable, Runnable {
	protected final Socket socket;
	protected final InputStream inputStream;
	protected final OutputStream outputStream;
	private final ArrayList<ConnectionListener> listeners = new ArrayList<>();
	private ConnectionListener defaultListener;
	private boolean lastConnectionState;

	private void updateState(boolean alive) {
		if (lastConnectionState == alive)
			return;
		for (ConnectionListener listener : listeners) {
			if (alive) listener.onConnectionEstablished();
			else listener.onConnectionLost();
		}
	};

	private AsyncReader asyncReader;
	
	public NetNode(Socket socket, AsyncReader.Callback onReceive) {
		this.socket = socket;
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create input/output streams for device", e);
		}

		asyncReader = new AsyncReader(inputStream, onReceive, e -> {
			// TODO: proper error handling
			close();
			updateState(false);
		});
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
		if (socket == null || socket.isClosed() || !socket.isConnected())
			return;
		try {
			socket.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to close device", e);
		}
	}
	
	public InputStream in() {
		return inputStream;
	}
	
	public OutputStream out() {
		return outputStream;
	}
	
	public Socket socket() {
		return socket;
	}
	
	// TODO: hopefully this doesn't block but we need to do something to guarantee it
	public boolean send(byte[] data) {
		try {
			outputStream.write(AsyncReader.intToBytes(data.length));
			outputStream.write(data);
			outputStream.flush();
			return true;
		} catch (IOException e) {
			// TODO: proper error handling
			close();
			updateState(false);
			return false;
		}
	}
	
	public interface ConnectionListener {
		void onConnectionEstablished();
		
		void onConnectionLost();
	}

	@Override
	public void run() {
		if (lastConnectionState)
			asyncReader.run();
	}
}
