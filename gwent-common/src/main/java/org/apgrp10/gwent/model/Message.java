package org.apgrp10.gwent.model;


import com.google.gson.Gson;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;

import java.io.*;
import java.time.Instant;
import java.util.Base64;

public class Message implements Serializable {
	private final int id;
	private final int numberOfReaction;
	private final long userId;
	private final byte type;    //0 -> new text message
	//1 -> new reaction message
	//2 -> delete text message
	//3 -> delete reaction
	//4 -> edit a message
	private final Instant creationTime;
	private final int replyOn;
	private String text;

	private Message(int id, String text, int numberOfReaction, byte type, long userId, Instant creationTime, int replyOn) {
		this.replyOn = replyOn;
		this.id = id;
		this.text = text;
		this.numberOfReaction = numberOfReaction;
		this.type = type;
		this.userId = userId;
		this.creationTime = creationTime;
	}

	public static Message newTextMessage(int id, String text, long owner, int replyOn) {
		return new Message(id, text, -1, (byte) (0), owner, Instant.now(), replyOn);
	}

	public static Message deleteTextMessage(int id, long owner) {
		return new Message(id, "", -1, (byte) (2), owner, Instant.now(), 0);
	}

	public static Message newReactionMessage(int id, int reaction, long owner) {
		return new Message(id, "", reaction, (byte) (1), owner, Instant.now(), 0);
	}

	public static Message deleteReactionMessage(int id, int reaction, long owner) {
		return new Message(id, "", reaction, (byte) (3), owner, Instant.now(), 0);
	}

	public static Message editMessage(int id, String text, long owner) {
		return new Message(id, text, -1, (byte) (4), owner, Instant.now(), 0);
	}

	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNumberOfReaction() {
		return numberOfReaction;
	}

	public User.PublicInfo getOwner() {
		//TODO return PublicInfo of this user id;
		return new User.PublicInfo(System.currentTimeMillis(), "salam", "khobi?", Avatar.random());
	}

	public Instant getCreationTime() {
		return creationTime;
	}

	public int getReplyOn() {
		return replyOn;
	}

	public byte getType() {
		return type;
	}
	public String toString (){
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(baos)){
			oos.writeObject(this);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			ANSI.logError(System.err, "invalid message", e);
			return null;
		}
	}
	public static Message fromString(String json) {
		byte[] data = Base64.getDecoder().decode(json);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
		     ObjectInputStream ois = new ObjectInputStream(bais)) {
				return (Message) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			ANSI.logError(System.err, "invalid message", e);
			return null;
		}
	}
}
