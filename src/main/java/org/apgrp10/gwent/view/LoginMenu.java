package org.apgrp10.gwent.view;

import java.util.Scanner;
import java.util.regex.Matcher;

import org.apgrp10.gwent.model.User;

public class LoginMenu extends Menu {
	@Override
	boolean check(Scanner scanner) {
		String input = scanner.nextLine();
		Matcher matcher;

		// This is just an example
		if ((matcher = getMatcher("menu enter register menu", input)).matches())
			menuEnterRegister();

		else if ((matcher = getMatcher("show current menu", input)).matches())
			System.out.println("Login Menu");

		else if ((matcher = getMatcher("menu exit", input)).matches())
			return false;

		else
			System.out.println("invalid command");

		return true;
	}

	private void login(String username, String password, boolean stayLogged) {
	}

	private void forgetPassword(String username) {
	}

	private boolean answer(User user, String questionNumber, String answer) {
		return false;
	}

	private void setPassword(User user, String password) {
	}

	private void menuEnterRegister() {
	}
}
