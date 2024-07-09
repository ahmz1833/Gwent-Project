package org.apgrp10.gwent.client.controller;

import com.google.gson.reflect.TypeToken;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.GameStage;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PreGameController {

	private PreGameController() {}

	public static Response startGame(Request request) {
		Server.setListener(request.getAction(), null);
		long seed = request.getBody().get("seed").getAsLong();
		User.PublicInfo user1 = MGson.fromJson(request.getBody().get("user1"), User.PublicInfo.class);
		User.PublicInfo user2 = MGson.fromJson(request.getBody().get("user2"), User.PublicInfo.class);
		Deck deck1 = Deck.fromJson(request.getBody().get("deck1"));
		Deck deck2 = Deck.fromJson(request.getBody().get("deck2"));
		int localPlayer = (user1.id() == UserController.getCurrentUser().id()) ? 0 : 1;
		ANSI.log("Game Start Request Received : " + request);
		GameStage.setCommonData(user1, user2, deck1, deck2, seed);
		switch (request.getAction()) {
			case "start" -> GameStage.setOnline(localPlayer);
			case "continueGame" -> {
				GameStage.setContinue(localPlayer, MGson.fromJson(request.getBody().get("cmds"),
						TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
				// TODO: Handle Messages
			}
			case "live" -> {
				GameStage.setLive(0, MGson.fromJson(request.getBody().get("cmds"),
						TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
				// TODO: Handle Massages
			}
			case "replay" -> GameStage.setReplay(0, MGson.fromJson(request.getBody().get("cmds"),
					TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
			default -> {
				ANSI.log("Unknown Game Start Request : " + request.getAction());
				return request.response(Response.BAD_REQUEST);
			}
		}
		Gwent.forEachStage(Stage::close);
		GameStage.getInstance().start();
		return request.response(Response.OK_NO_CONTENT);
	}

	public static void randomPlayRequest(Deck deck, Consumer<Response> callback) {
		Server.send(new Request("randomPlayRequest", MGson.makeJsonObject("deck", deck.toJson())), res -> {
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
