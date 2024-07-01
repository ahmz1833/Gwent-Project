package org.apgrp10.gwent.model;

public class User
{
	private final long id;
	private String username;
	private String passwordHash;
	private String nickname;
	private String email;

	public User(long id, String username, String passHash, String nickname, String email)
	{
		this.id = id;
		this.username = username;
		this.email = email;
		this.passwordHash = passHash;
		this.nickname = nickname;
	}

	public String getUsername()
	{
		return username;
	}
	
	public isPasswordCorrect(String password)
	{
		return passwordHash.equals();
	}
	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}
}