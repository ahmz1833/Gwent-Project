package org.apgrp10.gwent.model.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public abstract class Packet {
	protected final JsonObject header;
	protected final JsonObject body;

	public static interface Callback { public void call(Packet packet); }
	
	public Packet(JsonObject header, JsonObject body) {
		this.header = header;
		this.body = body;
	}
	
	public static Packet parse(String string) throws MalformedJsonException {
		JsonObject full = new GsonBuilder().create().fromJson(string, JsonObject.class);
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
		return new Gson().toJson(full);
	}
	
	public JsonObject getHeader() {
		return header;
	}
	
	public JsonObject getBody() {
		return body;
	}
}
