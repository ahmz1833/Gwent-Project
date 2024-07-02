package org.apgrp10.gwent.model;

import org.apgrp10.gwent.utils.SecurityUtils;

public class User {
	private final long id;
	private String username;
	private String passwordHash;
	private String nickname;
	private String email;
	private String securityQ;
	private Avatar avatar;
	private int[] friends;

	public User(long id, String username, String passHash, String nickname, String email, String securityQ, Avatar avatar) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.passwordHash = passHash;
		this.securityQ = securityQ;
		this.nickname = nickname;
		this.avatar = avatar;
	}

	public void setFriends(int[] friends) {
		this.friends = friends;
	}

	public int[] getFriends() {
		return friends;
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public boolean isPasswordCorrect(String password) {
		return passwordHash.equals(SecurityUtils.sha256Hash(password));
	}

	public void setPassword(String password) {
		this.passwordHash = SecurityUtils.sha256Hash(password);
	}

	public String setSecurityQ(String secQuestion, String secAnswer) {
		String text = secQuestion + " " + secAnswer;
		this.securityQ = SecurityUtils.sha256Hash(text);
		return this.securityQ;
	}

	public boolean isSecurityQCorrect(String secQuestion, String secAnswer) {
		String text = secQuestion + " " + secAnswer;
		return this.securityQ.equals(SecurityUtils.sha256Hash(text));
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

	public Avatar getAvatar() {
		return avatar;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}
}
