package org.apgrp10.gwent.server;

import javafx.util.Pair;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;
import org.apgrp10.gwent.utils.MGson;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GamesManager {
	private static final ArrayList<CurrentGame> games = new ArrayList<>();
	private static final Map<Long, Pair<Client, Deck>> waitingForPlaying = new ConcurrentHashMap<>();
	private static final SavedGames database;

	static {
		try {
			database = new SavedGames();
		} catch (Exception e) {
			ANSI.log("Error creating SavedGames database: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private GamesManager() {}

	/**
	 * @param client:   the client who wants to play
	 * @param deck:     the deck that the client wants to play with
	 * @param target:   the target client that the client wants to play with (-1 for random play)
	 * @param isPublic: whether the game should be public or not
	 * @return errorCode:
	 * -OK (200): OK
	 * -BAD_REQUEST (400): Client is already in a game
	 * -CONFLICT (409): The target is already requested, or in a game
	 * -NOT_FOUND (404): The target is offline (or non-existing)
	 */
	public static synchronized int requestPlay(Client client, Deck deck, long target, boolean isPublic) {
		// First, check if client is not already in a game
		if (games.stream().anyMatch(g -> g.p1() == client.loggedInUser().id() || g.p2() == client.loggedInUser().id()))
			return Response.BAD_REQUEST;

		var waiting = waitingForPlaying.get(target);  // target -1 means Random Play

		// if anyone is waiting for that place
		if (waiting != null && !waiting.getKey().isDone()) {
			// if we can join (the place belongs us , or public random place)
			if (target == client.loggedInUser().id() || target == -1) {
				// client can be joined with waiting client
				GameTask newGame = new GameTask(waiting.getKey(), client, waiting.getValue(), deck, gameRecord -> {
					games.removeIf(g -> g.p1() == gameRecord.player1ID() && g.p2() == gameRecord.player2ID());
					try {
						database.addGame(gameRecord, isPublic);
					} catch (Exception e) {
						ANSI.log("Error saving game to database: " + e.getMessage());
					}
				});
				games.add(new CurrentGame(waiting.getKey().loggedInUser().id(), client.loggedInUser().id(), isPublic, newGame));
				TaskManager.submit(newGame);
				// remove the waiting client
				waitingForPlaying.remove(target);
			} else { // we cannot join ( a user already requested the target for playing)
				return Response.CONFLICT;
			}
		}
		// Nobody is waiting for that place
		else {
			if (target != client.loggedInUser().id() && UserManager.isUserOnline(target)) {
				// check the target if is already in a game
				if (games.stream().anyMatch(g -> g.p1() == target || g.p2() == target))
					return Response.CONFLICT;
				Client.clientOfUser(target).send(new Request("requestPlay", MGson.makeJsonObject("from", client.loggedInUser().id(), "isPublic", isPublic)));
			} else if (target != -1)
				return Response.NOT_FOUND;  // requesting to an offline (or non-existing) User!
			waitingForPlaying.put(target, new Pair<>(client, deck));
		}
		return Response.OK;
	}

	public static synchronized void cancelPlayRequest(Client client) {
		waitingForPlaying.entrySet().removeIf(e -> e.getValue().getKey() == client);
	}

	public static void declinePlayRequest(Client client) {
		var waiter = waitingForPlaying.remove(client.loggedInUser().id());
		if(waiter != null && waiter.getKey() != null && !waiter.getKey().isDone())
			waiter.getKey().send(new Request("declinePlayRequest"));
	}

	public record CurrentGame(long p1, long p2, boolean isPublic, GameTask game) {}

	private static class SavedGames extends DatabaseTable {

		private SavedGames() throws Exception {
			super(ServerMain.SERVER_FOLDER + "games.db", "games", System::currentTimeMillis, GameDBColumn.values());
			if (database != null)
				throw new IllegalStateException("UserDatabase already exists");
		}

		private synchronized void addGame(GameRecord gameRecord, boolean isPublic) throws Exception {
			insert(
					Map.entry(GameDBColumn.isPublic, isPublic),
					Map.entry(GameDBColumn.player1, gameRecord.player1ID()),
					Map.entry(GameDBColumn.player2, gameRecord.player2ID()),
					Map.entry(GameDBColumn.seed, gameRecord.seed()),
					Map.entry(GameDBColumn.deck1, gameRecord.deck1()),
					Map.entry(GameDBColumn.deck2, gameRecord.deck2()),
					Map.entry(GameDBColumn.commands, listToString(gameRecord.commands(), Command::toBase64)),
					Map.entry(GameDBColumn.gameWinner, gameRecord.gameWinner()),
					Map.entry(GameDBColumn.roundWinner, listToString(gameRecord.roundWinner(), String::valueOf)),
					Map.entry(GameDBColumn.p1Sc, listToString(gameRecord.p1Sc(), String::valueOf)),
					Map.entry(GameDBColumn.p2Sc, listToString(gameRecord.p2Sc(), String::valueOf))
			);
		}

		private synchronized GameRecord getGameById(long id) throws Exception {
			if (!isIdTaken(id))
				throw new IllegalArgumentException("Game with id " + id + " does not exist");
			return new GameRecord(getValue(id, GameDBColumn.player1),
					getValue(id, GameDBColumn.player2),
					getValue(id, GameDBColumn.seed),
					Deck.fromBase64(getValue(id, GameDBColumn.deck1)).toJsonString(),
					Deck.fromBase64(getValue(id, GameDBColumn.deck2)).toJsonString(),
					stringToList(getValue(id, GameDBColumn.commands), Command::fromBase64),
					getValue(id, GameDBColumn.gameWinner),
					stringToList(getValue(id, GameDBColumn.roundWinner), Integer::parseInt),
					stringToList(getValue(id, GameDBColumn.p1Sc), Integer::parseInt),
					stringToList(getValue(id, GameDBColumn.p2Sc), Integer::parseInt));
		}

		private synchronized List<GameRecord> allGamesByPlayer(long playerId) {
//			return getAllIds().stream().filter(id -> {
//				try {
//					return getValue(id, GameDBColumn.player1).equals(playerId) || getValue(id, GameDBColumn.player2).equals(playerId);
//				} catch (Exception e) {
//					return false;
//				}
//			}).map(id -> {
//				try {
//					return getGameById(id);
//				} catch (Exception e) {
//					return null;
//				}
//			}).collect(Collectors.toList());
			return null;
		}

		private synchronized List<GameRecord> getLastGames(int n) {
			// if n is -1, return all games
			// because id is incremented, the last n games are the last n ids
			return getAllIds().stream().sorted().limit(n).map(id -> {
				try {
					return getGameById(id);
				} catch (Exception e) {
					return null;
				}
			}).collect(Collectors.toList());
		}

		private enum GameDBColumn implements DBColumn {
			isPublic("BIT"),
			player1("BIGINT"),
			player2("BIGINT"),
			seed("BIGINT"),
			deck1("TEXT"),
			deck2("TEXT"),
			commands("TEXT"),
			gameWinner("INTEGER"),
			roundWinner("TEXT"),
			p1Sc("TEXT"),
			p2Sc("TEXT");

			private final String type;

			GameDBColumn(String type) {
				this.type = type;
			}

			@Override
			public String type() {
				return type;
			}
		}
	}
}
