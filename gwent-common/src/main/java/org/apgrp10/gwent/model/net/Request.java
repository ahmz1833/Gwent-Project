package org.apgrp10.gwent.model.net;

import com.google.gson.JsonObject;
import org.apgrp10.gwent.utils.Random;

public class Request extends Packet {
	Request(JsonObject header, JsonObject body) {
		super(header, body);
	}

	public Request(String action, JsonObject body) {
		super(makeHeader(action), body);
	}

	public Request(String action) {this(action, new JsonObject());}

	private static JsonObject makeHeader(String action) {
		JsonObject header = new JsonObject();
		header.addProperty("type", "request");
		header.addProperty("id", Random.nextId());
		header.addProperty("action", action);
		return header;
	}

	public String getAction() {return header.get("action").getAsString();}

	public long getId() {return header.get("id").getAsLong();}

	public Response response(int status) {return new Response(getId(), status);}

	public Response response(int status, JsonObject body) {return new Response(getId(), status, body);}
}
