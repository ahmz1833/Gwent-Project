package org.apgrp10.gwent.client.controller;


import com.google.gson.JsonObject;
import javafx.animation.Animation;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.model.StoredList;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UserController {
	private static final String JWT_FILE_PATH = Gwent.APP_DATA + "jwt.txt";
	private static User currentUser;
	private static long toVerifyUser;
	private static String jwt;

	private static String loadJWTFromFile() {
		// Reads the JWT stored file
		try (FileInputStream fis = new FileInputStream(JWT_FILE_PATH)) {
			byte[] data = new byte[fis.available()];
			fis.read(data);
			String readJwt = new String(data);
			if (jwt == null) jwt = readJwt;
			return readJwt;
		} catch (Exception e) {
			ANSI.log("Failed to load JWT from file: " + e.getMessage());
			return null;
		}
	}

	private static void saveJWTToFile() {
		// Saves the JWT to a file
		try {
			Files.delete(Path.of(JWT_FILE_PATH));
		} catch (IOException ignored) {}
		try (FileOutputStream fos = new FileOutputStream(JWT_FILE_PATH)) {
			fos.write(jwt.getBytes());
		} catch (Exception e) {
			ANSI.log("Failed to save JWT to file: " + e.getMessage());
		}
	}

	public static void register(User.RegisterInfo registerInfo) {

	}

	public static void login(String username, String password) {
//		if (!userExists(username))
//			throw new IllegalArgumentException("The entered username does not exist!");
//		if (allUsers.stream().noneMatch(user -> user.getUsername().equals(username) && user.checkPassword(password)))
//			throw new IllegalArgumentException("The entered password is incorrect!");
//		currentUser = allUsers.stream().filter(user -> user.getUsername().equals(username)).findFirst().get();
	}

	public static void logout() {
//		currentUser = null;
//		LoginMenu.open();
//		MainMenu.close();
	}

	public static User getCurrentUser() {
		return currentUser;
	}

//	public static void removeUser(User user) {
//		allUsers.remove(user);
//		if (currentUser.equals(user)) logout();
//	}
//
//	public static List<User> getAllUsers() {
//		return List.copyOf(allUsers);
//	}

	public static void sendLoginRequest(String username, String password) {
		JsonObject jsonn = MGson.makeJsonObject("username", username, "passHash",
				User.hashPassword(password));
		Server.send(new Request("login", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Code sent successfully");
				// received userID
				toVerifyUser = res.getBody().get("userId").getAsLong();
				ANSI.log("User ID: " + toVerifyUser);
			} else {
				ANSI.log("Failed to login, error code " + res.getStatus());
				if(res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void completeLogin(String code, boolean saveJWT) {
		JsonObject jsonn = MGson.makeJsonObject("userId", toVerifyUser, "code", code);
		Server.send(new Request("verifyLogin", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Verified successfully");
				// Print the JWT received from the server
				jwt = res.getBody().get("jwt").getAsString();
				if(saveJWT) saveJWTToFile();
			} else {
				ANSI.log("Failed to verify, error code " + res.getStatus());
				if(res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static boolean authenticate() {
		// if we have jwt, send it to server. and put the User (in response) in static variable
		loadJWTFromFile();
		Response res = Server.sendAndWait(new Request("jwt", MGson.makeJsonObject("jwt", jwt)));
		if (res == null || !res.isOk()) return false;
		currentUser = MGson.fromJson(res.getBody(), User.class);
		return currentUser != null;
	}

	public static void updateUser() {authenticate();}
}
