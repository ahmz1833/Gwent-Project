package org.apgrp10.gwent.client.controller;


import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.client.view.LoginStage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.UserExperience;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class UserController {
	private static final String JWT_FILE_PATH = Gwent.APP_DATA + "jwt.txt";
	private static final HashMap<Long, User.PublicInfo> userInfoCache = new HashMap<>();
	private static final HashMap<Long, Boolean> onlineStatusCache = new HashMap<>();
	private static User currentUser;
	private static long toVerifyUser;
	private static String jwt;

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void onDisconnect() {
		currentUser = null;
	}

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

	public static void performAuthentication(boolean verbose) {
		if (Server.isConnected()) {
			authenticate(response -> {
				if (response.isOk()) {
					if(verbose) ANSI.log("Authenticated; Username: " + UserController.getCurrentUser().username(), ANSI.LGREEN, false);
					Gwent.forEachAbstractStage(AbstractStage::connectionEstablished);
					if (MainStage.getInstance().isWaitingForAuth()) MainStage.getInstance().start();
					Server.setListener("continueGame", PreGameController::startGame); // set listener for continueGame
					Server.setListener("requestPlay", PreGameController::handlePlayRequest); // set listener for playReplay
					Server.setListener("declinePlayRequest", PreGameController::handlePlayRequestDecline); // set listener for declinePlayRequest
				} else {
					if(verbose) ANSI.log("No Acceptable JWT, going to login page", ANSI.LRED, false);
					Gwent.forEachStage(Stage::close);
					if (!LoginStage.getInstance().isShowing()) LoginStage.getInstance().start();
				}
			});
		}
	}

	public static void sendRegisterRequest(String username, String password, String nickname, String email, String secQuestion, String secAnswer, Consumer<Response> callback) {
		User.RegisterInfo registerInfo = new User.RegisterInfo(new User.PublicInfo(0, username, nickname, Avatar.random()),
				User.hashPassword(password), email, User.hashSecurityQ(secQuestion, secAnswer));
		JsonObject json = (JsonObject) MGson.toJsonElement(registerInfo);
		Server.send(new Request("register", json), res -> {
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

	public static void sendLoginRequest(String username, String password, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("username", username, "passHash",
				User.hashPassword(password));
		Server.send(new Request("login", json), res -> {
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

	public static void verifyLogin(String code, boolean saveJWT, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("userId", toVerifyUser, "code", code);
		Server.send(new Request("verifyLogin", json), res -> {
			if (res.isOk()) {
				ANSI.log("Verified successfully");
				// Print the JWT received from the server
				jwt = res.getBody().get("jwt").getAsString();
				if (saveJWT) saveJWTToFile();
				performAuthentication(true); // Perform authentication (send jwt back to server)
			} else {
				ANSI.log("Failed to verify, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void requestForgetPasswordVerifyCode(String username, String email, String secQ, String secA, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("username", username, "email", email, "secQ", User.hashSecurityQ(secQ, secA));
		Server.send(new Request("forgetPassword", json), res -> {
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

	public static void verifyForgetPassword(String code, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("userId", toVerifyUser, "code", code);
		Server.send(new Request("verifyForgetPassword", json), res -> {
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
		JsonObject json = MGson.makeJsonObject("userId", toVerifyUser, "newPassHash", User.hashPassword(newPassword));
		toVerifyUser = 0;
		Server.send(new Request("resetPassword", json), res -> {
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

	public static void logout() {
		Server.send(new Request("logout", new JsonObject()), res -> {
			if (res.isOk()) {
				currentUser = null;
				jwt = null;
				try {
					Files.delete(Path.of(JWT_FILE_PATH));
				} catch (IOException ignored) {}
				Gwent.forEachStage(Stage::close);
				ANSI.log("Logged out successfully, JWT removed.");
				updateLocal();
			} else
				ANSI.log("Failed to logout, error code " + res.getStatus());
		});
	}

	public static void updateUser(User.PublicInfo newInfo, Consumer<Response> callback) {
		if (newInfo.id() != currentUser.id()) ANSI.log("User ID mismatch, cannot update user information");
		JsonObject json = (JsonObject) MGson.toJsonElement(newInfo);
		Server.send(new Request("updateUser", json), res -> {
			if (res.isOk()) {
				ANSI.log("User information updated successfully");
			} else {
				ANSI.log("Failed to update user information, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			updateLocal();
			callback.accept(res);
		});
	}

	public static void changeEmailRequest(String newEmail, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("userId", currentUser.id(), "newEmail", newEmail);
		Server.send(new Request("changeEmailRequest", json), res -> {
			if (res.isOk()) {
				ANSI.log("Email change request sent successfully");
			} else {
				ANSI.log("Failed to send email change request, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void changePassword(User user, String newPassword, Consumer<Response> callback) {
		JsonObject json = MGson.makeJsonObject("userId", user.id(),
				"oldPassHash", user.passwordHash(),
				"newPassHash", User.hashPassword(newPassword));
		Server.send(new Request("changePassword", json), res -> {
			if (res.isOk()) {
				ANSI.log("Password changed successfully");
			} else {
				ANSI.log("Failed to change password, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			updateLocal();
			callback.accept(res);
		});
	}

	public static void getUserInfo(String username, boolean refresh, Consumer<User.PublicInfo> callback) {
		Optional<User.PublicInfo> optInfo = userInfoCache.values().parallelStream()
				.filter(info -> info.username().equals(username)).findFirst();
		if (!refresh && optInfo.isPresent())
			callback.accept(optInfo.get());
		else Server.send(new Request("getUserInfo", MGson.makeJsonObject("username", username)), res -> {
			if(res.isOk()) {
				User.PublicInfo info = MGson.fromJson(res.getBody(), User.PublicInfo.class);
				isUserOnline(info.id(), online -> onlineStatusCache.put(info.id(), online)); // Check User Online status and put in cache
				userInfoCache.put(info.id(), info);
				callback.accept(info);
			}
			else {
				ANSI.log("Failed to get user info, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void getUserInfo(long id, boolean refresh, Consumer<User.PublicInfo> callback, Consumer<Response> onFailure) {
		isUserOnline(id, online -> onlineStatusCache.put(id, online)); // Check User Online status and put in cache
		if (!refresh && userInfoCache.containsKey(id))
			callback.accept(userInfoCache.get(id));
		else{
//			ANSI.log("Getting user info for id " + id);
			Server.send(new Request("getUserInfo", MGson.makeJsonObject("userId", id)), res -> {
				if(res.isOk()) {
					User.PublicInfo info = MGson.fromJson(res.getBody(), User.PublicInfo.class);
					userInfoCache.put(info.id(), info);
					callback.accept(info);
				}
				else {
					onFailure.accept(res);
				}
			});
		}
	}

	public static void getUserInfo(long id, boolean refresh, Consumer<User.PublicInfo> callback) {
		getUserInfo(id, refresh, callback, res -> {
			ANSI.log("Failed to get user info, error code " + res.getStatus());
			if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
				ANSI.printErrorResponse(null, res);
		});
	}

	public static User.PublicInfo getCachedInfo(long userId) {
		return userInfoCache.getOrDefault(userId,
				new User.PublicInfo(0, "Unknown", "Unknown", Avatar.random()));
	}

	public static boolean getCachedOnlineState(long userId) {
		return onlineStatusCache.getOrDefault(userId, false);
	}

	public static void cacheUserInfo(Runnable onFinish, Runnable onFailure, boolean refresh, Long[] ids) {
		if (ids.length == 0) {
			onFinish.run();
			return;
		}

		int[] cnt = {0};
		for (long id : ids) {
			getUserInfo(id, refresh, info -> {
				if (cnt[0] == -1)
					return;
				cnt[0]++;
				if (cnt[0] == ids.length)
					onFinish.run();
			}, res -> {
				if (cnt[0] != -1) {
					cnt[0] = -1;
					onFailure.run();
					onFinish.run();
				}
			});
		}
	}

	public static void cacheUserInfo(Runnable onFinish, boolean refresh, Long[] ids) {
		cacheUserInfo(onFinish, () -> ANSI.log("Failed to cache user infos"), refresh, ids);
	}

	public static void clearUserInfoCache() {
		userInfoCache.clear();
	}

	public static void isUserOnline(long id, Consumer<Boolean> callback) {
		Server.send(new Request("isUserOnline", MGson.makeJsonObject("userId", id)), res -> {
			if(res.isOk())
				callback.accept(res.getBody().get("online").getAsBoolean());
			else {
				ANSI.log("Failed to check user online status, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void searchUsername(String query, int limit, Consumer<ArrayList<Long>> callback) {
		Server.send(new Request("searchUsername", MGson.makeJsonObject("query", query, "limit", limit)), res -> {
			if(res.isOk()) {
				ArrayList<Long> result = MGson.fromJson(res.getBody().get("results"),
						TypeToken.getParameterized(ArrayList.class, Long.class).getType());
				callback.accept(result);
			}
			else {
				ANSI.log("Failed to search for username, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void sendFriendRequest(long id, Consumer<Response> callback) {
		Server.send(new Request("sendFriendRequest", MGson.makeJsonObject("userId", id)), res -> {
			if(res.isOk())
				ANSI.log("Friend request sent successfully");
			else {
				ANSI.log("Failed to send friend request, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void getUserExperience(long userId, Consumer<UserExperience> callback) {
		Server.send(new Request("getUserExperience", MGson.makeJsonObject("userId", userId)), res -> {
			if(res.isOk()) {
				UserExperience experience = MGson.fromJson(res.getBody(), UserExperience.class);
				callback.accept(experience);
			}
			else {
				ANSI.log("Failed to get user experience, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void getTopUsers(int limit, boolean sortByMaxScore, Consumer<List<UserExperience>> callback) {
		Server.send(new Request("getTopUsers", MGson.makeJsonObject("count", limit, "sortByMaxScore", sortByMaxScore)), res -> {
			if (res.isOk()) {
				List<UserExperience> leaderboard = MGson.fromJson(res.getBody().get("results"),
						TypeToken.getParameterized(ArrayList.class, UserExperience.class).getType());
				callback.accept(leaderboard);
			} else {
				ANSI.log("Failed to get Leaderboard");
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
		});
	}

	public static void deleteAccount(Consumer<Response> callback) {
		Server.send(new Request("deleteAccount"), res -> {
			if(res.isOk())
				ANSI.log("Account deleted successfully");
			else {
				ANSI.log("Failed to get user experience, error code " + res.getStatus());
				if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
					ANSI.printErrorResponse(null, res);
			}
			callback.accept(res);
		});
	}

	public static void updateLocal() {
		performAuthentication(false);
	}
}
