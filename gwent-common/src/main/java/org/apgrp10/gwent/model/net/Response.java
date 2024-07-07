package org.apgrp10.gwent.model.net;

import com.google.gson.JsonObject;

public class Response extends Packet {
	public static final int OK = 200;
	public static final int ACCEPTED = 202;
	public static final int OK_NO_CONTENT = 204;
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int NOT_FOUND = 404;
	public static final int CONFLICT = 409;
	public static final int INTERNAL_SERVER_ERROR = 500;

	Response(JsonObject header, JsonObject body) {
		super(header, body);
	}

	public Response(long id, int status, JsonObject body) {
		super(makeHeader(id, status), body);
	}

	public Response(long id, int status) {this(id, status, new JsonObject());}

	private static JsonObject makeHeader(long id, int status) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "response");
		header.addProperty("id", id);
		header.addProperty("status", status);
		return header;
	}

	public int getStatus() {return header.get("status").getAsInt();}

	public boolean isOk() {return 200 <= getStatus() && getStatus() <= 299;}

	public long getRequestId() {return header.get("id").getAsLong();}
}
