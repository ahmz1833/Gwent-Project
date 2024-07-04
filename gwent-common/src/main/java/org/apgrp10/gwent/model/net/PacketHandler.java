package org.apgrp10.gwent.model.net;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apgrp10.gwent.utils.Callback;

public class PacketHandler implements Runnable {
	private final NetNode node;

	public PacketHandler(Socket socket) { node = new NetNode(socket, this::parsePacket); }

	public void addOnClose(Runnable fn) { node.addOnClose(fn); }

	public NetNode getNetNode() { return node; }
	public void run() {
		if (!node.isClosed())
			node.run();
	}

	private final Map<Long, Callback<Response>> resCallbacks = new HashMap<>();
	private final Map<String, Function<Request, Response>> reqCallbacks = new HashMap<>();

	public Function<Request, Response> setListener(String action, Function<Request, Response> cb) {
		reqCallbacks.put(action, cb);
		return cb;
	}

	public void send(Request req, Callback<Response> onReceive) {
		resCallbacks.put(req.getId(), onReceive);
		if (!node.send(req.toString().getBytes()))
			node.close();
	}

	public void send(Response res) {
		if (!node.send(res.toString().getBytes()))
			node.close();
	}

	private void parsePacket(byte data[]) {
		Packet packet;
		try {
			packet = Packet.parse(new String(data));
		} catch (Exception e) {
			// TODO: proper error handling
			node.close();
			return;
		}
		if (packet instanceof Response) handleResponse((Response)packet);
		if (packet instanceof Request) handleRequest((Request)packet);
	}

	private void handleRequest(Request req) {
		if (req.getAction().equals("ping")) {
			send(new Response(req.getId(), Response.OK_NO_CONTENT));
			return;
		}
		Function<Request, Response> cb = reqCallbacks.get(req.getAction());
		if (cb == null) {
			send(new Response(req.getId(), Response.BAD_REQUEST));
			return;
		}
		Response res = cb.apply(req);
		send(res);
	}

	private void handleResponse(Response res) {
		if (!resCallbacks.containsKey(res.getRequestId())) {
			node.close();
			return;
		}
		Callback<Response> cb = resCallbacks.remove(res.getRequestId());
		if (cb != null)
			cb.call(res);
	}

	public void ping(Runnable onReceive) {
		send(new Request("ping"), res -> onReceive.run());
	}
}
