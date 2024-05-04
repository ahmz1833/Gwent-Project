package org.apgrp10.gwent.view;

import java.util.Scanner;
import java.util.regex.Matcher;

import org.apgrp10.gwent.model.User;

public class RegisterMenu extends Menu {
	@Override
	boolean check(Scanner scanner) {
		String input = scanner.nextLine();
		Matcher matcher;

		// This is just an example
		if ((matcher = getMatcher("show current menu", input)).matches())
			System.out.println("Register Menu");

		else if ((matcher = getMatcher("menu exit", input)).matches())
			return false;

		else
			System.out.println("invalid command");

		return true;
	}

	void register(String username, String password, String passwordConfig, String nickname, String email) {
	}

	void pickQuestion(User user, String questionNumber, String answer, String answerConfirm) {
	}
}
