package org.apgrp10.gwent.server;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.FriendshipRequest;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.DatabaseTable;
import org.apgrp10.gwent.utils.Random;
import org.apgrp10.gwent.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserManager {
	private static final ArrayList<FriendshipRequest> friendshipRequests = new ArrayList<>();
	private static final UserDatabase database;

	static {
		try {
			database = new UserDatabase();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private UserManager() {}

	public static synchronized User addUser(User.RegisterInfo userInfo) throws Exception {
		return database.addUser(userInfo);
	}

	public static synchronized boolean isUsernameTaken(String username) {
		return database.getUserId(username) != -1;
	}

	public static boolean isUserOnline(long id) { return Client.clientOfUser(id) != null; }

	public static User getUserById(long id) throws Exception {
		return database.getUserById(id);
	}

	public static synchronized User getUserByUsername(String username) throws Exception {
		long id = database.getUserId(username);
		return database.getUserById(id);
	}

	public static synchronized void updateUserInfo(User.PublicInfo updateInfo) throws Exception {
		database.updateUserInfo(updateInfo);
	}

	public static synchronized void updatePassword(long userId, String newPassHash) throws Exception {
		database.updatePassword(userId, newPassHash);
	}

	public static synchronized void updateEmail(long id, String email) throws Exception {
		database.updateEmail(id, email);
	}

	public static List<User> searchUsername(String query, int limit) {
		return Utils.search(database.getAllUsers(), User::username, query, limit);
	}

	public static synchronized void addFriendshipRequest(long from, long to) throws Exception {
		if (UserManager.haveFriendship(from, to))
			throw new IllegalArgumentException("Friendship Exists between Users " + from + " and " + to);
		if (haveFriendshipRequest(from, to) || haveFriendshipRequest(to, from))
			throw new IllegalArgumentException("Friendship Request Exists between Users " + from + " and " + to);
		friendshipRequests.add(FriendshipRequest.of(from, to));
	}

	public static synchronized boolean haveFriendshipRequest(long from, long to) {
		return friendshipRequests.stream().anyMatch(r -> r.from() == from && r.to() == to);
	}

	public static synchronized void acceptFriendshipRequest(long from, long to) throws Exception {
		for (FriendshipRequest request : new ArrayList<>(friendshipRequests)) {
			if (request.from() == from && request.to() == to) {
				friendshipRequests.remove(request);
				friendshipRequests.add(request.accept());
				database.addFriendShip(from, to);
				return;
			}
		}
	}

	public static synchronized void rejectFriendshipRequest(long from, long to) {
		for (FriendshipRequest request : new ArrayList<>(friendshipRequests)) {
			if (request.from() == from && request.to() == to) {
				friendshipRequests.remove(request);
				friendshipRequests.add(request.reject());
				return;
			}
		}
	}

	public static synchronized List<FriendshipRequest> getIncomingRequests(long userId) {
		return friendshipRequests.stream().filter(r -> r.to() == userId).collect(Collectors.toList());
	}

	public static synchronized List<FriendshipRequest> getOutgoingRequests(long userId) {
		return friendshipRequests.stream().filter(r -> r.from() == userId).collect(Collectors.toList());
	}

	public static synchronized boolean haveFriendship(long id1, long id2) throws Exception {
		// if id2 is in list if id1's friends, and vice versa
		return database.getFriendsIds(id1).contains(id2) && database.getFriendsIds(id2).contains(id1);
	}

	public static synchronized void removeFriendship(long id1, long id2) throws Exception {
		database.removeFriendship(id1, id2);
	}

	static class UserDatabase extends DatabaseTable {
		UserDatabase() throws Exception {
			super(ServerMain.SERVER_FOLDER + "gwent.db", "users", Random::nextId, UserDBColumns.values());
			if (database != null)
				throw new IllegalStateException("UserDatabase already exists");
		}

		User addUser(User.RegisterInfo userInfo) throws Exception {
			if (isUsernameTaken(userInfo.username()))
				throw new IllegalArgumentException("Username " + userInfo.username() + " is already taken");
			long id = insert(Map.entry(UserDBColumns.username, userInfo.username()),
					Map.entry(UserDBColumns.nickname, userInfo.nickname()),
					Map.entry(UserDBColumns.email, userInfo.email()),
					Map.entry(UserDBColumns.passHash, userInfo.passwordHash()),
					Map.entry(UserDBColumns.securityQuestion, userInfo.securityQ()),
					Map.entry(UserDBColumns.avatar, userInfo.avatar().toBase64()),
					Map.entry(UserDBColumns.friends, ""));
			return new User(User.RegisterInfo.copyWithId(userInfo, id));
		}

		User getUserById(long id) throws Exception {
			if (!isIdTaken(id))
				throw new IllegalArgumentException("User with id " + id + " does not exist");
			User user = new User(getUserRegisterInfoById(id));
			user.updateFriends(getFriendsIds(id));
			return user;
		}

		User.PublicInfo getUserPublicInfoById(long id) throws Exception {
			if (!isIdTaken(id))
				throw new IllegalArgumentException("User with id " + id + " does not exist");
			return new User.PublicInfo(id,
					getValue(id, UserDBColumns.username),
					getValue(id, UserDBColumns.nickname),
					Avatar.fromBase64(getValue(id, UserDBColumns.avatar)));
		}

		User.RegisterInfo getUserRegisterInfoById(long id) throws Exception {
			if (!isIdTaken(id))
				throw new IllegalArgumentException("User with id " + id + " does not exist");
			return new User.RegisterInfo(
					getUserPublicInfoById(id),
					getValue(id, UserDBColumns.passHash),
					getValue(id, UserDBColumns.email),
					getValue(id, UserDBColumns.securityQuestion));
		}

		long getUserId(String username) {
			return getId("WHERE username = ('" + username + "')");
		}

		void updateUserInfo(User.PublicInfo info) throws Exception {
			updateInfo(info.id(), UserDBColumns.username, info.username());
			updateInfo(info.id(), UserDBColumns.nickname, info.nickname());
			updateInfo(info.id(), UserDBColumns.avatar, info.avatar().toBase64());
		}

		void updateEmail(long id, String newEmail) throws Exception {
			updateInfo(id, UserDBColumns.email, newEmail);
		}

		void updatePassword(long id, String newPassHash) throws Exception {
			updateInfo(id, UserDBColumns.passHash, newPassHash);
		}

		List<Long> getFriendsIds(long id) throws Exception {
			return stringToList(getValue(id, UserDBColumns.friends), Long::parseLong);
		}

		void addFriendShip(long id1, long id2) throws Exception {
			if (haveFriendship(id1, id2))
				return;
			addNewFriend(id1, id2);
			addNewFriend(id2, id1);
		}

		private void addNewFriend(long idOwner, long idFriend) throws Exception {
			List<Long> list = stringToList(getValue(idOwner, UserDBColumns.friends), Long::parseLong);
			list.add(idFriend);
			updateInfo(idOwner, UserDBColumns.friends, listToString(list, String::valueOf));
		}

		void removeFriendship(long id1, long id2) throws Exception {
			removeFriend(id1, id2);
			removeFriend(id2, id1);
		}

		private void removeFriend(long id1, long id2) throws Exception {
			List<Long> list = stringToList(getValue(id1, UserDBColumns.friends), Long::parseLong);
			list.remove(id2);
			updateInfo(id1, UserDBColumns.friends, listToString(list, String::valueOf));
		}

		List<User> getAllUsers() {
			return getAllIds().stream().map(id -> {
				try {
					return getUserById(id);
				} catch (Exception e) {
					return null;
				}
			}).collect(Collectors.toList());
		}

		enum UserDBColumns implements DBColumn {
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
}
