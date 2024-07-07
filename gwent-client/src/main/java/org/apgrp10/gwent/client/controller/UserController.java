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
import java.util.function.Consumer;

public class UserController {
	private static final String JWT_FILE_PATH = Gwent.APP_DATA + "jwt.txt";
	private static User currentUser;
	private static long toVerifyUser;
	private static String jwt;

	public static String loadJWTFromFile() {
		// Reads the JWT stored file
		try (FileInputStream fis = new FileInputStream(JWT_FILE_PATH)) {
			byte[] data = new byte[fis.available()];
			fis.read(data);
			String readJwt = new String(data);
			if (jwt == null) jwt = readJwt;
			return readJwt;
		} catch (Exception e) {
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

	public static void logout() {
//		currentUser = null;
//		LoginMenu.open();
//		MainMenu.close();
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void sendLoginRequest(String username, String password, Consumer<Response> callback) {
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
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void completeLogin(String code, boolean saveJWT, Consumer<Response> callback) {
		JsonObject jsonn = MGson.makeJsonObject("userId", toVerifyUser, "code", code);
		Server.send(new Request("verifyLogin", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Verified successfully");
				// Print the JWT received from the server
				jwt = res.getBody().get("jwt").getAsString();
				if (saveJWT) saveJWTToFile();
			} else {
				ANSI.log("Failed to verify, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void authenticate(Consumer<Response> callback) {
		// if we have jwt, send it to server. and put the User (in response) in static variable
		loadJWTFromFile();
		Server.send(new Request("jwt", MGson.makeJsonObject("jwt", jwt)), res -> {
			if (!res.isOk()) {
				ANSI.log("Failed to authenticate, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			else currentUser = MGson.fromJson(res.getBody(), User.class);
			callback.accept(res);
		});
	}

	public static void onDisconnect() {
		currentUser = null;
	}

//	public static void updateUser() {authenticate();}
}
