package org.apgrp10.gwent.model.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Response extends Packet {
	Response(JsonObject header, JsonObject body) {
		super(header, body);
	}

	public Response(long id, int status, JsonObject body) {
		super(makeHeader(id, status), body);
	}

	public Response(long id, int status) { this(id, status, new JsonObject()); }

	private static JsonObject makeHeader(long id, int status) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "response");
		header.addProperty("id", id);
		header.addProperty("status", status);
		return header;
	}

	public int getStatus() { return header.get("status").getAsInt(); }
	public boolean isOk() { return getStatus() == 200; }
	public long getRequestId() { return header.get("id").getAsLong(); }
}
