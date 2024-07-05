package org.apgrp10.gwent.model;

import org.apgrp10.gwent.utils.SecurityUtils;

public class User {
	private final long id;
	private RegisterInfo registerInfo;
	private long[] friends;

	public User(RegisterInfo registerInfo) {
		this.id = registerInfo.publicInfo().id();
		this.registerInfo = registerInfo;
	}

	public static String hashSecurityQ(String secQuestion, String secAnswer) {
		String text = secQuestion + " " + secAnswer;
		return SecurityUtils.sha256(text);
	}

	public static String hashPassword(String password) {
		return SecurityUtils.sha256(password);
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
		return isPassHashCorrect(hashPassword(password));
	}

	public boolean isPassHashCorrect(String passHash) {
		return passHash.equals(registerInfo.passwordHash);
	}

	public boolean isSecurityQCorrect(String secQuestion, String secAnswer) {
		return isSecurityQCorrect(hashSecurityQ(secQuestion, secAnswer));
	}

	public boolean isSecurityQCorrect(String hashed) {
		return registerInfo.securityQ.equals(hashed);
	}

	public PublicInfo publicInfo() {
		return registerInfo.publicInfo();
	}

	public RegisterInfo registerInfo() {
		return registerInfo;
	}

	public record PublicInfo(long id, String username, String nickname, Avatar avatar) {}

	public record RegisterInfo(PublicInfo publicInfo, String passwordHash, String email, String securityQ) {
		public String username() {
			return publicInfo.username();
		}

		public String nickname() {
			return publicInfo.nickname();
		}

		public Avatar avatar() {
			return publicInfo.avatar();
		}

		public static RegisterInfo copyWithId(RegisterInfo src, long newId) {
			return new RegisterInfo(new PublicInfo(newId, src.username(), src.nickname(), src.avatar()),
					src.passwordHash(), src.email(), src.securityQ());
		}
	}
}
