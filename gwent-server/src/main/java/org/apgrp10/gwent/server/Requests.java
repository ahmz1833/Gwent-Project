package org.apgrp10.gwent.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apgrp10.gwent.model.*;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.stream.Collectors;

import static org.apgrp10.gwent.server.Client.AuthLevel.*;
import static org.apgrp10.gwent.server.ServerMain.SECRET_KEY;

public class Requests {
	final static HashMap<Client, Long> resetPassQueue = new HashMap<>();

	private Requests() {}

	/**
	 * Handles the 'jwt' request. Verifies the JWT and returns the user object if valid.
	 *
	 * @jsonParam jwt (String) : The JWT to verify
	 * @statusCode 202 - Accepted (Valid JWT)
	 * @statusCode 401 - Unauthorized (Invalid JWT)
	 * @Authorizations ALL - All clients can perform this request
	 */
	@Authorizations(ALL)
	public static Response jwt(Client client, Request req) {
		JsonElement jwtElement = req.getBody().get("jwt");
		String jwt = jwtElement != null ? jwtElement.getAsString() : "";
		JsonObject payload = SecurityUtils.verifyJWT(jwt, SECRET_KEY);
		if (payload != null && payload.get("exp").getAsLong() > System.currentTimeMillis()) try {
			// Return user object
			User user = UserManager.getUserById(payload.get("sub").getAsLong());
			if (Client.clientOfUser(user) != null)
				Client.clientOfUser(user).setLoggedInUser(null);
			client.setLoggedInUser(user);
			return req.response(Response.ACCEPTED, (JsonObject) MGson.toJsonElement(user));
		} catch (Exception e) {
			return req.response(Response.UNAUTHORIZED);
		}
		else
			return req.response(Response.UNAUTHORIZED);
	}

