package org.apgrp10.gwent.model;

import org.apgrp10.gwent.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

public class User {
	private final RegisterInfo registerInfo;
	private final List<Long> friends = new ArrayList<>();

	public User(RegisterInfo registerInfo) {
		this.registerInfo = registerInfo;
	}

	public static String hashSecurityQ(String secQuestion, String secAnswer) {
		String text = secQuestion + " " + secAnswer;
		return SecurityUtils.sha256(text);
	}

	public static String hashPassword(String password) {
		return SecurityUtils.sha256(password);
	}

	public void updateFriends(List<Long> newFriends) {
		friends.clear();
		friends.addAll(newFriends);
	}

	public void getFriends(List<Long> friends) {
		friends.addAll(this.friends);
	}

	public boolean isPassHashCorrect(String passHash) {
		return passHash.equals(registerInfo.passwordHash);
	}

	public boolean isSecQHashCorrect(String secQ) {
		return secQ.equals(registerInfo.securityQ);
	}

	public long id() {return publicInfo().id();}

	public String username() {return publicInfo().username();}

	public String passwordHash() {return registerInfo.passwordHash();}

	public String nickname() {return publicInfo().nickname();}

	public String email() {return registerInfo.email();}

	public Avatar avatar() {return publicInfo().avatar();}

	public PublicInfo publicInfo() {return registerInfo.publicInfo();}

	public RegisterInfo registerInfo() {return registerInfo;}

	public record PublicInfo(long id, String username, String nickname, Avatar avatar) {}

	public record RegisterInfo(PublicInfo publicInfo, String passwordHash, String email, String securityQ) {
		public static RegisterInfo copyWithId(RegisterInfo src, long newId) {
			return new RegisterInfo(new PublicInfo(newId, src.username(), src.nickname(), src.avatar()),
					src.passwordHash(), src.email(), src.securityQ());
		}

		public long id() {return publicInfo.id();}

		public String username() {return publicInfo.username();}

		public String nickname() {return publicInfo.nickname();}

		public Avatar avatar() {return publicInfo.avatar();}
	}
}
