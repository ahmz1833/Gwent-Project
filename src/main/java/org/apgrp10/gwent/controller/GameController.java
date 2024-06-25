package org.apgrp10.gwent.controller;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.view.GameMenu;

import javafx.stage.Stage;

public class GameController {
	public static class PlayerData {
		public final User user;
		public final Deck deck;
		public final InputController controller;
		public final List<Card> handCards = new ArrayList<>();
		public final List<Card> usedCards = new ArrayList<>();

		public PlayerData(Deck deck, InputController controller) {
			this.user = deck.getUser();
			this.deck = deck;
			this.controller = controller;
		}
	}
	private final Stage stage;
	private final GameMenu gameMenu;
	private final PlayerData playerData[] = new PlayerData[2];
	private int turn = 0;
	private int activePlayer = 0;
	private Card activeCard;

	public GameController(Stage stage, InputController c0, InputController c1, Deck d0, Deck d1) {
		playerData[0] = new PlayerData(d0, c0);
		playerData[1] = new PlayerData(d1, c1);
		this.stage = stage;
		turn = 0;

		// must be last so GameController initialization is complete
		gameMenu = new GameMenu(this, stage);

		// must be after instanciating GameMenu
		c0.start(this, 0);
		c1.start(this, 1);
		c0.beginTurn();
	}

	public GameMenu getGameMenu() { return gameMenu; }

	public PlayerData getPlayer(int player) { return playerData[player]; }

	private void playCard(Command.PlayCard cmd) {
		// TODO
	}

	private void moveToHand(Command.MoveToHand cmd) {
		playerData[cmd.player()].deck.removeCard(cmd.card());
		playerData[cmd.player()].handCards.add(cmd.card());
	}

	public void sendCommand(Command cmd) {
		if (cmd instanceof Command.PlayCard) playCard((Command.PlayCard)cmd);
		if (cmd instanceof Command.MoveToHand) moveToHand((Command.MoveToHand)cmd);
		gameMenu.redraw();
	}

	public void setActivePlayer(int player) { activePlayer = player; }
	public int getActivePlayer() { return activePlayer; }
}
