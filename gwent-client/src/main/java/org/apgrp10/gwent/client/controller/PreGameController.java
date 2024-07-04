package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.GameMenu;
import org.apgrp10.gwent.client.view.PreGameMenu;
import org.apgrp10.gwent.client.view.PreGameStage;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javafx.application.Platform;

public class PreGameController {
	private final User.PublicInfo user1, user2;
	private Deck deck1 = null;
	private Deck deck2 = null;
	private boolean isLocal1, isLocal2;
	private Long seed = null;

	// TODO: support when !isLocal1 && !isLocal2
	public PreGameController(User.PublicInfo user1, User.PublicInfo user2, boolean isLocal1, boolean isLocal2) {
		this.user1 = user1;
		this.user2 = user2;
		this.isLocal1 = isLocal1;
		this.isLocal2 = isLocal2;
		if (isLocal1)
			new PreGameMenu(this, true, user1);
		else if (isLocal2)
			new PreGameMenu(this, false, user2);

		if (!isLocal2 || !isLocal1) {
			Server.instance().setListener("start", req -> {
				if (!isLocal1) deck1 = Deck.fromJsonString(req.getBody().get("deck1").getAsString());
				if (!isLocal2) deck2 = Deck.fromJsonString(req.getBody().get("deck2").getAsString());
				seed = req.getBody().get("seed").getAsLong();
				Server.instance().sendResponse(new Response(req.getId(), 200));
				if (deck1 != null && deck2 != null)
					startGame();
			});
		}
	}

	private void startGame() {
		deck1.setUser(user1);
		deck2.setUser(user2);
		// TODO: for now we just set a random game up;
		InputController c1, c2;
		c1 = isLocal1? new MouseInputController(): new ServerInputController();
		c2 = isLocal2? new MouseInputController(): new ServerInputController();
		GameMenu gameMenu = new GameMenu(PreGameStage.getInstance());
		GameController controller = new GameController(c1, c2, deck1, deck2, seed, gameMenu, () -> System.exit(1));

		if (!isLocal1 || !isLocal2) {
			Server.instance().setListener("command", req -> {
				Server.instance().sendResponse(new Response(req.getId(), 200));
				Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
				int player = req.getBody().get("player").getAsInt();
				if (player == 0 && !isLocal1) ((ServerInputController)c1).sendCommand(cmd);
				if (player == 1 && !isLocal2) ((ServerInputController)c2).sendCommand(cmd);
			});
			for (int i = 0; i < 2; i++) {
				if (i == 0 && !isLocal1) continue;
				if (i == 1 && !isLocal2) continue;
				int ii = i;
				controller.addCommandListener(ii, cmd -> {
					JsonObject json = new JsonObject();
					json.add("cmd", new JsonPrimitive(cmd.toBase64()));
					json.add("player", new JsonPrimitive(ii));
					Server.instance().sendRequest(new Request("command", json));
				});
			}
		}
	}

	public void setDeck1(Deck deck) {
		deck1 = deck;
		if (isLocal1 && !isLocal2)
			Server.instance().sendRequest(Deck.deckRequest(0, deck));
		if (isLocal2)
			new PreGameMenu(this, false, user2);
	}

	public void setDeck2(Deck deck) {
		deck2 = deck;
		if (isLocal2 && !isLocal1)
			Server.instance().sendRequest(Deck.deckRequest(1, deck));
		if (isLocal1 && isLocal2)
			seed = System.currentTimeMillis();
	}
}
