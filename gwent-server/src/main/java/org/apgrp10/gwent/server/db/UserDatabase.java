package org.apgrp10.gwent.server.db;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.server.ServerMain;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;
import org.apgrp10.gwent.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDatabase extends DatabaseTable {
	private static final String tableName = "users";
	private static UserDatabase instance;

	private UserDatabase() throws Exception {
		super(ServerMain.SERVER_FOLDER + "gwent.db", tableName, Random::nextId, UserDBColumns.values());
	}

	public synchronized static UserDatabase getInstance() {
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

	public synchronized User addUser(User.RegisterInfo userInfo) throws Exception {
		if (isUsernameTaken(userInfo.username()))
			throw new IllegalArgumentException("Username " + userInfo.username() + " is already taken");
		long id = insert(Map.entry(UserDBColumns.username, userInfo.username()),
				Map.entry(UserDBColumns.nickname, userInfo.nickname()),
				Map.entry(UserDBColumns.email, userInfo.email()),
				Map.entry(UserDBColumns.passHash, userInfo.passwordHash()),
				Map.entry(UserDBColumns.securityQuestion, userInfo.securityQ()),
				Map.entry(UserDBColumns.avatar, userInfo.avatar().toBase64()),
				Map.entry(UserDBColumns.friends, ""));
		return new User(id, userInfo);
	}

	public synchronized User getUserByUsername(String username) throws Exception {
		long id = getUserId(username);
		return getUserById(id);
	}

	public synchronized User getUserById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("User with id " + id + " does not exist");
		return new User(id, getUserRegisterInfoById(id));
	}

	public synchronized User.PublicInfo getUserPublicInfoById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("User with id " + id + " does not exist");
		return new User.PublicInfo(
				getValue(id, UserDBColumns.username),
				getValue(id, UserDBColumns.nickname),
				Avatar.fromBase64(getValue(id, UserDBColumns.avatar)));
	}

	public synchronized User.RegisterInfo getUserRegisterInfoById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("User with id " + id + " does not exist");
		return new User.RegisterInfo(
				getUserPublicInfoById(id),
				getValue(id, UserDBColumns.passHash),
				getValue(id, UserDBColumns.email),
				getValue(id, UserDBColumns.securityQuestion));
	}

	public synchronized boolean isUsernameTaken(String username) {
		return getUserId(username) != -1;
	}

	public synchronized long getUserId(String username) {
		return getId("WHERE username = ('" + username + "')");
	}

	public synchronized long[] getFriendsIds(long id) throws Exception {
		return Arrays.stream(((String) getValue(id, UserDBColumns.friends)).split(",")) // split by comma
				.map(String::trim).mapToLong(Long::parseLong).toArray();
	}

	public synchronized String[] getFriendsUsernames(String username) throws Exception {
		return (String[]) Arrays.stream(getFriendsIds(getUserId(username))).mapToObj(id -> {
			try {
				return (String) getValue(id, UserDBColumns.username);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).toArray();
	}

	private synchronized void addNewFriend(long idOwner, long idFriend) throws Exception {
		String newData = (String) getValue(idOwner, UserDBColumns.friends) + idFriend + ",";
		updateInfo(idOwner, UserDBColumns.friends, newData);
	}

	public synchronized boolean haveFriendShip(long id1, long id2) throws Exception {
		for (long id : getFriendsIds(id1)) if (id == id2) return true;
		return false;
	}

	public synchronized void addFriendShip(long id1, long id2) throws Exception {
		if (haveFriendShip(id1, id2))
			return;
		addNewFriend(id1, id2);
		addNewFriend(id2, id1);
	}

	public synchronized void deleteFriendShip(long id1, long id2) throws Exception {
		deleteFriendShipOfOne(id1, id2);
		deleteFriendShipOfOne(id2, id1);
	}

	private synchronized void deleteFriendShipOfOne(long id1, long id2) throws Exception {
		StringBuilder newData = new StringBuilder();
		for (long id : getFriendsIds(id1)) if (id != id2) newData.append(id).append(",");
		updateInfo(id1, UserDBColumns.friends, newData.toString());
	}

	public synchronized ArrayList<User> getAllUsers() {
		return (ArrayList<User>) getAllIds().stream().map(id -> {
			try {
				return getUserById(id);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());
	}

	public enum UserDBColumns implements DBColumn {
		username("TEXT"),
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