	/**
	 * Handles the 'register' request. Sends a verification email to the user.
	 *
	 * @jsonParam The JSON body of request is a User.RegisterInfo object
	 * @statusCode 204 - No Content (Verification email sent)
	 * @statusCode 409 - Conflict (Username taken)
	 * @statusCode 500 - Internal Server Error (Exception catch and send back in ServerMain)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response register(Client client, Request req) throws Exception {
		synchronized (UserManager.class) {
			User.RegisterInfo registerInfo = MGson.fromJson(req.getBody(), User.RegisterInfo.class);
			if (UserManager.isUsernameTaken(registerInfo.username()))
				return req.response(Response.CONFLICT); // Username taken
			Email2FAManager.sendRegMailAndAddToQueue(registerInfo);
			return req.response(Response.OK_NO_CONTENT);
		}
	}

	/**
	 * Handles the 'login' request. Sends a login code to the user.
	 *
	 * @jsonParam username (String) : The username of the user
	 * @jsonParam passHash (String) : The password hash of the user
	 * @statusCode 200 - OK (Login code sent) -> return userId (long) : The user ID
	 * @statusCode 401 - Unauthorized (Incorrect password)
	 * @statusCode 404 - Not Found (Username not found)
	 * @statusCode 500 - Internal Server Error (Exception catch and send back in ServerMain)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response login(Client client, Request req) throws Exception {
		String username = req.getBody().get("username").getAsString();
		String passHash = req.getBody().get("passHash").getAsString();
		if (!UserManager.isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found
		User user = UserManager.getUserByUsername(username);
		if (user.isPassHashCorrect(passHash)) {
			Email2FAManager.sendLoginCodeAndAddToQueue(user.email(), client, user.id());
			return req.response(Response.OK, MGson.makeJsonObject("userId", user.id()));
		} else
			return req.response(Response.UNAUTHORIZED); // Incorrect password
	}

	/**
	 * Handles the 'verifyLogin' request. Verifies the login code and returns a JWT if valid.
	 *
	 * @jsonParam userId (long) : The user ID
	 * @jsonParam code (String) : The login code
	 * @statusCode 200 - OK (JWT returned as 'jwt')
	 * @statusCode 401 - Unauthorized (Invalid code)
	 * @statusCode 500 - Internal Server Error (Exception catch and send back in ServerMain)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response verifyLogin(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		String code = req.getBody().get("code").getAsString();
		if (Email2FAManager.verifyLoginCode(client, code, userId)) {
			JsonObject userJson = MGson.makeJsonObject("sub", userId,
					"exp", System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7); // 1 week
			String jwt = SecurityUtils.makeJWT(userJson, ServerMain.SECRET_KEY);
			return req.response(Response.OK, MGson.makeJsonObject("jwt", jwt));
		} else
			return req.response(Response.UNAUTHORIZED);
	}

	/**
	 * Handles the 'forgetPassword' request. Sends a login code to the user.
	 *
	 * @jsonParam username (String) : The username of the user
	 * @jsonParam email (String) : The email of the user
	 * @jsonParam secQ (String) : The hash of the securityQ/A of the user
	 * @statusCode 200 - OK (Login code sent) -> return userId (long) : The user ID
	 * @statusCode 401 - Unauthorized (Incorrect email or security question)
	 * @statusCode 404 - Not Found (Username not found)
	 * @statusCode 500 - Internal Server Error (Exception catch and send back in ServerMain)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response forgetPassword(Client client, Request req) throws Exception {
		String username = req.getBody().get("username").getAsString(),
				email = req.getBody().get("email").getAsString(),
				secQ = req.getBody().get("secQ").getAsString();

		if (!UserManager.isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found

		User user = UserManager.getUserByUsername(username);
		if (user.email().equals(email) && user.isSecQHashCorrect(secQ)) {
			Email2FAManager.sendLoginCodeAndAddToQueue(user.email(), client, user.id());
			return req.response(Response.OK, MGson.makeJsonObject("userId", user.id()));
		} else
			return req.response(Response.UNAUTHORIZED); // Incorrect email or security question
	}

	/**
	 * Handles the 'verifyForgetPassword' request. Verifies the login code to reset the password.
	 *
	 * @jsonParam userId (long) : The user ID
	 * @jsonParam code (String) : The login code
	 * @statusCode 204 - No Content (Password reset)
	 * @statusCode 401 - Unauthorized (Invalid code)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response verifyForgetPassword(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		String code = req.getBody().get("code").getAsString();
		if (Email2FAManager.verifyLoginCode(client, code, userId)) {
			resetPassQueue.put(client, userId);
			return req.response(Response.OK_NO_CONTENT);
		} else
			return req.response(Response.UNAUTHORIZED);
	}

	/**
	 * Handles the 'resetPassword' request. Resets the password of the user.
	 *
	 * @note only works if the client has previously verified the forget password code
	 * @jsonParam userId (long) : The user ID
	 * @jsonParam newPassHash (String) : The new password hash of the user
	 * @statusCode 204 - No Content (Password reset)
	 * @statusCode 401 - Unauthorized (Invalid client or user ID)
	 * @statusCode 500 - Internal Server Error (Exception catch and send back in ServerMain)
	 * @Authorizations NOT_LOGGED_IN - Only clients that are not logged in can perform this request
	 */
	@Authorizations(NOT_LOGGED_IN)
	public static Response resetPassword(Client client, Request req) throws Exception {
		long userId = req.getBody().get("userId").getAsLong();
		if (!resetPassQueue.containsKey(client) || resetPassQueue.get(client) != userId)
			return req.response(Response.UNAUTHORIZED); // Unauthorized
		String newPassHash = req.getBody().get("newPassHash").getAsString();
		UserManager.updatePassword(userId, newPassHash);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'logout' request. Logs out the user.
	 *
	 * @statusCode 204 - No Content (User logged out)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response logout(Client client, Request req) {
		client.setLoggedInUser(null);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'updateUser' request. Updates the user information.
	 *
	 * @jsonParam info (User.PublicInfo) : The new public information of the user
	 * @statusCode 204 - No Content (User information updated)
	 * @statusCode 401 - Unauthorized (User ID mismatch)
	 * @statusCode 409 - Conflict (Username taken)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response updateUser(Client client, Request req) throws Exception {
		User.PublicInfo updateInfo = MGson.fromJson(req.getBody(), User.PublicInfo.class);
		if (client.loggedInUser().id() != updateInfo.id())
			return req.response(Response.UNAUTHORIZED);
		if (UserManager.isUsernameTaken(updateInfo.username()) &&
		    !updateInfo.username().equals(client.loggedInUser().username()))
			return req.response(Response.CONFLICT); // Username taken
		UserManager.updateUserInfo(updateInfo);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'changeEmailRequest' request. Sends a verification email to change the email of the user.
	 *
	 * @jsonParam userId (long) : The user ID
	 * @jsonParam newEmail (String) : The new email of the user
	 * @statusCode 204 - No Content (Verification email sent)
	 * @statusCode 401 - Unauthorized (User ID mismatch)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response changeEmailRequest(Client client, Request req) throws Exception {
		long userId = req.getBody().get("userId").getAsLong();
		User user = client.loggedInUser();
		if (userId != user.id()) return req.response(Response.UNAUTHORIZED);
		String newMail = req.getBody().get("newEmail").getAsString();
		Email2FAManager.sendRegMailAndAddToQueue(new User.RegisterInfo(user.publicInfo(), null, newMail, null));
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'changePassword' request. Changes the password of the user.
	 *
	 * @jsonParam oldPassHash (String) : The old password hash of the user
	 * @jsonParam newPassHash (String) : The new password hash of the user
	 * @statusCode 204 - No Content (Password changed)
	 * @statusCode 401 - Unauthorized (Incorrect old password)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response changePassword(Client client, Request req) throws Exception {
		String oldPassHash = req.getBody().get("oldPassHash").getAsString();
		String newPassHash = req.getBody().get("newPassHash").getAsString();
		if (client.loggedInUser().isPassHashCorrect(oldPassHash)) {
			UserManager.updatePassword(client.loggedInUser().id(), newPassHash);
			return req.response(Response.OK_NO_CONTENT);
		} else
			return req.response(Response.UNAUTHORIZED);
	}

	/**
	 * Handles the 'getUserInfo' request. Returns the public information of the user.
	 *
	 * @jsonParam userId:long : The user ID || username:String : The username of the user
	 * @statusCode 200 - OK -> body:User.PublicInfo
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getUserInfo(Client client, Request req) {
		JsonElement userIdElement = req.getBody().get("userId");
		JsonElement usernameElement = req.getBody().get("username");
		User user;
		try {
			if (userIdElement != null) {  // if userId has been provided, it has priority
				long userId = userIdElement.getAsLong();
				user = UserManager.getUserById(userId);
			} else if (usernameElement != null) { // if userId is not provided, check for username
				String username = usernameElement.getAsString();
				user = UserManager.getUserByUsername(username);
			} else
				return req.response(Response.BAD_REQUEST);
		} catch (Exception e) {
			return req.response(Response.NOT_FOUND);
		}
		if (user == null) return req.response(Response.NOT_FOUND);
		return req.response(Response.OK, (JsonObject) MGson.toJsonElement(user.publicInfo()));
	}

	/**
	 * Handles the 'deleteAccount' request. Deletes the account of the user.
	 *
	 * @statusCode 204 - No Content (Account deleted)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response deleteAccount(Client client, Request req) throws Exception {
		User user = client.loggedInUser();
		UserManager.removeUser(user.id());
		client.setLoggedInUser(null);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'isUserOnline' request. Returns whether the user is online.
	 *
	 * @jsonParam userId (long) : The user ID
	 * @statusCode 200 - OK -> body:{"online":boolean} : Whether the user is online
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response isUserOnline(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		if (!UserManager.isIdExist(userId))
			return req.response(Response.NOT_FOUND);
		return req.response(Response.OK, MGson.makeJsonObject("online", UserManager.isUserOnline(userId)));
	}

	/**
	 * Handles the 'searchUsername' request. Searches for usernames that match the query.
	 *
	 * @jsonParam query (String) : The query to search for
	 * @jsonParam limit (int) : The maximum number of results to return
	 * @statusCode 200 - OK -> body {"results":[long]} : The list of user IDs
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response searchUsername(Client client, Request req) {
		String query = req.getBody().get("query").getAsString();
		int limit = req.getBody().get("limit").getAsInt();
		List<User> result = UserManager.searchUsername(query, limit);
		List<Long> responseResult = result.stream().map(User::id).collect(Collectors.toList());
		return req.response(Response.OK, MGson.makeJsonObject("results", responseResult));
	}

	/**
	 * Handles the 'getFriendList' request. Returns the list of friends of the user.
	 *
	 * @statusCode 200 - OK -> body:[long] : The list of user IDs
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getFriendList(Client client, Request req) throws Exception {
		List<Long> friendList = UserManager.getUserById(client.loggedInUser().id()).getFriends();
		return req.response(Response.OK, MGson.makeJsonObject("results", friendList));
	}

	/**
	 * Handles the 'addFriendshipRequest' request. Adds a friendship request.
	 *
	 * @jsonParam from (long) : The user ID of the sender
	 * @jsonParam to (long) : The user ID of the receiver
	 * @statusCode 204 - No Content (Friendship request added)
	 * @statusCode 400 - Bad Request (from and to are the same)
	 * @statusCode 401 - Unauthorized (from is not the client)
	 * @statusCode 404 - Not Found (User not found)
	 * @statusCode 500 - Internal Server Error (Exception - in case of existing friendship request)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response addFriendshipRequest(Client client, Request req) throws Exception {
		long from = req.getBody().get("from").getAsLong();
		long to = req.getBody().get("to").getAsLong();
		if (from != client.loggedInUser().id())
			return req.response(Response.UNAUTHORIZED);
		if (from == to)
			return req.response(Response.BAD_REQUEST);
		if (!UserManager.isIdExist(to))
			return req.response(Response.NOT_FOUND);
		UserManager.addFriendshipRequest(from, to);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'getIncomingFriendshipRequests' request. Returns the list of incoming friendship requests.
	 *
	 * @statusCode 200 - OK -> body:{result:[FriendshipRequest] : The list of friendship requests}
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getIncomingFriendshipRequests(Client client, Request req) throws Exception {
		List<FriendshipRequest> incomingRequests = UserManager.getIncomingRequests(client.loggedInUser().id());
		return req.response(Response.OK, MGson.makeJsonObject("results", incomingRequests));
	}

	/**
	 * Handles the 'getOutgoingFriendshipRequests' request. Returns the list of outgoing friendship requests.
	 *
	 * @statusCode 200 - OK -> body:{result:[FriendshipRequest] : The list of friendship requests}
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getOutgoingFriendshipRequests(Client client, Request req) throws Exception {
		List<FriendshipRequest> outgoingRequests = UserManager.getOutgoingRequests(client.loggedInUser().id());
		return req.response(Response.OK, MGson.makeJsonObject("results", outgoingRequests));
	}

	/**
	 * Handles the 'acceptFriendshipRequest' request. Accepts a friendship request.
	 *
	 * @jsonParam from (long) : The user ID of the sender
	 * @statusCode 204 - No Content (Friendship request accepted)
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response acceptFriendshipRequest(Client client, Request req) throws Exception {
		long from = req.getBody().get("from").getAsLong();
		if (!UserManager.isIdExist(from))
			return req.response(Response.NOT_FOUND);
		UserManager.acceptFriendshipRequest(from, client.loggedInUser().id());
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'rejectFriendshipRequest' request. Rejects a friendship request.
	 *
	 * @jsonParam from (long) : The user ID of the sender
	 * @statusCode 204 - No Content (Friendship request rejected)
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response rejectFriendshipRequest(Client client, Request req) throws Exception {
		long from = req.getBody().get("from").getAsLong();
		if (!UserManager.isIdExist(from))
			return req.response(Response.NOT_FOUND);
		UserManager.rejectFriendshipRequest(from, client.loggedInUser().id());
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'removeFriendship' request. Removes a friendship.
	 *
	 * @jsonParam userId (long) : The user ID of the friend
	 * @statusCode 204 - No Content (Friendship removed)
	 * @statusCode 404 - Not Found (User is not a friend)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response removeFriendship(Client client, Request req) throws Exception {
		long id = req.getBody().get("userId").getAsLong();
		if (!UserManager.isIdExist(id) || !UserManager.haveFriendship(client.loggedInUser().id(), id))
			return req.response(Response.NOT_FOUND);
		UserManager.removeFriendship(client.loggedInUser().id(), id);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'getUserExperience' request. Returns the experience of the user.
	 *
	 * @jsonParam userId (long) : The user ID
	 * @statusCode 200 - OK -> body:UserExperience : The experience of the user
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getUserExperience(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		if (!UserManager.isIdExist(userId)) return req.response(Response.NOT_FOUND);
		Map<Long, UserExperience> allExp = GamesManager.getAllExperiences();
		return req.response(Response.OK, (JsonObject) MGson.toJsonElement(allExp.get(userId)));
	}

	/**
	 * Handles the 'getTopUsers' request. Returns the top users.
	 *
	 * @jsonParam count (int) : The number of users to return
	 * @jsonParam sortByMaxScore (boolean) : Whether to sort by max score or wins
	 * @statusCode 200 - OK -> body:{result:[UserExperience] : The list of user experiences}
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getTopUsers(Client client, Request req) {
		int count = req.getBody().get("count").getAsInt();
		boolean sortByMaxScore = req.getBody().get("sortByMaxScore").getAsBoolean(); // false: sort by wins
		List<UserExperience> scoreboard = GamesManager.getTopPlayers(count, sortByMaxScore);
		return req.response(Response.OK, MGson.makeJsonObject("results", scoreboard));
	}

	/**
	 * Handles the 'getCurrentGames' request. Returns the list of current games that the user can see.
	 *
	 * @statusCode 200 - OK -> body {"results":[{p1:long, p2:long, isPublic:boolean}]}
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getCurrentGames(Client client, Request req) {
		List<GamesManager.CurrentGame> currentGames = GamesManager.getVisibleCurrentGames(client.loggedInUser().id());
		List<JsonObject> result = currentGames.stream().map(game -> {
			JsonObject gameJson = new JsonObject();
			gameJson.addProperty("p1", game.p1());
			gameJson.addProperty("p2", game.p2());
			gameJson.addProperty("isPublic", game.isPublic());
			return gameJson;
		}).toList();
		return req.response(Response.OK, MGson.makeJsonObject("results", result));
	}

	/**
	 * Handles the 'attendLiveWatching' request. Attends a live watching of a game.
	 *
	 * @jsonParam player (long) : The ID of a player in the game
	 * @statusCode 204 - No Content (Attended) -> Server will send 'live' request soon
	 * @statusCode 404 - Not Found (Game not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response attendLiveWatching(Client client, Request req) {
		long gameId = req.getBody().get("player").getAsLong();
		if (GamesManager.attendLiveWatching(client, gameId))
			return req.response(Response.OK_NO_CONTENT);
		else
			return req.response(Response.NOT_FOUND);
	}

	/**
	 * Handles the 'getMyDoneGameList' request. Returns the list of games that the user has played.
	 *
	 * @statusCode 200 - OK -> body:{long:GameRecord} : The list of game records (not playable - without commands)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getMyDoneGameList(Client client, Request req) {
		HashMap<Long, GameRecord> doneGames = GamesManager.getGamesByPlayer(client.loggedInUser().id());
		// remove Command List in each record (because of its large size)
		for (Long gameId : new ArrayList<>(doneGames.keySet()))
			doneGames.put(gameId, doneGames.get(gameId).withoutCmds());
		return req.response(Response.OK, (JsonObject) MGson.toJsonElement(doneGames));
	}

	/**
	 * Handles the 'getLastGame' request. Returns the last game of the user.
	 *
	 * @statusCode 200 - OK -> body:GameRecord : The last game record
	 * @statusCode 404 - Not Found (No games found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getLastGame(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		// get the last game of the user (greatest key in the map)
		HashMap<Long, GameRecord> games = GamesManager.getGamesByPlayer(userId);
		Optional<Long> greatestId = games.keySet().stream().max(Long::compareTo);
		if (greatestId.isEmpty())
			return req.response(Response.NOT_FOUND);
		GameRecord lastGame = games.get(greatestId.get());
		return req.response(Response.OK, (JsonObject) MGson.toJsonElement(lastGame));
	}

	/**
	 * Handles the 'replayGame' request. Replays a game.
	 *
	 * @jsonParam recordedGameId (long) : The ID of the recorded game
	 * @statusCode 204 - No Content (Game replayed) -> Server will send 'replay' request soon
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response replayGame(Client client, Request req) {
		long recordedGameId = req.getBody().get("recordedGameId").getAsLong();
		GamesManager.replayGame(client, recordedGameId);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'requestPlay' request. Requests to play a game.
	 *
	 * @jsonParam deck (Deck) : The deck to play with
	 * @jsonParam target (long) : The target user ID (-1 for random play)
	 * @jsonParam isPublic (boolean) : Whether the game should be public
	 * @statusCode 204 - No Content (Play request sent) -> When the target accepts, Server will send 'start' request soon
	 * @statusCode 400 - Bad Request (Client is already in a game)
	 * @statusCode 404 - Not Found (The target is offline or non-existing)
	 * @statusCode 409 - Conflict (The target is already requested or in a game)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response requestPlay(Client client, Request req) {
		Deck deck = Deck.fromJson(req.getBody().get("deck"));
		long target = req.getBody().get("target").getAsLong();
		boolean isPublic = req.getBody().get("isPublic").getAsBoolean();
		return req.response(GamesManager.requestPlay(client, deck, target, isPublic));
	}

	/**
	 * Handles the 'cancelPlayRequest' request. Cancels the 'requestPlay' already sent from client
	 *
	 * @statusCode 204 - No Content (Play request cancelled)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response cancelPlayRequest(Client client, Request req) {
		GamesManager.cancelPlayRequest(client);
		return req.response(Response.OK_NO_CONTENT);
	}

	/**
	 * Handles the 'declinePlayRequest' request. Declines the 'requestPlay' from another client
	 * (It sends a 'declinePlayRequest' to the waiter client)
	 *
	 * @statusCode 204 - No Content (Play request declined)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response declinePlayRequest(Client client, Request req) {
		GamesManager.declinePlayRequest(client);
		return req.response(Response.OK_NO_CONTENT);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Authorizations {
		Client.AuthLevel value();
	}
}
