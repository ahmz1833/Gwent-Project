package org.apgrp10.gwent.model;

import java.io.*;
import java.util.Base64;

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
	
	record VetoCard(int player, int cardId) implements Command, Serializable {}
	
	record PlayCard(int player, int cardId, int row) implements Command, Serializable {}
	
	record SwapCard(int player, int cardId1, int cardId2) implements Command, Serializable {}
	
	record PlayLeader(int player) implements Command, Serializable {}
	
	record MoveToHand(int player, int cardId) implements Command, Serializable {}
	
	record Pass(int player) implements Command, Serializable {}
	
	record SetActiveCard(int player, int cardId) implements Command, Serializable {}
	
	record PickResponse(int player, int cardId, String what) implements Command, Serializable {}
	
	record Cheat(int player, int cheatId) implements Command, Serializable {}
	
	record Sync() implements Command, Serializable {}
}
