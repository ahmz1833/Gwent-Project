package org.apgrp10.gwent.model;

public class User
{
	private final String username;
	private String password;
	private String nickname;
	private String email;
	private int highestScore;

	public User(String username, String password, String nickname, String email)
	{
		this.username = username;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
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