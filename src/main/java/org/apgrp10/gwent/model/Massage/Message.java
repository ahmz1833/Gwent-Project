package org.apgrp10.gwent.model.Massage;


import org.apgrp10.gwent.model.User;

import java.time.Instant;

public class Message {
	private final int id;
	private final String text;
	private final int numberOfReaction;
	private final byte type;    //0 -> new text message
	//1 -> new reaction message
	//2 -> delete text message
	//3 -> delete reaction
	//4 -> edit a message
	private final User owner;
	private final Instant creationTime;

	private Message(int id, String text, int numberOfReaction, byte type, User owner, Instant creationTime) {
		this.id = id;
		this.text = text;
		this.numberOfReaction = numberOfReaction;
		this.type = type;
		this.owner = owner;
		this.creationTime = creationTime;
	}

	public static Message newTextMessage(int id, String text, User owner) {
		return new Message(id, text, -1, (byte) (0), owner, Instant.now());
	}

	public static Message deleteTextMessage(int id, User owner) {
		return new Message(id, "", -1, (byte) (2), owner, Instant.now());
	}

	public static Message newReactionMessage(int id, int reaction, User owner) {
		return new Message(id, "", reaction, (byte) (1), owner, Instant.now());
	}

	public static Message deleteReactionMessage(int id, int reaction, User owner) {
		return new Message(id, "", reaction, (byte) (3), owner, Instant.now());
	}

	public static Message editMessage(int id, String text, User owner) {
		return new Message(id, text, -1, (byte) (4), owner, Instant.now());
	}

	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public int getNumberOfReaction() {
		return numberOfReaction;
	}

	public User getOwner() {
		return owner;
	}

	public Instant getCreationTime() {
		return creationTime;
	}

	public byte getType() {
		return type;
	}
}
