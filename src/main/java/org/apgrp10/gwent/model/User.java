package org.apgrp10.gwent.model;

import java.util.ArrayList;

public class User {
	private static User currentUser = null;
	private String username, password, nickname, email;
	private int highestScore;

	public User(String username, String password, String nickname, String email) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(User user) {
		currentUser = user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
