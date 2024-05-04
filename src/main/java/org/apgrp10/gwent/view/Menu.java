package org.apgrp10.gwent.view;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Menu {
	abstract boolean check(Scanner scanner);

	public void run(Scanner scanner) {
	}

	Matcher getMatcher(String pattern, String input) {
		pattern = pattern.replaceAll("\\<", "(?<").replaceAll("(?<! )\\>", ">\\\\S+)").replaceAll(" \\>", ">.+)");
		return Pattern.compile(pattern).matcher(input.trim());
	}
}
