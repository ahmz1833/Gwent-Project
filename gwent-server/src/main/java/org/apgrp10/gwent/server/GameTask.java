package org.apgrp10.gwent.server;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GameTask extends Task {
	private GameController gameController;
	private Client c1, c2;
	private Deck d1, d2;
	private boolean done;

	public GameTask(Client c1, Client c2) {
		this.c1 = c1;
		this.c2 = c2;
		c1.sendRequest(new Request("makeDeck"));
		c2.sendRequest(new Request("makeDeck"));
		c1.setListener("deck", req -> {
			c1.sendResponse(new Response(req.getId(), 200));
			c1.setListener("deck", null);
			addCommand(() -> {
				d1 = Deck.fromJsonString(req.getBody().get("deck").getAsString());
				if (d1 != null && d2 != null)
					start();
			});
		});
		c2.setListener("deck", req -> {
			c2.sendResponse(new Response(req.getId(), 200));
			c2.setListener("deck", null);
			addCommand(() -> {
				d2 = Deck.fromJsonString(req.getBody().get("deck").getAsString());
				if (d1 != null && d2 != null)
					start();
			});
		});
	}

	private void start() {
		addCommand(() -> {
			JsonObject startBody = new JsonObject();
			startBody.add("seed", new JsonPrimitive(Random.nextId()));
			startBody.add("deck1", new JsonPrimitive(d1.toJsonString()));
			startBody.add("deck2", new JsonPrimitive(d2.toJsonString()));

			c1.sendRequest(new Request("start", startBody));
			c2.sendRequest(new Request("start", startBody));

			gameController = new GameController(
				new DummyInputController(),
				new DummyInputController(),
				d1,
				d2,
				Random.nextId(),
				null,
				() -> {
					done = true;
					c1.setListener("command", null);
					c2.setListener("command", null);
				}
			);

			c1.setListener("command", req -> handleCommand(c1, req));
			c2.setListener("command", req -> handleCommand(c2, req));
		});
	}

	private void handleCommand(Client client, Request req) {
		Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
		int player = req.getBody().get("player").getAsInt();
		sendCommand(player, cmd);

		client.sendResponse(new Response(req.getId(), 200));

		JsonObject commandBody = new JsonObject();
		commandBody.add("cmd", new JsonPrimitive(cmd.toBase64()));
		commandBody.add("player", new JsonPrimitive(player));
		c1.sendRequest(new Request("command", commandBody));
		c2.sendRequest(new Request("command", commandBody));
	}

	public boolean isDone() {
		return done;
	}

	public void sendCommand(int player, Command cmd) {
		addCommand(() -> {
			gameController.sendCommand(player, cmd);
		});
	}
}
