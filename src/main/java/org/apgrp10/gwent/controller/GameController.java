package org.apgrp10.gwent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.Ability;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.Faction;
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
	private final List<List<Card>> row = new ArrayList<>();
	private final List<List<Card>> special = new ArrayList<>();
	private int turn = 0;
	private int activePlayer = 0;
	private Card activeCard;
	private Map<Integer, Card> cardIdMap = new HashMap<>();

	private static interface StrengthModifier {
		public int modify(Card card, int currentStrength);
	}

	private List<List<StrengthModifier>> rowModifiers = new ArrayList<>();

	public GameController(Stage stage, InputController c0, InputController c1, Deck d0, Deck d1) {
		playerData[0] = new PlayerData(d0, c0);
		playerData[1] = new PlayerData(d1, c1);
		this.stage = stage;
		turn = 0;

		for (int i = 0; i < 6; i++) {
			row.add(new ArrayList<>());
			special.add(new ArrayList<>());
			rowModifiers.add(new ArrayList<>());
		}

		int id = 0;
		Random rand = new Random(System.currentTimeMillis());
		for (PlayerData p : playerData) {
			Deck d = p.deck;

			// order is important so gameIds are deterministic
			id = d.assignGameIds(id);
			d.shuffle(rand);

			for (Card card : d.getDeck())
				cardIdMap.put(card.getGameId(), card);
		}

		// must be last so GameController initialization is complete
		gameMenu = new GameMenu(this, stage);

		// must be after instanciating GameMenu
		c0.start(this, 0);
		c1.start(this, 1);
		c0.beginTurn();
	}

	public Card cardById(int cardId) {
		return cardIdMap.get(cardId);
	}

	public GameMenu getGameMenu() { return gameMenu; }

	public PlayerData getPlayer(int player) { return playerData[player]; }

	private void playCard(Command.PlayCard cmd) {
		Card card = cardById(cmd.cardId());
		int idx = cmd.row();
		if (idx < 6) { // normal row
			gameMenu.animationToRow(card, idx);
			playerData[cmd.player()].handCards.remove(card);
			row.get(idx).add(card);
		} else if (idx < 12) { // special place
			idx -= 6;
			gameMenu.animationToSpecial(card, idx);
			playerData[cmd.player()].handCards.remove(card);
			special.get(idx).add(card);
		}

		switch (card.ability) {
			case SPY -> {
				List<Card> deck = playerData[cmd.player()].deck.getDeck();
				if (!deck.isEmpty()) moveCardToHand(cmd.player(), deck.get(0));
				if (!deck.isEmpty()) moveCardToHand(cmd.player(), deck.get(0));
			}
			default -> {}
		};
	}

	private void swapCard(Command.SwapCard cmd) {
		Card c1 = cardById(cmd.cardId1());
		Card c2 = cardById(cmd.cardId2());
		gameMenu.animationSwap(c1, c2);

		// there are some guarantees because of canSwap and we rely on them
		int player = cmd.player();
		playerData[player].handCards.set(playerData[player].handCards.indexOf(c1), c2);
		for (int i = (player == 1? 0: 3); i < (player == 1? 3: 6); i++) {
			if (row.get(i).contains(c2)) {
				row.get(i).set(row.get(i).indexOf(c2), c1);
				break;
			}
		}
	}

	private void moveCardToHand(int player, Card card) {
		if (player == activePlayer)
			gameMenu.animationToHand(card);
		playerData[player].deck.removeCard(card);
		playerData[player].handCards.add(card);
	}

	private void moveToHand(Command.MoveToHand cmd) {
		Card card = cardById(cmd.cardId());
		moveCardToHand(cmd.player(), card);
	}

	private void setActiveCard(Command.SetActiveCard cmd) {
		activeCard = cardById(cmd.cardId());
	}

	private void pass(Command.Pass cmd) {
		playerData[turn].controller.endTurn();
		turn = 1 - turn;
		playerData[turn].controller.beginTurn();
	}

	public static interface CommandListener { public void call(Command cmd); }
	private final List<CommandListener> commandListeners = new ArrayList<>();
	public void addCommandListener(CommandListener cb) { commandListeners.add(cb); }

	public void sendCommand(Command cmd) {
		if (cmd instanceof Command.PlayCard) playCard((Command.PlayCard)cmd);
		if (cmd instanceof Command.SwapCard) swapCard((Command.SwapCard)cmd);
		if (cmd instanceof Command.MoveToHand) moveToHand((Command.MoveToHand)cmd);
		if (cmd instanceof Command.SetActiveCard) setActiveCard((Command.SetActiveCard)cmd);
		if (cmd instanceof Command.Pass) pass((Command.Pass)cmd);
		if (cmd instanceof Command.Sync) gameMenu.redraw();
		System.out.println(cmd);

		for (CommandListener cb : commandListeners)
			cb.call(cmd);
	}

	public void setActivePlayer(int player) { activePlayer = player; gameMenu.redraw(); }
	public int getActivePlayer() { return activePlayer; }
	public int getTurn() { return turn; }
	public Card getActiveCard() { return activeCard; }

	public List<Card> getRow(int i) { return row.get(i); }
	public List<Card> getSpecial(int i) { return special.get(i); }
	
	public boolean canPlace(int player, int row, Card card) {
		if (player == 1)
			row = 5 - row;
		if (card.ability == Ability.SPY)
			row = 5 - row;
		if (row < 3)
			return false;
		return switch (card.row) {
			case CLOSED -> row == 3;
			case SIEGE -> row == 5;
			case RANGED -> row == 4;
			case AGILE -> row == 3 || row == 4;
			default -> false;
		};
	}
	public boolean canPlaceSpecial(int player, int row, Card card) {
		if (card.ability == Ability.DECOY)
			return false;

		return card.faction == Faction.SPECIAL;
	}
	public boolean canSwap(int player, Card c1, Card c2) {
		if (c1.ability != Ability.DECOY)
			return false;

		boolean inRow = false;
		for (int i = (player == 1? 0: 3); i < (player == 1? 3: 6); i++)
			inRow |= row.get(i).contains(c2);
		return playerData[player].handCards.contains(c1) && inRow;
	}

	public int calcRowScore(int i) {
		int ans = 0;
		for (Card card : row.get(i))
			ans += card.getScore();
		return ans;
	}

	public int calcPlayerScore(int player) {
		int ans = 0;
		for (int i = (player == 1? 0: 3); i < (player == 1? 3: 6); i++)
			ans += calcRowScore(i);
		return ans;
	}
}
