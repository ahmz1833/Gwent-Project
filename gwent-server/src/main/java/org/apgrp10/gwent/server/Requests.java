package org.apgrp10.gwent.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
			User user = UserManager.getInstance().getUserById(payload.get("sub").getAsLong());
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
			if (UserManager.getInstance().isUsernameTaken(registerInfo.username()))
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
		if (!UserManager.getInstance().isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found
		User user = UserManager.getInstance().getUserByUsername(username);
		if (user.isPassHashCorrect(passHash)) {
			Email2FAManager.sendLoginCodeAndAddToQueue(user.registerInfo().email(), client, user.getId());
			return req.response(Response.OK, MGson.makeJsonObject("userId", user.getId()));
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

		if (!UserManager.getInstance().isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found

		User user = UserManager.getInstance().getUserByUsername(username);
		if (user.registerInfo().email().equals(email) && user.registerInfo().securityQ().equals(secQ)) {
			Email2FAManager.sendLoginCodeAndAddToQueue(user.registerInfo().email(), client, user.getId());
			return req.response(Response.OK, MGson.makeJsonObject("userId", user.getId()));
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
		UserManager.getInstance().updatePassword(userId, newPassHash);
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
		if (client.loggedInUser().getId() != updateInfo.id())
			return req.response(Response.UNAUTHORIZED);
		if (UserManager.getInstance().isUsernameTaken(updateInfo.username()))
			return req.response(Response.CONFLICT); // Username taken
		UserManager.getInstance().updateUserInfo(updateInfo);
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
		if (userId != user.getId()) return req.response(Response.UNAUTHORIZED);
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
			UserManager.getInstance().updatePassword(client.loggedInUser().getId(), newPassHash);
			return req.response(Response.OK_NO_CONTENT);
		} else
			return req.response(Response.UNAUTHORIZED);
	}

	/**
	 * Handles the 'getUserInfo' request. Returns the public information of the user.
	 *
	 * @jsonParam userId:long : The user ID || username:String : The username of the user
	 * @statusCode 200 - OK -> body:{info:User.PublicInfo, online:boolean, best:GameRecord}
	 * @statusCode 404 - Not Found (User not found)
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response getUserInfo(Client client, Request req) throws Exception {
		JsonElement userIdElement = req.getBody().get("userId");
		JsonElement usernameElement = req.getBody().get("username");
		User user;
		if (userIdElement != null) {  // if userId has been provided, it has priority
			long userId = userIdElement.getAsLong();
			user = UserManager.getInstance().getUserById(userId);
		} else if (usernameElement != null) { // if userId is not provided, check for username
			String username = usernameElement.getAsString();
			user = UserManager.getInstance().getUserByUsername(username);
		} else
			return req.response(Response.BAD_REQUEST);

		if (user == null)
			return req.response(Response.NOT_FOUND);

		JsonObject body = MGson.makeJsonObject(
				"info", user.publicInfo(),
				"online", isUserOnline(user.getId()),
				"best", getBestGameRecord(user.getId()));
		return req.response(Response.OK, body);
	}


	// TODO: implement and move to a better place (UserManager)
	private static boolean isUserOnline(long id) {
		return false;
	}


	// TODO: implement and move to a better place (GamesManager)
	private static GameRecord getBestGameRecord(long id) {
		return null;
	}

	/**
	 * Handles the 'searchUsername' request. Searches for usernames that match the query.
	 *
	 * @jsonParam query (String) : The query to search for
	 * @jsonParam limit (int) : The maximum number of results to return
	 * @statusCode 200 - OK -> body:[User.PublicInfo] : The list of public information of the users
	 * @Authorizations LOGGED_IN - Only clients that are logged in can perform this request
	 */
	@Authorizations(LOGGED_IN)
	public static Response searchUsername(Client client, Request req) {
		String query = req.getBody().get("query").getAsString();
		int limit = req.getBody().get("limit").getAsInt();
		List<User> result = UserManager.getInstance().searchUsername(query, limit);
		List<User.PublicInfo> responseResult = result.stream().map(User::publicInfo).collect(Collectors.toList());
		return req.response(Response.OK, (JsonObject) MGson.toJsonElement(responseResult));
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Authorizations {
		Client.AuthLevel value();
	}
}
