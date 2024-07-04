package org.apgrp10.gwent.server.db;

import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.server.ServerMain;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;

import java.util.ArrayList;
import java.util.Arrays;
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
	                          int set1P1Sc, int set1P2Sc, int set2P1Sc, int set2P2Sc, int set3P1Sc, int set3P2Sc) throws Exception {
		long id = insert(Map.entry(GameDBColumn.isPublic, isPublic),
				Map.entry(GameDBColumn.player1, player1ID),
				Map.entry(GameDBColumn.player2, player2ID),
				Map.entry(GameDBColumn.seed, seed),
				Map.entry(GameDBColumn.deck1, deck1),
				Map.entry(GameDBColumn.deck2, deck2),
				Map.entry(GameDBColumn.commands, commands.stream().map(Command::toBase64).collect(Collectors.joining(","))),
				Map.entry(GameDBColumn.set1, set1P1Sc + "-" + set1P2Sc),
				Map.entry(GameDBColumn.set2, set2P1Sc + "-" + set2P2Sc),
				Map.entry(GameDBColumn.set3, set3P1Sc + "-" + set3P2Sc));
		return new GameRecord(id, isPublic, player1ID, player2ID, seed, deck1, deck2, commands,
				set1P1Sc, set1P2Sc, set2P1Sc, set2P2Sc, set3P1Sc, set3P2Sc);
	}

	public synchronized GameRecord getGameById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("Game with id " + id + " does not exist");
		String set1 = getValue(id, GameDBColumn.set1);
		String set2 = getValue(id, GameDBColumn.set2);
		String set3 = getValue(id, GameDBColumn.set3);
		int set1P1Sc = Integer.parseInt(set1.split("-")[0]);
		int set1P2Sc = Integer.parseInt(set1.split("-")[1]);
		int set2P1Sc = Integer.parseInt(set2.split("-")[0]);
		int set2P2Sc = Integer.parseInt(set2.split("-")[1]);
		int set3P1Sc = Integer.parseInt(set3.split("-")[0]);
		int set3P2Sc = Integer.parseInt(set3.split("-")[1]);
		return new GameRecord(id,
				getValue(id, GameDBColumn.isPublic),
				getValue(id, GameDBColumn.player1),
				getValue(id, GameDBColumn.player2),
				getValue(id, GameDBColumn.seed),
				Deck.fromBase64(getValue(id, GameDBColumn.deck1)),
				Deck.fromBase64(getValue(id, GameDBColumn.deck2)),
				Arrays.stream(((String)getValue(id, GameDBColumn.commands)).split(","))
						.map(Command::fromBase64).collect(Collectors.toList()),
				set1P1Sc, set1P2Sc, set2P1Sc, set2P2Sc, set3P1Sc, set3P2Sc);
	}

	public synchronized void updateGame(GameRecord game) throws Exception {
		update(game.id(), Map.entry(GameDBColumn.isPublic, game.isPublic()),
				Map.entry(GameDBColumn.player1, game.player1ID()),
				Map.entry(GameDBColumn.player2, game.player2ID()),
				Map.entry(GameDBColumn.seed, game.seed()),
				Map.entry(GameDBColumn.deck1, game.deck1().toBase64()),
				Map.entry(GameDBColumn.deck2, game.deck2().toBase64()),
				Map.entry(GameDBColumn.commands, game.commands().stream().map(Command::toBase64).collect(Collectors.joining(","))),
				Map.entry(GameDBColumn.set1, game.set1P1Sc() + "-" + game.set1P2Sc()),
				Map.entry(GameDBColumn.set2, game.set2P1Sc() + "-" + game.set2P2Sc()),
				Map.entry(GameDBColumn.set3, game.set3P1Sc() + "-" + game.set3P2Sc()));
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
		set1("TEXT"),
		set2("TEXT"),
		set3("TEXT");

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
