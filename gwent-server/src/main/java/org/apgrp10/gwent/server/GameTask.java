package org.apgrp10.gwent.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.*;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GameTask extends Task {
	private final Data[] data = {new Data(), new Data()};
	private final List<Message> msgs = new ArrayList<>(), publicMsgs = new ArrayList<>();
	private final List<Command> cmds = new ArrayList<>();
	private final List<Client> liveClients = new ArrayList<>();
	private final Consumer<GameRecord> onEnd;
	private GameController gameController;
	private boolean done;
	private long seed;

	public GameTask(Client c1, Client c2, Deck d1, Deck d2, Consumer<GameRecord> onEnd) {
		data[0].client = c1;
		data[0].deck = d1;
		data[1].client = c2;
		data[1].deck = d2;
		data[0].user = c1.loggedInUser();
		data[1].user = c2.loggedInUser();
		data[0].client.setListener("chatMessage", req -> handleMessage(req, false));
		data[1].client.setListener("chatMessage", req -> handleMessage(req, false));
		this.onEnd = onEnd;
		start();
	}

	private Response handleCommand(Request req) {
		Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
		sendCommand(cmd);
		return req.response(Response.OK_NO_CONTENT);
	}

	private Response handleMessage(Request req, boolean isPublic) {
		Message msg = Message.fromString(req.getBody().get("msg").getAsString());
		if (msg.getType() == 0)
			msg.setId(Random.nextId());
		addCommand(() -> {
			if (!isPublic) {
				for (int i = 0; i < 2; i++)
					if (data[i].client != null)
						data[i].client.send(new Request("chatMessage", MGson.makeJsonObject("msg", msg.toString())));
				msgs.add(msg);
			}
			for (Client client : liveClients)
				client.send(new Request("chatMessage", MGson.makeJsonObject("msg", msg.toString())));
			publicMsgs.add(msg);
		});
		return req.response(Response.OK_NO_CONTENT);
	}

	private void start() {
		addCommand(() -> {
			seed = Random.nextPosLong();

			Request startingRequest = startingRequest("start", false, false);
			data[0].client.send(startingRequest);
			data[1].client.send(startingRequest);

			gameController = new GameController(
					new DummyInputController(),
					new DummyInputController(),
					data[0].user.publicInfo(),
					data[1].user.publicInfo(),
					data[0].deck,
					data[1].deck,
					seed,
					null,
					gr -> {
						done = true;
						removeAllListeners();
						ANSI.log("game record: " + gr);
						onEnd.accept(gr);
					},
					// these two are not important
					0,
					false
			);

			data[0].client.setListener("command", this::handleCommand);
			data[1].client.setListener("command", this::handleCommand);
		});
	}

	private void removeAllListeners() {
		for (int i = 0; i < 2; i++)
			if (data[i].client != null) {
				data[i].client.setListener("command", null);
				data[i].client.setListener("chatMessage", null);
			}
		for (Client client : liveClients)
			client.setListener("chatMessage", null);
	}

	public boolean isDone() {
		return done;
	}

	public void sendCommandAsync(Command cmd) {
		cmds.add(cmd);
		JsonObject commandBody = MGson.makeJsonObject("cmd", cmd.toBase64(), "player", cmd.player());
		for (int i = 0; i < 2; i++)
			if (data[i].client != null)
				data[i].client.send(new Request("command", commandBody));
		for (Client client : liveClients)
			client.send(new Request("command", commandBody));
		gameController.sendCommand(cmd);
	}

	public void sendCommand(Command cmd) {
		addCommand(() -> sendCommandAsync(cmd));
	}

	private Request startingRequest(String action, boolean fastForward, boolean isPublic) {
		JsonObject startBody = MGson.makeJsonObject(
				"seed", seed,
				"user1", MGson.toJsonElement(data[0].user.publicInfo()),
				"user2", MGson.toJsonElement(data[1].user.publicInfo()),
				"deck1", data[0].deck.toJsonString(),
				"deck2", data[1].deck.toJsonString()
		);
		if (fastForward) {
			startBody.add("cmds", MGson.toJsonElement(cmds.stream()
					.map(Command::toBase64).collect(Collectors.toList())));
			startBody.add("msgs", MGson.toJsonElement((isPublic ? publicMsgs : msgs).stream()
					.map(Message::toString).collect(Collectors.toList())));
		}
		return new Request(action, startBody);
	}

	// TODO: use this in requests.java for attending a live-watching
	public void addLiveClient(Client client) {
		addCommand(() -> {
			client.send(startingRequest("liveRequest", true, true));
			liveClients.add(client);
			client.setListener("chatMessage", req -> handleMessage(req, true));
		});
	}

	private Client continueWaitingResponse;

	protected void iterate() {
		if (done)
			return;

		for (int ii = 0; ii < 2; ii++) {
			final int i = ii;
			Data d = data[i];
			if (d.client != null && d.client.isDone()) {
				d.client = null;
				d.disTime = System.currentTimeMillis();
				sendCommandAsync(new Command.Connection(i, false));
			}
			if (d.client == null) {
				if (continueWaitingResponse == null) {
					Client c = Client.clientOfUser(d.user);
					if (c != null) {
						continueWaitingResponse = c;
						c.send(startingRequest("continueGame", true, false), res -> {
							if (res.isOk()) {
								sendCommand(new Command.Connection(i, true));
								addCommand(() -> { d.client = c; });
							}
						});
					}
				} else if (continueWaitingResponse.isDone()) {
					continueWaitingResponse = null;
				}
				if (System.currentTimeMillis() - d.disTime >= 60_000) {
					sendCommandAsync(new Command.Resign(i, "disconnected"));
					sendCommandAsync(new Command.Sync(i));
					return;
				}
			}
		}

		// Remove all disconnected live-watcher clients
		liveClients.removeIf(Client::isDone);
	}

	private static class Data {
		public Client client;
		public Deck deck;
		public long disTime;
		public User user;
	}
}
