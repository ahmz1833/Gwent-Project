package org.apgrp10.gwent.server.DataBase;

import java.sql.*;
import java.util.ArrayList;

public class GamesDataBaseController extends DataBaseController {
	private static GamesDataBaseController instance;

	private GamesDataBaseController() {
		super("CREATE TABLE IF NOT EXISTS games (id BIGINT PRIMARY KEY, isPublic BIT,player1 INTEGER, player2 INTEGER" +
				", seed INTEGER, deck1 TEXT, deck2 TEXT, commands TEXT, set1 TEXT, set2 TEXT, set3 TEXT)");
	}

	public static GamesDataBaseController getInstance() {
		if (instance == null) instance = new GamesDataBaseController();
		return instance;
	}

	public void addGame(long id, boolean isPublic, int player1ID, int player2ID, int seed, String deck1, String deck2) {
		String command = "INSERT INTO games (id, isPublic, player1, player2, seed, deck1, deck2, commands, set1, set2, set3)" +
				" VALUES (" + id + "," +
				(isPublic ? 1 : 0) + "," +
				player1ID + "," +
				player2ID + "," +
				seed + "," +
				"'" + deck1 + "' ," +
				"'" + deck2 + "' ," +
				" '', '0-0', '0-0', '0-0')";
		executeCommand(command);
	}

	private ResultSet getRowGame(long id) {
		return getRow("games", "id = " + id);
	}


	private int getIntValueOfGame(long id, String value) {
		try {
			return getRowGame(id).getInt(value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String getStringValueOfGame(long id, String value) {
		return getValue(getRowGame(id), value);
	}

	private boolean getBooleanValueOfGame(long id, String value) {
		try {
			return getRowGame(id).getBoolean(value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private long getLongValueOfGame(long id, String value) {
		try {
			return getRowGame(id).getLong(value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
		return getBooleanValueOfGame(id, GamesDataBaseColumn.isPublic.name());
	}

	public String getCommands(long id) {
		return getStringValueOfGame(id, GamesDataBaseColumn.commands.name());
	}

	public void setCommands(long id, String commands) {
		updateInfo("games", "id = " + id, commands, GamesDataBaseColumn.commands.name());
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
}

enum GamesDataBaseColumn {
	isPublic, player1, player2, seed, deck1, deck2, commands, set1, set2, set3
}
