package org.apgrp10.gwent.client.controller;

import com.google.gson.reflect.TypeToken;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.Dialogs;
import org.apgrp10.gwent.client.view.GameStage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.client.view.MessageStage;
import org.apgrp10.gwent.client.view.PreGameStage;
import org.apgrp10.gwent.model.*;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class PreGameController {
	public static final int NONE = 0, WAITING = 1, DECLINED = 2, CANCELED = 3;   // Accepted is also none
	public static int lastRequestStatus = NONE;

	private PreGameController() {}

	public static void getCurrentGames(Consumer<List<GameInCurrent>> callback) {
		Server.send(new Request("getCurrentGames"), res -> {
			if (res.isOk()) {
				List<GameInCurrent> currentGames = MGson.fromJson(res.getBody().get("results"),
						TypeToken.getParameterized(ArrayList.class, GameInCurrent.class).getType());
				callback.accept(currentGames);
			} else {
				ANSI.log("Failed to get Current Games");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void attendLiveWatching(GameInCurrent game, Consumer<Response> callback) {
		Server.send(new Request("attendLiveWatching", MGson.makeJsonObject("player", game.p1)), res -> {
			if (res.isOk()) {
				ANSI.log("Attending Live Watching");
				Server.setListener("live", PreGameController::startGame);
				lastRequestStatus = WAITING;
			} else {
				lastRequestStatus = NONE;
				ANSI.log("Failed to attend Live Watching");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void getMyDoneGameList(Consumer<HashMap<Long, GameRecord>> callback) {
		Server.send(new Request("getMyDoneGameList"), res -> {
			if (res.isOk()) {
				HashMap<Long, GameRecord> records = MGson.fromJson(res.getBody(),
						TypeToken.getParameterized(HashMap.class, Long.class, GameRecord.class).getType());
				callback.accept(records);
			} else {
				ANSI.log("Failed to get Game Records");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void getLastGame(long userId, Consumer<GameRecord> callback) {
		Server.send(new Request("getLastGame", MGson.makeJsonObject("userId", userId)), res -> {
			if (res.isOk()) {
				GameRecord record = MGson.fromJson(res.getBody(), GameRecord.class);
				callback.accept(record);
			} else if (res.getStatus() == Response.NOT_FOUND)
				callback.accept(null);
			else {
				ANSI.log("Failed to get Last Game");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void replayGame(long recordedGameId, Consumer<Response> callback) {
		Server.send(new Request("replayGame", MGson.makeJsonObject("recordedGameId", recordedGameId)), res -> {
			if (res.isOk()) {
				ANSI.log("Replay Game Requested.");
				Server.setListener("replay", PreGameController::startGame);
				lastRequestStatus = WAITING;
			} else {
				lastRequestStatus = NONE;
				ANSI.log("Failed to replay Game");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static Response handlePlayRequest(Request request) {
		long from = request.getBody().get("from").getAsLong();
		boolean isPublic = request.getBody().get("isPublic").getAsBoolean();
		UserController.getUserInfo(from, false, publicInfo -> {
			ANSI.log("Play Request Received From: " + publicInfo.username());
			if (MainStage.getInstance().isShowing()) {
				boolean result = MainStage.getInstance().showConfirmDialog(Dialogs.INFO(), "Play Request",
						"You have a play request from " + publicInfo.username() + "\n" +
						"The Game Will be " + (isPublic ? "Public" : "Private") + "\n" +
						"Do you want to play?",
						"Select Deck", "Cancel");
				if (result) {
					Gwent.forEachStage(Stage::close);
					PreGameStage.getInstance().setupFriendMode(isPublic);
					PreGameStage.getInstance().start();
					return;
				}
			}
			Server.send(new Request("declinePlayRequest")); // Decline the request
		});
		return request.response(Response.OK_NO_CONTENT);
	}

	public static Response startGame(Request request) {
		lastRequestStatus = NONE;
		Server.setListener(request.getAction(), null);
		long seed = request.getBody().get("seed").getAsLong();
		User.PublicInfo user1 = MGson.fromJson(request.getBody().get("user1"), User.PublicInfo.class);
		User.PublicInfo user2 = MGson.fromJson(request.getBody().get("user2"), User.PublicInfo.class);
		Deck deck1 = Deck.fromJson(request.getBody().get("deck1"));
		Deck deck2 = Deck.fromJson(request.getBody().get("deck2"));
		int localPlayer = (user1.id() == UserController.getCurrentUser().id()) ? 0 : 1;
		ANSI.log("Game Stage Request '%s' Received / User1: %s / User2: %s".formatted(request.getAction(), user1.username(), user2.username()));
		GameStage.setCommonData(user1, user2, deck1, deck2, seed);
		switch (request.getAction()) {
			case "start" -> GameStage.setOnline(localPlayer);
			case "continueGame" -> {
				MessageStage.setInitialMessages(MGson.fromJson(request.getBody().get("msgs"),
						TypeToken.getParameterized(ArrayList.class, String.class).getType()));
				GameStage.setContinue(localPlayer, MGson.fromJson(request.getBody().get("cmds"),
						TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
			}
			case "live" -> {
				MessageStage.setInitialMessages(MGson.fromJson(request.getBody().get("msgs"),
						TypeToken.getParameterized(ArrayList.class, String.class).getType()));
				GameStage.setLive(0, MGson.fromJson(request.getBody().get("cmds"),
						TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
			}
			case "replay" -> GameStage.setReplay(0, MGson.fromJson(request.getBody().get("cmds"),
					TypeToken.getParameterized(ArrayList.class, Command.class).getType()));
			default -> {
				ANSI.log("Unknown Game Start Request : " + request.getAction());
				return request.response(Response.BAD_REQUEST);
			}
		}
		GameStage.getInstance().start();
		return request.response(Response.OK_NO_CONTENT);
	}

	public static void requestPlay(Deck deck, long target, boolean isPublic, Consumer<Response> callback) {
		Server.send(new Request("requestPlay", MGson.makeJsonObject(
				"deck", deck.toJson(), "target", target, "isPublic", isPublic)), res -> {
			if (res.isOk()) {
				ANSI.log("Play Request Sent");
				Server.setListener("start", PreGameController::startGame);
				lastRequestStatus = WAITING;
			} else {
				lastRequestStatus = NONE;
				ANSI.log("Failed to send Play Request");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void cancelPlayRequest() {
		Server.send(new Request("cancelPlayRequest"), res -> {
			if (res.isOk()) {
				ANSI.log("Play Request Cancelled");
				Server.setListener("start", null);
				lastRequestStatus = CANCELED;
			} else {
				lastRequestStatus = NONE;
				ANSI.log("Failed to cancel Play Request");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static Response handlePlayRequestDecline(Request request) {
		ANSI.log("Play Request Declined");
		Server.setListener("start", null);
		lastRequestStatus = DECLINED;
		return request.response(Response.OK_NO_CONTENT);
	}

	public static int getLastRequestState() {
		return lastRequestStatus;
	}

	public record GameInCurrent(long p1, long p2, boolean isPublic) {}
}
