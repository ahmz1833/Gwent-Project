package org.apgrp10.gwent.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
	private GameController gameController;
	private static class Data {
		public Client client;
		public Deck deck;
		public long disTime;
		public User user;
	};
	private Data data[] = {new Data(), new Data()};
	private boolean done;
	private List<Message> msgs = new ArrayList<>();
	private List<Message> publicMsgs = new ArrayList<>();
	private List<Command> cmds = new ArrayList<>();
	private List<Client> liveClients = new ArrayList<>();
	private long seed;
	private Consumer<GameRecord> onEnd;

	public GameTask(Client c1, Client c2, Deck d1, Deck d2, Consumer<GameRecord> onEnd) {
		data[0].client = c1;
		data[0].deck = d1;
		data[1].client = c2;
		data[1].deck = d2;
		data[0].user = c1.loggedInUser();
		data[1].user = c2.loggedInUser();
		data[0].client.setListener("chatMessage", this::handleMessage);
		data[1].client.setListener("chatMessage", this::handleMessage);
		this.onEnd = onEnd;
		start();
	}

	private Response handleMessage(Request req) {
		Message msg = Message.fromString(req.getBody().get("msg").getAsString());
		msg.setId(Random.nextId());
		addCommand(() -> {
			for (int i = 0; i < 2; i++)
				if (data[i].client != null)
					data[i].client.send(new Request("chatMessage", MGson.makeJsonObject("msg", msg.toString())));
			msgs.add(msg);
		});
		return req.response(Response.OK_NO_CONTENT);
	}

	private Response handlePublicMessage(Request req) {
		Message msg = Message.fromString(req.getBody().get("msg").getAsString());
		msg.setId(Random.nextId());
		addCommand(() -> {
			for (Client client : liveClients)
				client.send(new Request("chatMessage", MGson.makeJsonObject("msg", msg.toString())));
			publicMsgs.add(msg);
		});
		return req.response(Response.OK_NO_CONTENT);
	}

	private void start() {
		addCommand(() -> {
			seed = Random.nextPosLong();
			JsonObject startBody = MGson.makeJsonObject(
					"seed", seed,
					"user1", MGson.toJsonElement(data[0].user.publicInfo()),
					"user2", MGson.toJsonElement(data[1].user.publicInfo()),
					"deck1", data[0].deck.toJsonString(),
					"deck2", data[1].deck.toJsonString()
			);

			data[0].client.send(new Request("start", startBody));
			data[1].client.send(new Request("start", startBody));

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
		if (data[0].client != null) data[0].client.setListener("command", null);
		if (data[1].client != null) data[1].client.setListener("command", null);
		// TODO: remove listeners for chat , and listeners of live clients
	}

	private Response handleCommand(Request req) {
		Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
		sendCommand(cmd);
		return req.response(Response.OK_NO_CONTENT);
	}

	public boolean isDone() {
		return done;
	}

	public void sendCommandAsync(Command cmd) {
		cmds.add(cmd);
		JsonObject commandBody = MGson.makeJsonObject("cmd", cmd.toBase64(), "player", cmd.player());
		if (data[0].client != null) data[0].client.send(new Request("command", commandBody));
		if (data[1].client != null) data[1].client.send(new Request("command", commandBody));
		gameController.sendCommand(cmd);
	}
	public void sendCommand(Command cmd) {
		addCommand(() -> sendCommandAsync(cmd));
	}

	private Request continueRequest() {
		return new Request("continueGame", MGson.makeJsonObject(
				"seed", seed,
				"user1", MGson.toJsonElement(data[0].user.publicInfo()),
				"user2", MGson.toJsonElement(data[1].user.publicInfo()),
				"deck1", data[0].deck.toJsonString(),
				"deck2", data[1].deck.toJsonString(),
				"cmds", cmds.stream().map(c -> c.toBase64()).collect(Collectors.toList()),
				"msgs", msgs.stream().map(m -> m.toString()).collect(Collectors.toList())
		));
	}

	private Request liveRequest() {
		return new Request("liveGame", MGson.makeJsonObject(
				"seed", seed,
				"user1", MGson.toJsonElement(data[0].user.publicInfo()),
				"user2", MGson.toJsonElement(data[1].user.publicInfo()),
				"deck1", data[0].deck.toJsonString(),
				"deck2", data[1].deck.toJsonString(),
				"cmds", cmds.stream().map(c -> c.toBase64()).collect(Collectors.toList()),
				"msgs", publicMsgs.stream().map(m -> m.toString()).collect(Collectors.toList())
		));
	}

	// TODO: use this in requests.java for attending a live-watching
	// TODO: redirecting commands for live-watching clients
	public void addLiveClient(Client client) {
		addCommand(() -> {
			client.send(liveRequest());
			liveClients.add(client);
		});
	}

	protected void iterate() {
		if (done)
			return;

		for (int i = 0; i < 2; i++) {
			Data d = data[i];
			if (d.client != null && d.client.isDone()) {
				d.client = null;
				d.disTime = System.currentTimeMillis();
			}
			if (d.client == null) {
				Client c = Client.clientOfUser(d.user);
				if (c != null) {
					d.client = c;
					c.send(continueRequest());
				} else if (System.currentTimeMillis() - d.disTime >= 60_000) {
					sendCommandAsync(new Command.Resign(i, "disconnected"));
					return;
				}
			}
		}

		// Remove all disconnected live-watcher clients
		for (int i = 0; i < liveClients.size(); i++) {
			if (liveClients.get(i).isDone())
				liveClients.remove(i--);
		}
	}
}
