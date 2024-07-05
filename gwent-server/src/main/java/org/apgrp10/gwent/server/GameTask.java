package org.apgrp10.gwent.server;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
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
		c1.send(new Request("makeDeck"));
		c2.send(new Request("makeDeck"));
		c1.setListener("deck", req -> {
			c1.setListener("deck", null);
			addCommand(() -> {
				d1 = Deck.fromJsonString(req.getBody().get("deck").getAsString());
				if (d1 != null && d2 != null) {start();}
			});
			return req.response(Response.OK_NO_CONTENT);
		});
		c2.setListener("deck", req -> {
			c2.setListener("deck", null);
			addCommand(() -> {
				d2 = Deck.fromJsonString(req.getBody().get("deck").getAsString());
				if (d1 != null && d2 != null)
					start();
			});
			return req.response(Response.OK_NO_CONTENT);
		});
	}

	private void start() {
		addCommand(() -> {
			long seed = Random.nextPosLong();
			JsonObject startBody = MGson.makeJsonObject("seed", seed,
					"deck1", d1.toJsonString(),
					"deck2", d2.toJsonString());

			c1.send(new Request("start", startBody));
			c2.send(new Request("start", startBody));

			gameController = new GameController(
				new DummyInputController(),
				new DummyInputController(),
				d1,
				d2,
				seed,
				null,
				gr -> {
					done = true;
					c1.setListener("command", null);
					c2.setListener("command", null);
					ANSI.log("game record: " + gr);
				}
			);

			c1.setListener("command", this::handleCommand);
			c2.setListener("command", this::handleCommand);
		});
	}

	private Response handleCommand(Request req) {
		Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
		int player = req.getBody().get("player").getAsInt();
		sendCommand(cmd);
		JsonObject commandBody = MGson.makeJsonObject("cmd", cmd.toBase64(), "player", player);
		c1.send(new Request("command", commandBody));
		c2.send(new Request("command", commandBody));
		return req.response(Response.OK_NO_CONTENT);
	}

	public boolean isDone() {
		return done;
	}

	public void sendCommand(Command cmd) {
		addCommand(() -> gameController.sendCommand(cmd));
	}
}
