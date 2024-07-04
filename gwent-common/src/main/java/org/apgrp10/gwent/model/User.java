package org.apgrp10.gwent.model;

import org.apgrp10.gwent.utils.SecurityUtils;

public class User {
	private final long id;
	private RegisterInfo registerInfo;
	private long[] friends;

	public User(long id, RegisterInfo registerInfo) {
		this.id = id;
		this.registerInfo = registerInfo;
	}

	public static String hashSecurityQ(String secQuestion, String secAnswer) {
		String text = secQuestion + " " + secAnswer;
		return SecurityUtils.sha256Hash(text);
	}

	public static String hashPassword(String password) {
		return SecurityUtils.sha256Hash(password);
	}

	public void update(RegisterInfo registerInfo) {
		this.registerInfo = registerInfo;
	}

	public long[] getFriends() {
		return friends;
	}

	public void setFriends(long[] friends) {
		this.friends = friends;
	}

	public long getId() {
		return id;
	}

	public boolean isPasswordCorrect(String password) {
		return hashPassword(password).equals(registerInfo.passwordHash);
	}

	public boolean isSecurityQCorrect(String secQuestion, String secAnswer) {
		return hashSecurityQ(secQuestion, secAnswer).equals(registerInfo.securityQ);
	}

	public PublicInfo publicInfo() {
		return registerInfo.publicInfo();
	}

	public RegisterInfo registerInfo() {
		return registerInfo;
	}

	public record PublicInfo(String username, String nickname, Avatar avatar) {}

	public record RegisterInfo(PublicInfo publicInfo, String passwordHash, String email, String securityQ) {}
}
