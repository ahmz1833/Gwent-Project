package org.apgrp10.gwent.model.net;

import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import org.apgrp10.gwent.utils.MGson;

public abstract class Packet {
	protected final JsonObject header;
	protected final JsonObject body;

	public Packet(JsonObject header, JsonObject body) {
		this.header = header;
		this.body = body;
	}

	public static Packet parse(String string) throws MalformedJsonException {
		JsonObject full = MGson.fromJson(string, JsonObject.class);
		JsonObject header = full.get("header").getAsJsonObject();
		JsonObject body = full.get("body").getAsJsonObject();
		if (header == null || body == null) throw new MalformedJsonException("Header or body is null");
		if (header.get("type").getAsString().equals("request")) return new Request(header, body);
		else if (header.get("type").getAsString().equals("response")) return new Response(header, body);
		else throw new MalformedJsonException("Invalid type");
	}

	@Override
	public String toString() {
		JsonObject full = new JsonObject();
		full.add("header", header);
		full.add("body", body);
		return MGson.toJson(full);
	}

	public JsonObject getHeader() { return header; }
	public JsonObject getBody() { return body; }
}
