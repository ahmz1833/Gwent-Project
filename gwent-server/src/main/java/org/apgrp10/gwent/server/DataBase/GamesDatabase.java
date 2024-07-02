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
				Deck.fromBase64String(getValue(id, GameDBColumn.deck2)),
				Command.parse(getValue(id, GameDBColumn.commands)),
				getEachSetResult(id, 1)[0], getEachSetResult(id, 1)[1],
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
	
	public int[] getPlayersId(long id) {
		int[] list = new int[2];
		list[0] = getIntValueOfGame(id, GamesDataBaseColumn.player1.name());
		list[1] = getIntValueOfGame(id, GamesDataBaseColumn.player2.name());
		return list;
	}
	
	public String[] getDecks(long id) {
		String[] list = new String[2];
		list[0] = getStringValueOfGame(id, GamesDataBaseColumn.deck1.name());
		list[1] = getStringValueOfGame(id, GamesDataBaseColumn.deck2.name());
		return list;
	}
	
	public int getSeed(long id) {
		return getIntValueOfGame(id, GamesDataBaseColumn.seed.name());
	}
	
	public boolean isPublic(long id) {
		return ge
	}
	
	public String getCommands(long id) {
		return getStringValueOfGame(id, GameDBColumn.commands);
	}
	
	public void setCommands(long id, String commands) throws SQLException {
		updateInfo("games", "id = " + id, commands, GameDBColumn.commands);
	}
	
	public int[] getEachSetResult(long id, int setNum) {
		if (setNum > 3 || setNum < 1)
			throw new RuntimeException("set number out of range");
		int[] result = new int[2];
		String text = getStringValueOfGame(id, "set" + setNum);
		result[0] = Integer.parseInt(text.substring(0, text.indexOf("-")));
		result[1] = Integer.parseInt(text.substring(text.indexOf("-") + 1));
		return result;
	}
	
	public void setEachSetResult(long id, int setNum, int player1Score, int player2Score) {
		if (setNum > 3 || setNum < 1)
			throw new RuntimeException("set number out of range");
		updateInfo("games", "id = " + id, player1Score + "-" + player2Score,
				"set" + setNum);
	}
	
	public Long[] allGamesByPlayer(int playerId) {
		try {
			ArrayList<Long> allUsers = new ArrayList<>();
			ResultSet table = stmt.executeQuery("SELECT * FROM games");
			while (table.next()) {
				if (table.getInt("player1") == playerId || table.getInt("player2") == playerId)
					allUsers.add(table.getLong("id"));
			}
			return allUsers.toArray(new Long[0]);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
