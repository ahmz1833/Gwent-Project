package org.apgrp10.gwent.server.db;

import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.server.ServerMain;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GamesDatabase extends DatabaseTable {
	private static final String tableName = "games";
	private static GamesDatabase instance;

	private GamesDatabase() throws Exception {
		super(ServerMain.SERVER_FOLDER + "gwent.db", tableName, System::currentTimeMillis, GameDBColumn.values());
	}

	public synchronized static GamesDatabase getInstance() {
		if (instance == null) {
			try {
				instance = new GamesDatabase();
			} catch (Exception e) {
				ANSI.logError(System.err, "Failed to create GamesDatabase instance", e);
				return null;
			}
		}
		return instance;
	}

	public synchronized GameRecord addGame(boolean isPublic, long player1ID, long player2ID, long seed, Deck deck1, Deck deck2, List<Command> commands,
	                          int gameWinner, List<Integer> roundWinner, List<Integer> p1Sc, List<Integer> p2Sc) throws Exception {
		insert(
				Map.entry(GameDBColumn.isPublic, isPublic),
				Map.entry(GameDBColumn.player1, player1ID),
				Map.entry(GameDBColumn.player2, player2ID),
				Map.entry(GameDBColumn.seed, seed),
				Map.entry(GameDBColumn.deck1, deck1),
				Map.entry(GameDBColumn.deck2, deck2),
				Map.entry(GameDBColumn.commands, listToString(commands, Command::toBase64)),
				Map.entry(GameDBColumn.gameWinner, gameWinner),
				Map.entry(GameDBColumn.roundWinner, listToString(roundWinner, String::valueOf)),
				Map.entry(GameDBColumn.p1Sc, listToString(p1Sc, String::valueOf)),
				Map.entry(GameDBColumn.p2Sc, listToString(p2Sc, String::valueOf))
		);
		return new GameRecord(player1ID, player2ID, seed, deck1.toJsonString(), deck2.toJsonString(),
				new ArrayList<>(commands), gameWinner, new ArrayList<>(roundWinner),
				new ArrayList<>(p1Sc), new ArrayList<>(p2Sc));
	}

	public synchronized GameRecord getGameById(long id) throws Exception {
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

	public synchronized void updateGame(long id, boolean isPublic, GameRecord game) throws Exception {
		update(id, Map.entry(GameDBColumn.isPublic, isPublic),
				Map.entry(GameDBColumn.player1, game.player1ID()),
				Map.entry(GameDBColumn.player2, game.player2ID()),
				Map.entry(GameDBColumn.seed, game.seed()),
				Map.entry(GameDBColumn.deck1, Deck.fromJsonString(game.deck1()).toBase64()),
				Map.entry(GameDBColumn.deck2, Deck.fromJsonString(game.deck2()).toBase64()),
				Map.entry(GameDBColumn.commands, listToString(game.commands(), Command::toBase64)),
				Map.entry(GameDBColumn.gameWinner, game.gameWinner()),
				Map.entry(GameDBColumn.roundWinner, listToString(game.roundWinner(), String::valueOf)),
				Map.entry(GameDBColumn.p1Sc, listToString(game.p1Sc(), String::valueOf)),
				Map.entry(GameDBColumn.p2Sc, listToString(game.p2Sc(), String::valueOf)));
	}

	public synchronized ArrayList<GameRecord> allGamesByPlayer(long playerId) {
		return (ArrayList<GameRecord>) getAllIds().stream().filter(id -> {
			try {
				return getValue(id, GameDBColumn.player1).equals(playerId) || getValue(id, GameDBColumn.player2).equals(playerId);
			} catch (Exception e) {
				return false;
			}
		}).map(id -> {
			try {
				return getGameById(id);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());
	}

	public synchronized List<GameRecord> getLastGames(int n) {
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

	public enum GameDBColumn implements DBColumn {
		isPublic("BIT"),
		player1("BIGINT"),
		player2("BIGINT"),
		seed("BIGINT"),
		deck1("TEXT"),
		deck2("TEXT"),
		commands("TEXT"),
		gameWinner("INT"),
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
