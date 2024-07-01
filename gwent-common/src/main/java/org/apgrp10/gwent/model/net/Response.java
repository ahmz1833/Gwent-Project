package org.apgrp10.gwent.model.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Response extends Packet {
	private static final Gson gson = new Gson();
	
	Response(JsonObject header, JsonObject body) {
		super(header, body);
	}
	
	public Response(int time, int requestTime, boolean ok, JsonObject body) {
		super(makeHeader(time, requestTime, ok), body);
	}
	
	private static JsonObject makeHeader(int time, int requestTime, boolean ok) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "response");
		header.addProperty("time", time);
		header.addProperty("responseTo", requestTime);
		header.addProperty("ok", ok);
		return header;
	}
	
	public boolean isOk() {
		return header.get("ok").getAsBoolean();
	}
	
	public int getRelatedRequest() {
		return header.get("responseTo").getAsInt();
	}
	
	public int getTime() {
		return header.get("time").getAsInt();
	}
}