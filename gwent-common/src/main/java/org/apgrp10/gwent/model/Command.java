package org.apgrp10.gwent.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public interface Command {
	public static record VetoCard(int player, int cardId) implements Command, Serializable {}
	public static record PlayCard(int player, int cardId, int row) implements Command, Serializable {}
	public static record SwapCard(int player, int cardId1, int cardId2) implements Command, Serializable {}
	public static record PlayLeader(int player) implements Command, Serializable {}
	public static record MoveToHand(int player, int cardId) implements Command, Serializable {}
	public static record Pass(int player) implements Command, Serializable {}
	public static record SetActiveCard(int player, int cardId) implements Command, Serializable {}
	public static record PickResponse(int player, int cardId, String what) implements Command, Serializable {}
	public static record Cheat(int player, int cheatId) implements Command, Serializable {}
	public static record Sync() implements Command, Serializable {}

	default public String toBase64() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ous = new ObjectOutputStream(baos);
			ous.writeObject(this);
			return new String(Base64.getEncoder().encode(baos.toByteArray()));
		} catch (Exception e) {
			// wtf?
			System.exit(1);
			return null;
		}
	}

	static public Command fromBase64(String str) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(str.getBytes()));
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Command)ois.readObject();
		} catch (Exception e) {
			return null;
		}
	}
}
