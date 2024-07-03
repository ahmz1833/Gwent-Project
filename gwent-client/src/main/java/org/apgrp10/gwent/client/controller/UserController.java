package org.apgrp10.gwent.client.controller;


import org.apgrp10.gwent.client.model.StoredList;
import org.apgrp10.gwent.model.User;

import java.util.List;

public class UserController
{
	private static final List<User> allUsers = StoredList.of("users-local.json", User.class);
	
	private static User currentUser;
	
	public static boolean userExists(String username)
	{
		return allUsers.stream().anyMatch(user -> user.getUsername().equals(username));
	}
	
	public static void register(String username, String password, String passwordConfirm)
	{
		if (!password.equals(passwordConfirm))
			throw new IllegalArgumentException("Password confirm does not match with password!");
		if (userExists(username))
			throw new IllegalArgumentException("The entered username already exists!");
//		allUsers.add(new User(username, password));
	}
	
	public static void login(String username, String password)
	{
//		if (!userExists(username))
//			throw new IllegalArgumentException("The entered username does not exist!");
//		if (allUsers.stream().noneMatch(user -> user.getUsername().equals(username) && user.checkPassword(password)))
//			throw new IllegalArgumentException("The entered password is incorrect!");
//		currentUser = allUsers.stream().filter(user -> user.getUsername().equals(username)).findFirst().get();
	}
	
	public static void logout()
	{
//		currentUser = null;
//		LoginMenu.open();
//		MainMenu.close();
	}
	
	public static User getCurrentUser()
	{
		return currentUser;
	}
	
	public static void removeUser(User user)
	{
		allUsers.remove(user);
		if (currentUser.equals(user)) logout();
	}
	
	public static List<User> getAllUsers()
	{
		return List.copyOf(allUsers);
	}
}
