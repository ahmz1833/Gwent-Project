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
	private long[] friends;

	public record UserInfo(String username,
	                String passHash,
	                String nickname,
	                String email,
	                String securityQ,
	                Avatar avatar) {}

	public User(long id, UserInfo userInfo) {
		this.id = id;
		this.username = userInfo.username();
		this.passwordHash = userInfo.passHash();
		this.nickname = userInfo.nickname();
		this.email = userInfo.email();
		this.securityQ = userInfo.securityQ();
		this.avatar = userInfo.avatar();
	}

	public void setFriends(long[] friends) {
		this.friends = friends;
	}

	public long[] getFriends() {
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
