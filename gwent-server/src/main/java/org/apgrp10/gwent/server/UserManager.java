package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.User.RegisterInfo;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.DatabaseTable;
import org.apgrp10.gwent.utils.Random;
import org.apgrp10.gwent.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager extends DatabaseTable {
	private static final String tableName = "users";
	private static UserManager instance;

	private UserManager() throws Exception {
		super(ServerMain.SERVER_FOLDER + "gwent.db", tableName, Random::nextId, UserDBColumns.values());
	}

	public synchronized static UserManager getInstance() {
		if (instance == null) {
			try {
				instance = new UserManager();
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
		return new User(RegisterInfo.copyWithId(userInfo, id));
	}

	public synchronized User getUserByUsername(String username) throws Exception {
		long id = getUserId(username);
		return getUserById(id);
	}

	public synchronized User getUserById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("User with id " + id + " does not exist");
		return new User(getUserRegisterInfoById(id));
	}

	public synchronized User.PublicInfo getUserPublicInfoById(long id) throws Exception {
		if (!isIdTaken(id))
			throw new IllegalArgumentException("User with id " + id + " does not exist");
		return new User.PublicInfo(id,
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

	public synchronized void updateUserInfo(User.PublicInfo info) throws Exception {
		updateInfo(info.id(), UserDBColumns.username, info.username());
		updateInfo(info.id(), UserDBColumns.nickname, info.nickname());
		updateInfo(info.id(), UserDBColumns.avatar, info.avatar().toBase64());
	}

	public synchronized void updateEmail(long id, String newEmail) throws Exception {
		updateInfo(id, UserDBColumns.email, newEmail);
	}

	public synchronized void updatePassword(long id, String newPassHash) throws Exception {
		updateInfo(id, UserDBColumns.passHash, newPassHash);
	}

	public synchronized List<Long> getFriendsIds(long id) throws Exception {
		return stringToList(getValue(id, UserDBColumns.friends), Long::parseLong);
	}

	public synchronized List<String> getFriendsUsernames(String username) throws Exception {
		return getFriendsIds(getUserId(username)).stream().map(id -> {
			try {
				return getUserById(id).publicInfo().username();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
	}

	private synchronized void addNewFriend(long idOwner, long idFriend) throws Exception {
		List<Long> list = stringToList(getValue(idOwner, UserDBColumns.friends), Long::parseLong);
		list.add(idFriend);
		updateInfo(idOwner, UserDBColumns.friends, listToString(list, String::valueOf));
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
		List<Long> list = stringToList(getValue(id1, UserDBColumns.friends), Long::parseLong);
		list.remove(id2);
		updateInfo(id1, UserDBColumns.friends, listToString(list, String::valueOf));
	}

	public synchronized List<User> getAllUsers() {
		return getAllIds().stream().map(id -> {
			try {
				return getUserById(id);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());
	}

	public List<User> searchUsername(String query, int limit) {
		return Utils.search(getAllUsers(), user -> user.publicInfo().username(), query, limit);
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

