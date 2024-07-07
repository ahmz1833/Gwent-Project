package org.apgrp10.gwent.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.server.db.UserDatabase;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import static org.apgrp10.gwent.server.Client.AuthLevel.*;

import static org.apgrp10.gwent.server.ServerMain.SECRET_KEY;

public class Requests {
	private Requests() {}

	@Authorizations(NOT_LOGGED_IN)
	public static Response register(Client client, Request req) {
		synchronized (UserDatabase.class) {
			User.RegisterInfo registerInfo = MGson.fromJson(req.getBody(), User.RegisterInfo.class);
			if (UserDatabase.getInstance().isUsernameTaken(registerInfo.username()))
				return req.response(Response.CONFLICT); // Username taken
			try {
				Email2FAUtils.sendRegMailAndAddToQueue(registerInfo);
				return req.response(Response.OK_NO_CONTENT);
			} catch (Exception e) {
				ANSI.logError(System.err, "Failed to send email", e);
				return ANSI.createErrorResponse(req, "Failed to send mail", e);
			}
		}
	}

	@Authorizations(NOT_LOGGED_IN)
	public static Response login(Client client, Request req) {
		String username = req.getBody().get("username").getAsString();
		String passHash = req.getBody().get("passHash").getAsString();
		if (!UserDatabase.getInstance().isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found
		try {
			User user = UserDatabase.getInstance().getUserByUsername(username);
			if (user.isPassHashCorrect(passHash)) {
				Email2FAUtils.sendLoginCodeAndAddToQueue(user.registerInfo().email(), client, user.getId());
				return req.response(Response.OK, MGson.makeJsonObject("userId", user.getId()));
			} else
				return req.response(Response.UNAUTHORIZED); // Incorrect password
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to login user", e);
			return ANSI.createErrorResponse(req, "Failed to login user", e);
		}
	}

	@Authorizations(NOT_LOGGED_IN)
	public static Response verifyLogin(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		String code = req.getBody().get("code").getAsString();
		if (Email2FAUtils.verifyLoginCode(client, code, userId)) try {
			JsonObject userJson = MGson.makeJsonObject("sub", userId,
					"exp", System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7); // 1 week
			String jwt = SecurityUtils.makeJWT(userJson, ServerMain.SECRET_KEY);
			return req.response(Response.OK, MGson.makeJsonObject("jwt", jwt));
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to verify login code", e);
			return ANSI.createErrorResponse(req, "Failed to verify Code", e);
		}
		else
			return req.response(Response.UNAUTHORIZED);
	}

	@Authorizations(ALL)
	public static Response jwt(Client client, Request req) {
		JsonElement jwtElement = req.getBody().get("jwt");
		String jwt = jwtElement != null ? jwtElement.getAsString() : "";
		JsonObject payload = SecurityUtils.verifyJWT(jwt, SECRET_KEY);
		if (payload != null && payload.get("exp").getAsLong() > System.currentTimeMillis()) try {
			// Return user object
			User user = UserDatabase.getInstance().getUserById(payload.get("sub").getAsLong());
			client.setLoggedInUser(user);
			return req.response(Response.ACCEPTED, (JsonObject) MGson.toJsonElement(user));
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to get user by id", e);
			return ANSI.createErrorResponse(req, "Failed to get user by id", e);
		}
		else
			return req.response(Response.UNAUTHORIZED);
	}

	@Authorizations(NOT_LOGGED_IN)
	public static Response forgetPassword(Client client, Request req) {
		String username = req.getBody().get("username").getAsString(),
				email = req.getBody().get("email").getAsString(),
				secQ = req.getBody().get("secQ").getAsString();

		if (!UserDatabase.getInstance().isUsernameTaken(username))
			return req.response(Response.NOT_FOUND); // Username not found
		try {
			User user = UserDatabase.getInstance().getUserByUsername(username);
			if (user.registerInfo().email().equals(email) && user.registerInfo().securityQ().equals(secQ)) {
				Email2FAUtils.sendLoginCodeAndAddToQueue(user.registerInfo().email(), client, user.getId());
				return req.response(Response.OK, MGson.makeJsonObject("userId", user.getId()));
			} else
				return req.response(Response.UNAUTHORIZED); // Incorrect email or security question
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to forget password", e);
			return ANSI.createErrorResponse(req, "Failed to forget password", e);
		}
	}

	final static HashMap<Client, Long> resetPassQueue = new HashMap<>();

	@Authorizations(NOT_LOGGED_IN)
	public static Response verifyForgetPassword(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		String code = req.getBody().get("code").getAsString();
		if (Email2FAUtils.verifyLoginCode(client, code, userId)) try{
			resetPassQueue.put(client, userId);
			return req.response(Response.OK_NO_CONTENT);
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to verify forget password code", e);
			return ANSI.createErrorResponse(req, "Failed to verify forget password code", e);
		}
		else
			return req.response(Response.UNAUTHORIZED);
	}

	@Authorizations(NOT_LOGGED_IN)
	public static Response resetPassword(Client client, Request req) {
		long userId = req.getBody().get("userId").getAsLong();
		if(!resetPassQueue.containsKey(client) || resetPassQueue.get(client) != userId)
			return req.response(Response.UNAUTHORIZED); // Unauthorized
		String newPassHash = req.getBody().get("newPassHash").getAsString();
		try {
			UserDatabase.getInstance().updatePassword(userId, newPassHash);
			return req.response(Response.OK_NO_CONTENT);
		} catch (Exception e) {
			ANSI.logError(System.err, "Failed to reset password", e);
			return ANSI.createErrorResponse(req, "Failed to reset password", e);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Authorizations {
		Client.AuthLevel value();
	}
}
