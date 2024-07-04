package org.apgrp10.gwent.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.Reader;

public class MGson {
	private static final Gson gson = new Gson();

	public static Gson get(boolean prettyPrinting, boolean serializeNulls) {
		GsonBuilder builder = new GsonBuilder();
		if (prettyPrinting) builder.setPrettyPrinting();
		if (serializeNulls) builder.serializeNulls();
		return builder.create();
	}

	public static String toJson(Object object) {
		return gson.toJson(object);
	}

	public static <T> T fromJson(String json, Class<T> type) {
		return gson.fromJson(json, type);
	}

	public static <T> T fromJson(JsonElement json, Class<T> type) {
		return gson.fromJson(json, type);
	}

	public static <T> T fromJson(Reader reader, Class<T> type) {
		return gson.fromJson(reader, type);
	}

	public static <T> T fromJson(JsonReader reader, Class<T> type) {
		return gson.fromJson(reader, type);
	}

	public static JsonElement toJsonElement(Object object) {
		return gson.toJsonTree(object);
	}

	public static JsonObject makeJsonObject(Object... objects) {
		JsonObject json = new JsonObject();
		if(objects.length % 2 != 0) throw new IllegalArgumentException("Invalid number of arguments");
		for (int i = 0; i < objects.length; i += 2) {
			if(!(objects[i] instanceof String)) throw new IllegalArgumentException("Key must be a string");
			String key = (String) objects[i];
			Object value = objects[i + 1];
			if (value instanceof String) json.addProperty(key, (String) value);
			else if (value instanceof Number) json.addProperty(key, (Number) value);
			else if (value instanceof Boolean) json.addProperty(key, (Boolean) value);
			else if (value instanceof Character) json.addProperty(key, (Character) value);
			else if (value instanceof JsonElement) json.add(key, (JsonElement) value);
			else if (value == null) json.add(key, null);
			else json.add(key, toJsonElement(value));
		}
		return json;
	}
}
