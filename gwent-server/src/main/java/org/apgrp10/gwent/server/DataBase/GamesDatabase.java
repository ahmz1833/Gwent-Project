package org.apgrp10.gwent.server.DataBase;

import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.server.ServerMain;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GamesDatabase extends DatabaseTable {
	private static final String tableName = "games";
	private static GamesDatabase instance;

	private GamesDatabase() throws Exception {
		super(ServerMain.SERVER_FOLDER + "gwent.db", tableName, System::currentTimeMillis, GameDBColumn.values());
	}

	public static GamesDatabase getInstance() {
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

	public GameRecord addGame(boolean isPublic, long player1ID, long player2ID, long seed, Deck deck1, Deck deck2, List<Command> commands,
	                          int set1P1Sc, int set1P2Sc, int set2P1Sc, int set2P2Sc, int set3P1Sc, int set3P2Sc) throws Exception {
		long id = insert(Map.entry(GameDBColumn.isPublic, isPublic),
				Map.entry(GameDBColumn.player1, player1ID),
				Map.entry(GameDBColumn.player2, player2ID),
				Map.entry(GameDBColumn.seed, seed),
				Map.entry(GameDBColumn.deck1, deck1),
				Map.entry(GameDBColumn.deck2, deck2),
				Map.entry(GameDBColumn.commands, ""),
				Map.entry(GameDBColumn.set1, set1P1Sc + "-" + set1P2Sc),
				Map.entry(GameDBColumn.set2, set2P1Sc + "-" + set2P2Sc),
				Map.entry(GameDBColumn.set3, set3P1Sc + "-" + set3P2Sc));
		return new GameRecord(id, isPublic, player1ID, player2ID, seed, deck1, deck2, commands,
				set1P1Sc, set1P2Sc, set2P1Sc, set2P2Sc, set3P1Sc, set3P2Sc);
	}

	public GameRecord getGameById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("Game with id " + id + " does not exist");
		return new GameRecord(id,
				getValue(id, GameDBColumn.isPublic),
				getValue(id, GameDBColumn.player1),
				getValue(id, GameDBColumn.player2),
				getValue(id, GameDBColumn.seed),
				Deck.fromBase64(getValue(id, GameDBColumn.deck1)),
				Deck.fromBase64(getValue(id, GameDBColumn.deck2)),
				Command.fromBase64(getValue(id, GameDBColumn.commands)),
				getValue(),
				getEachSetResult(id, 2)[0], getEachSetResult(id, 2)[1],
				getEachSetResult(id, 3)[0], getEachSetResult(id, 3)[1]);
	}

	public Long[] getAllIds() {
		try {
			ArrayList<Long> allUsers = new ArrayList<>();
			ResultSet table = stmt.executeQuery("SELECT * FROM games");
			while (table.next()) {
				allUsers.add(table.getLong("id"));
			}
			return allUsers.toArray(new Long[0]);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Long[] allGamesByPlayer(int playerId) {

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
