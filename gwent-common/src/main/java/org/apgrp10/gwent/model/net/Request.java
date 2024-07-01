package org.apgrp10.gwent.model.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Request extends Packet {
	private static final Gson gson = new Gson();
	
	Request(JsonObject header, JsonObject body) {
		super(header, body);
	}
	
	public Request(int time, String action, JsonObject body) {
		super(makeHeader(time, action), body);
	}
	
	private static JsonObject makeHeader(int time, String action) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "request");
		header.addProperty("time", time);
		header.addProperty("action", action);
		return header;
	}
	
	public String getAction() {
		return header.get("action").getAsString();
	}
	
	public int getTime() {
		return header.get("time").getAsInt();
	}
}
