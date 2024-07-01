package org.apgrp10.gwent.server.DataBase;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.SecurityQuestion;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.server.ServerMain;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class UserDatabase extends DatabaseTable {
	private static final String tableName = "users";
	private static UserDatabase instance;
	
	private UserDatabase() throws Exception {
		super(ServerMain.SERVER_FOLDER + "gwent.db", tableName, () -> null, UserDBColumns.values());
	}
	
	public static UserDatabase getInstance() {
		if (instance == null) {
			try {
				instance = new UserDatabase();
			} catch (Exception e) {
				ANSI.logError(System.err, "Failed to create UserDatabase instance", e);
				return null;
			}
		}
		return instance;
	}
	
	public User addUser(String username, String nickname, String email, String passHash,
	                    SecurityQuestion securityQuestion, Avatar avatar) {
		long id = insert(Map.entry(UserDBColumns.username, username),
				Map.entry(UserDBColumns.nickname, nickname),
				Map.entry(UserDBColumns.email, email),
				Map.entry(UserDBColumns.passHash, passHash),
				Map.entry(UserDBColumns.securityQuestion, securityQuestion.toString()),
				Map.entry(UserDBColumns.avatar, avatar.toBase64String()),
				Map.entry(UserDBColumns.friends, ""));
		return new User(id, username, nickname, email, passHash, securityQuestion, avatar);
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
	
	public String getUserInfo(int id, UserDBColumns column) {
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
	
	public String getUserInfo(String username, UserDBColumns column) {
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
			friendsString[i] = getValueOfPerson(intList[i], UserDBColumns.username.name());
		}
		return friendsString;
	}
	
	public void updateInfo(int id, UserDBColumns column, String newData) {
		updateInfo("users", "id = " + id, newData, column.name());
	}
	
	public void updateInfo(String username, UserDBColumns column, String newData) {
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
	
	public String[] getAllUsernames() throws SQLException {
		ArrayList<String> allUsers = new ArrayList<>();
		ResultSet table = stmt.executeQuery("SELECT * FROM users");
		while (table.next())
			allUsers.add(table.getString("username"));
		return allUsers.toArray(new String[0]);
	}
	
	public enum UserDBColumns implements DBColumn {
		id("INTEGER PRIMARY KEY"),
		username("TEXT NOT NULL UNIQUE"),
		nickname("TEXT"),
		email("TEXT"),
		passHash("TEXT"),
		securityQuestion("TEXT"),
		avatar("TEXT"),
		friends("TEXT");
		
		private final String type;
		
		UserDBColumns(String type) {
			this.type = type;
		}
		
		@Override
		public String type() {
			return type;
		}
	}
}

