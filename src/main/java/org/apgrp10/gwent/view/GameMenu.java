package org.apgrp10.gwent.view;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameMenu extends Menu {
	boolean check(Scanner scanner) {
		String input = scanner.nextLine();
		Matcher matcher;

		// This is just an example
		if ((matcher = getMatcher("show current menu", input)).matches())
			System.out.println("play Menu");
		else if ((matcher = getMatcher("menu exit", input)).matches())
			return false;
		else
			System.out.println("invalid command");
		return true;
	}

	private void vetoCard(Matcher matcher) {

	}

	private void inHandDeck(Matcher matcher) {

	}

	private void remainingCardsToPlay() {

	}

	private void outOfPlayCards() {

	}

	private void cardsInRou(Matcher matcher) {

	}

	private void spellsInPlay() {

	}

	private void placeCard(Matcher matcher) {

	}

	private void showCommander() {

	}

	private void commanderPowerPlay() {

	}

	private void showPlayersInfo() {

	}

	private void showPlayersLives() {

	}

	private void showNumberOfCardsInHand() {

	}

	private void showTurnInfo() {

	}

	private void showTotalScore() {

	}

	private void showTotalScoreOfRow(Matcher matcher) {

	}

	private void passRound() {

	}
}
