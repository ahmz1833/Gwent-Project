package org.apgrp10.gwent.model.net;

import org.apgrp10.gwent.utils.Random;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Request extends Packet {
	Request(JsonObject header, JsonObject body) {
		super(header, body);
	}

	public Request(String action, JsonObject body) {
		super(makeHeader(action), body);
	}

	public Request(String action) { this(action, new JsonObject()); }

	private static JsonObject makeHeader(String action) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "request");
		header.addProperty("id", Random.nextId());
		header.addProperty("action", action);
		return header;
	}

	public String getAction() { return header.get("action").getAsString(); }
	public long getId() { return header.get("id").getAsLong(); }
}
