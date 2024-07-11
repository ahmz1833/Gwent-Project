package org.apgrp10.gwent.model;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.utils.MGson;

import java.util.List;
import java.util.function.Function;

public record GameRecord(long player1ID,
                         long player2ID,
                         long seed,
                         String deck1,
                         String deck2,
                         List<Command> commands,
                         int gameWinner,
                         List<Integer> roundWinner,
                         List<Integer> p1Sc,
                         List<Integer> p2Sc) {
	public int totalScore(long playerId) {
		return (playerId == player1ID ? p1Sc : p2Sc).stream().mapToInt(Integer::intValue).sum();
	}

	public Deck getDeck1() {
		return Deck.fromJsonString(deck1);
	}

	public Deck getDeck2() {
		return Deck.fromJsonString(deck2);
	}

	public Request createReplayRequest(Function<Long, User.PublicInfo> infoProvider) {
		JsonObject body = MGson.makeJsonObject(
				"seed", seed,
				"user1", infoProvider.apply(player1ID),
				"user2", infoProvider.apply(player2ID),
				"deck1", getDeck1().toJson(),
				"deck2", getDeck2().toJson(),
				"cmds", MGson.toJsonElement(commands, TypeToken.getParameterized(List.class, Command.class).getType())
		);
		return new Request("replay", body);
	}

	public GameRecord withoutCmds() {
		return new GameRecord(player1ID, player2ID, seed, deck1, deck2, List.of(), gameWinner, roundWinner, p1Sc, p2Sc);
	}

	public long gameWinnerId() {
		return gameWinner == 0 ? player1ID : gameWinner == 1 ? player2ID : -1;
	}
}
