package org.apgrp10.gwent.server.DataBase;

import java.sql.*;
import java.util.ArrayList;

public class UserDataBaseController extends DataBaseController {
	private static UserDataBaseController instance;

	private UserDataBaseController() {
		super("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY," + " username TEXT NOT NULL UNIQUE,nickname TEXT" +
				", email TEXT, password TEXT," + " securityQuestion TEXT, avatar TEXT, friends TEXT)");
	}

	public static UserDataBaseController getInstance() {
		if (instance == null) instance = new UserDataBaseController();
		return instance;
	}

	public void addUser(String username, String nickname, String email, String password, String securityQuestion,
						String avatar) {
		String command = "INSERT INTO users (username, nickname, email, password, securityQuestion, avatar, friends)" +
				" VALUES ('" + username + "' " + ", '" + nickname + "' " + ", '" + email + "' " + ", '" + password + "' " +
				", '" + securityQuestion + "' " + ", '" + avatar + "' " + ", '')";
		executeCommand(command);
	}

	private ResultSet getRowPerson(int id) {
		return getRow("users", "id = " + id);
	}

	private ResultSet getRowPerson(String username) {
		return getRow("users", "username = ('" + username + "')");
	}

	private String getValueOfPerson(int id, String value) {
		return getValue(getRowPerson(id), value);
	}

	private String getValueOfPerson(String username, String value) {
		return getValue(getRowPerson(username), value);
	}

	public String getUserInfo(int id, UserDataBaseColumn column) {
		return getValueOfPerson(id, column.name());
	}

	public int getId(String username) {
		ResultSet result = getRow("users", "username = ('" + username + "')");
		try {
			return result.getInt("id");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUserInfo(String username, UserDataBaseColumn column) {
		return getValueOfPerson(username, column.name());
	}

	public int[] getFriendsIds(int id) {
		String friendsText = getValueOfPerson(id, "friends");
		String[] friendsString = friendsText.split(",");
		int[] friends = new int[friendsString.length];
		for (int i = 0; i < friendsString.length; i++) {
			try {
				friends[i] = Integer.parseInt(friendsString[i].trim());
			} catch (Exception ignored) {
			}
		}
		return friends;
	}

	public String[] getFriendsUsernames(String username) {
		int[] intList = getFriendsIds(getId(username));
		String[] friendsString = new String[intList.length];
		for (int i = 0; i < intList.length; i++) {
			friendsString[i] = getValueOfPerson(intList[i], UserDataBaseColumn.username.name());
		}
		return friendsString;
	}

	public void updateInfo(int id, UserDataBaseColumn column, String newData) {
		updateInfo("users", "id = " + id, newData, column.name());
	}

	public void updateInfo(String username, UserDataBaseColumn column, String newData) {
		updateInfo("users", "username = ('" + username + "')", newData, column.name());

	}

	private void addNewFriend(int idOwner, int idFriend) {
		String newData = getValueOfPerson(idOwner, "friends");
		updateInfo("users", "id = " + idOwner, newData + idFriend + ",", "friends");
	}

	public boolean haveFriendShip(int id1, int id2) {
		for (int id : getFriendsIds(id1))
			if (id == id2)
				return true;
		return false;
	}

	public void addFriendShip(int id1, int id2) {
		if (haveFriendShip(id1, id2))
			return;
		addNewFriend(id1, id2);
		addNewFriend(id2, id1);
	}

	public void deleteFriendShip(int id1, int id2) {
		deleteFriendShipOfOne(id1, id2);
		deleteFriendShipOfOne(id2, id1);
	}

	private void deleteFriendShipOfOne(int id1, int id2) {
		StringBuilder newData = new StringBuilder();
		for (int id : getFriendsIds(id1))
			if (id != id2)
				newData.append(id).append(",");
		updateInfo("users", "id = " + id1, newData.toString(), "friends");

	}

	public String[] getAllUsernames() {
		try {
			ArrayList<String> allUsers = new ArrayList<>();
			ResultSet table = stmt.executeQuery("SELECT * FROM users");
			while (table.next()) {
				allUsers.add(table.getString("username"));
			}
			return allUsers.toArray(new String[0]);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}

