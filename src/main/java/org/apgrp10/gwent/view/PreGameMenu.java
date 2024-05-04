package org.apgrp10.gwent.view;

import java.util.Scanner;
import java.util.regex.Matcher;

public class PreGameMenu extends Menu {

	boolean check(Scanner scanner) {
		String input = scanner.nextLine();
		Matcher matcher;

		// This is just an example
		if ((matcher = getMatcher("show current menu", input)).matches())
			System.out.println("game Menu");
		else if ((matcher = getMatcher("menu exit", input)).matches())
			return false;
		else
			System.out.println("invalid command");
		return true;
	}


	private void createGameWith(Matcher matcher) {

	}

	private void showFactions() {

	}

	private void selectFaction(Matcher matcher) {

	}

	private void showCards() {

	}

	private void showDeck() {

	}

	private void showInfoCurrentUser() {

	}

	private void saveDeckByAddress(Matcher matcher) {

	}

	private void saveDeckByName(Matcher matcher) { //it finds the file and call loadDeckByAddress

	}

	private void loadDeckByAddress(Matcher matcher) {

	}

	private void loadDeckByName(Matcher matcher) { //it finds the file and call loadDeckByAddress

	}

	private void showLeaders() {

	}

	private void selectLeader(Matcher matcher) {

	}

	private void addToDeck(Matcher matcher) {

	}

	private void deleteFromDeck(Matcher matcher) {

	}

	private void changeTurn() {

	}

	private void startGame() {

	}
}
