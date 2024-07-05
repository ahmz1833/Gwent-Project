package org.apgrp10.gwent.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(Command.CommandAdapter.class)
public interface Command {
	static Command fromBase64(String str) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(str.getBytes()));
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Command) ois.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	default String toBase64() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ous = new ObjectOutputStream(baos);
			ous.writeObject(this);
			return new String(Base64.getEncoder().encode(baos.toByteArray()));
		} catch (Exception e) {
			// should be unreachable
			System.exit(1);
			return null;
		}
	}

	static class CommandAdapter extends TypeAdapter<Command> {
		@Override
		public void write(JsonWriter out, Command value) throws IOException {
			out.value(value.toBase64());
		}

		@Override
		public Command read(JsonReader in) throws IOException {
			return Command.fromBase64(in.nextString());
		}
	}

	int player();

	record VetoCard(int player, int cardId) implements Command, Serializable {}

	record PlayCard(int player, int cardId, int row) implements Command, Serializable {}

	record SwapCard(int player, int cardId1, int cardId2) implements Command, Serializable {}

	record PlayLeader(int player) implements Command, Serializable {}

	record MoveToHand(int player, int cardId) implements Command, Serializable {}

	record Pass(int player) implements Command, Serializable {}

	record SetActiveCard(int player, int cardId) implements Command, Serializable {}

	record PickResponse(int player, int cardId, String what) implements Command, Serializable {}

	record Cheat(int player, int cheatId) implements Command, Serializable {}

	record Sync(int player) implements Command, Serializable {}
}
