package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.User;

import java.util.ArrayList;

public class UserController {
	private static final ArrayList<User> allUsers = new ArrayList<>();
	
	public static boolean haveUserWithUsername(String username) {
		for (User user : allUsers)
			if (user.getUsername().equals(username))
				return true;
		return false;
	}
	
	public static User getUserByName(String username) {
		for (User user : allUsers)
			if (user.getUsername().equals(username))
				return user;
		return null;
	}
	
}
