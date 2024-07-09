package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.*;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import java.util.function.Consumer;

public class PreGameController {

	private PreGameController(){}

	public static void randomPlayRequest(Deck deck, Consumer<Response> callback) {
		Server.send(new Request("randomPlayRequest", MGson.makeJsonObject("deck", deck.toJsonString())), res -> {
			if (res.isOk()) {
				ANSI.log("Random Play Request Sent");
				Server.setListener("start", PreGameController::startGame);
			} else {
				ANSI.log("Failed to send Random Play Request");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	private static Response startGame(Request request) {
		Server.setListener("start", null);
		long seed = request.getBody().get("seed").getAsLong();
		User.PublicInfo user1 = MGson.fromJson(request.getBody().get("user1"), User.PublicInfo.class);
		User.PublicInfo user2 = MGson.fromJson(request.getBody().get("user2"), User.PublicInfo.class);
		Deck deck1 = Deck.fromJsonString(request.getBody().get("deck1").getAsString());
		Deck deck2 = Deck.fromJsonString(request.getBody().get("deck2").getAsString());
		int localPlayer = (user1.id() == UserController.getCurrentUser().id()) ? 0 : 1;
		GameStage.setCommonData(user1, user2, deck1, deck2, seed);
		GameStage.setOnline(localPlayer);
		GameStage.getInstance().start();
		return request.response(Response.OK_NO_CONTENT);
	}

	public static void cancelRandomPlayRequest() {
		Server.send(new Request("cancelRandomPlayRequest"), res -> {
			if (res.isOk()) {
				ANSI.log("Random Play Request Cancelled");
				Server.setListener("start", null);
			} else {
				ANSI.log("Failed to cancel Random Play Request");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static boolean isWaitingForOpponent() {
		return Server.hasListener("start");
	}

//	public static void startGame(User opponent, Deck deck) {
//		PreGameMenu.getInstance().close();
//		GameController.startGame(opponent, deck);
//	}
}
