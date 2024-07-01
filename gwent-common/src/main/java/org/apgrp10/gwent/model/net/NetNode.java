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
	private final ArrayList<Runnable> onClose = new ArrayList<>();

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
		});
	}
	public NetNode(Socket socket) { this(socket, null); }

	public void setOnReceive(AsyncReader.Callback cb) { asyncReader.setOnReceive(cb); }

	public Runnable addOnClose(Runnable fn) { onClose.add(fn); return fn; }
	public void removeOnClose(Runnable fn) { onClose.remove(fn); }

	public boolean isClosed() {
		return socket == null || socket.isClosed() || !socket.isConnected();
	}
	
	@Override
	public void close() {
		if (isClosed())
			return;
		try {
			socket.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to close device", e);
		}
		for (Runnable fn : onClose)
			fn.run();
	}
	
	public InputStream in() { return inputStream; }
	public OutputStream out() { return outputStream; }
	public Socket socket() { return socket; }
	
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
			return false;
		}
	}
	
	@Override
	public void run() {
		if (!isClosed())
			asyncReader.run();
	}
}
