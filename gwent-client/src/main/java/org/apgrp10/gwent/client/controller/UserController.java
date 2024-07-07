package org.apgrp10.gwent.client.controller;


import com.google.gson.JsonObject;
import javafx.stage.Window;
import org.apgrp10.gwent.client.ClientMain;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.client.view.LoginStage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.model.Avatar;
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
import java.util.ArrayList;
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

	public static void sendRegisterRequest(String username, String password, String nickname, String email, String secQuestion, String secAnswer, Consumer<Response> callback) {
		User.RegisterInfo registerInfo = new User.RegisterInfo(new User.PublicInfo(0, username, nickname, Avatar.random()),
				User.hashPassword(password), email, User.hashSecurityQ(secQuestion, secAnswer));
		JsonObject jsonn = (JsonObject) MGson.toJsonElement(registerInfo);
		Server.send(new Request("register", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Registered successfully");
			} else {
				ANSI.log("Failed to register, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
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
				performAuthentication(); // Perform authentication (send jwt back to server)
			} else {
				ANSI.log("Failed to verify, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void performAuthentication() {
		if (Server.isConnected()) {
			authenticate(response -> {
				if (response.isOk()) {
					ANSI.log("Authenticated; Username: " + UserController.getCurrentUser().publicInfo().username(), ANSI.LGREEN, false);
					for (Window window : new ArrayList<>(Window.getWindows()))
						if (window instanceof AbstractStage stage)
							stage.connectionEstablished();
					if (MainStage.getInstance().isWaitingForAuth())
						MainStage.getInstance().start();
				} else {
					ANSI.log("Failed to authenticate, Please Login again.", ANSI.LRED, false);
					for (Window window : new ArrayList<>(Window.getWindows()))
						if (window instanceof AbstractStage stage)
							stage.close();
					if (!LoginStage.getInstance().isShowing())
						LoginStage.getInstance().start();
				}
			});
		}
	}

	private static void authenticate(Consumer<Response> callback) {
		// if we have jwt, send it to server. and put the User (in response) in static variable
		loadJWTFromFile();
		Server.send(new Request("jwt", MGson.makeJsonObject("jwt", UserController.jwt)), res -> {
			if (!res.isOk()) {
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			} else currentUser = MGson.fromJson(res.getBody(), User.class);
			callback.accept(res);
		});
	}

	public static void onDisconnect() {
		currentUser = null;
	}

	public static void requestForgetPasswordVerifyCode(String username, String email, String secQ, String secA, Consumer<Response> callback) {
		JsonObject jsonn = MGson.makeJsonObject("username", username, "email", email, "secQ", User.hashSecurityQ(secQ, secA));
		Server.send(new Request("forgetPassword", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Code sent successfully");
				// received userID
				toVerifyUser = res.getBody().get("userId").getAsLong();
				ANSI.log("User ID: " + toVerifyUser);
			} else {
				ANSI.log("Failed to send code, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void forgetPasswordVerify(String code, Consumer<Response> callback) {
		JsonObject jsonn = MGson.makeJsonObject("userId", toVerifyUser, "code", code);
		Server.send(new Request("verifyForgetPassword", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Verified successfully");
			} else {
				ANSI.log("Failed to verify, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void resetPassword(String newPassword, Consumer<Response> callback) {
		JsonObject jsonn = MGson.makeJsonObject("userId", toVerifyUser, "newPassHash", User.hashPassword(newPassword));
		toVerifyUser = 0;
		Server.send(new Request("resetPassword", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Password changed successfully");
			} else {
				ANSI.log("Failed to change password, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void syncUserInformation(User user, Consumer<Response> callback) {
		JsonObject jsonn = (JsonObject) MGson.toJsonElement(user);
		Server.send(new Request("updateUser", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("User information updated successfully");
			} else {
				ANSI.log("Failed to update user information, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			performAuthentication(); // for the changes to take effect to local user
			callback.accept(res);
		});
	}

	public static void changePassword(User user, String newPassword, Consumer<Response> callback) {
		JsonObject jsonn = MGson.makeJsonObject("userId", user.getId(),
				"oldHash", user.registerInfo().passwordHash(),
				"newHash", User.hashPassword(newPassword));
		Server.send(new Request("changePassword", jsonn), res -> {
			if (res.isOk()) {
				ANSI.log("Password changed successfully");
			} else {
				ANSI.log("Failed to change password, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			performAuthentication(); // for the changes to take effect to local user
			callback.accept(res);
		});
	}
//	public static void updateUser() {authenticate();}
}
