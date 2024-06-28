package org.apgrp10.gwent.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apgrp10.gwent.client.model.WaitExec;
import org.apgrp10.gwent.client.view.GameMenu;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.Ability;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.Faction;

import javafx.stage.Stage;

public class GameController {
	public static class PlayerData {
		public final User user;
		public final Deck deck;
		public final InputController controller;
		public final List<Card> handCards = new ArrayList<>();
		public final List<Card> usedCards = new ArrayList<>();
		public boolean vetoDone;

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
	private boolean lastPassed;

	public boolean isPasses() {
		return lastPassed;
	}

	public GameController(Stage stage, InputController c0, InputController c1, Deck d0, Deck d1, long seed) {
		playerData[0] = new PlayerData(d0, c0);
		playerData[1] = new PlayerData(d1, c1);
		this.stage = stage;
		turn = 0;

		for (int i = 0; i < 6; i++) {
			row.add(new ArrayList<>());
			special.add(new ArrayList<>());
		}

		int id = 0;
		Random rand = new Random(seed);
		for (PlayerData p : playerData) {
			Deck d = p.deck;

			id = d.assignGameIds(id);
			d.shuffle(rand);

			for (Card card : d.getDeck())
				cardIdMap.put(card.getGameId(), card);
		}

		for (PlayerData p : playerData) for (int i = 0; i < 10; i++) {
			p.handCards.add(p.deck.getDeck().get(0));
			p.deck.getDeck().remove(0);
		}

		// must be last so GameController initialization is complete
		gameMenu = new GameMenu(this, stage);

		// must be after instanciating GameMenu
		c0.start(this, 0);
		c1.start(this, 1);
		c0.veto();
		c1.veto();
	}

	public Card cardById(int cardId) {
		return cardIdMap.get(cardId);
	}

	public GameMenu getGameMenu() { return gameMenu; }

	public PlayerData getPlayer(int player) { return playerData[player]; }

	private void placeCard(Card card, int idx) {
		playerData[0].handCards.remove(card);
		playerData[1].handCards.remove(card);
		playerData[0].deck.removeCard(card);
		playerData[1].deck.removeCard(card);
		if (idx < 6) { // normal row
			gameMenu.animationToRow(card, idx);
			row.get(idx).add(card);
		} else if (idx < 12) { // special place
			idx -= 6;
			gameMenu.animationToSpecial(card, idx);
			special.get(idx).add(card);
		}
	}

	private void playCard(Command.PlayCard cmd) {
		Card card = cardById(cmd.cardId());
		placeCard(card, cmd.row());

		if (card.ability == Ability.SPY) {
			List<Card> deck = playerData[cmd.player()].deck.getDeck();
			if (!deck.isEmpty()) moveCardToHand(cmd.player(), deck.get(0));
			if (!deck.isEmpty()) moveCardToHand(cmd.player(), deck.get(0));
		}

		if (card.ability == Ability.MUSTER) {
			List<Card> toBeMustered = new ArrayList<>();
			for (Card c : playerData[cmd.player()].deck.getDeck())
				if (card.name.equals(c.name))
					toBeMustered.add(c);
			for (Card c : toBeMustered)
				placeCard(c, cmd.row());
		}

		if (card.ability == Ability.MEDIC) {
			if (!playerData[cmd.player()].usedCards.isEmpty()) {
				playerData[cmd.player()].controller.reviveCard();
				return;
			}
		}

		if (!lastPassed)
			nextTurn(1000);
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

		if (!lastPassed)
			nextTurn(1000);
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

	private void nextTurn(long delay) {
		playerData[turn].controller.endTurn();
		new WaitExec(delay, () -> {
			turn = 1 - turn;
			playerData[turn].controller.beginTurn();
		});
	}

	private void setActiveCard(Command.SetActiveCard cmd) {
		activeCard = cardById(cmd.cardId());
	}

	private void nextRound() {
		// TODO
		System.exit(0);
	}

	private void pass(Command.Pass cmd) {
		if (!lastPassed) {
			lastPassed = true;
			nextTurn(0);
		} else {
			nextRound();
		}
	}

	private void vetoCard(Command.VetoCard cmd) {
		Card card = cardById(cmd.cardId());
		PlayerData data = playerData[cmd.player()];

		if (card == null) {
			data.vetoDone = true;
			if (playerData[0].vetoDone && playerData[1].vetoDone)
				playerData[0].controller.beginTurn();
			return;
		}

		List<Card> deck = data.deck.getDeck();
		List<Card> hand = data.handCards;
		hand.set(hand.indexOf(card), deck.get(0));
		deck.remove(0);
		deck.add(card);
	}

	public static interface CommandListener { public void call(Command cmd); }
	private final List<CommandListener> commandListeners = new ArrayList<>();
	public void addCommandListener(CommandListener cb) { commandListeners.add(cb); }
	public void removeCommandListener(CommandListener cb) { commandListeners.remove(cb); }

	private List<Command> commandQueue = new ArrayList<>();

	private void syncCommands() {
		for (Command cmd : commandQueue) {
			if (cmd instanceof Command.VetoCard) vetoCard((Command.VetoCard)cmd);
			if (cmd instanceof Command.PlayCard) playCard((Command.PlayCard)cmd);
			if (cmd instanceof Command.SwapCard) swapCard((Command.SwapCard)cmd);
			if (cmd instanceof Command.MoveToHand) moveToHand((Command.MoveToHand)cmd);
			if (cmd instanceof Command.SetActiveCard) setActiveCard((Command.SetActiveCard)cmd);
			if (cmd instanceof Command.Pass) pass((Command.Pass)cmd);
		}
		commandQueue.clear();
		gameMenu.redraw();
	}

	public void sendCommand(Command cmd) {
		System.out.println(cmd);

		if (cmd instanceof Command.Sync)
			syncCommands();
		else
			commandQueue.add(cmd);

		// we make a deep copy because some listeners might remove themselves while we are iterating
		List<CommandListener> copy = new ArrayList<>();
		copy.addAll(commandListeners);
		for (CommandListener cb : copy)
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

	public int calcCardScore(Card card) {
		if (card.isHero)
			return card.strength;

		int row = -1;
		for (int i = 0; i < 6; i++)
			if (this.row.get(i).contains(card))
				row = i;
		if (row == -1)
			return card.strength;

		int score = card.strength;
		
		if (card.ability == Ability.BOND) {
			int x = 0;
			for (Card c : this.row.get(row)) {
				if (card.name.equals(c.name))
					x += score;
			}
			score = x;
		}

		if (this.row.get(row).stream().anyMatch(c -> c.ability == Ability.HORN)
				|| special.get(row).stream().anyMatch(c -> c.ability == Ability.HORN))
			score *= 2;

		return score;
	}

	public int calcRowScore(int i) {
		int ans = 0;
		for (Card card : row.get(i))
			ans += calcCardScore(card);
		return ans;
	}

	public int calcPlayerScore(int player) {
		int ans = 0;
		for (int i = (player == 1? 0: 3); i < (player == 1? 3: 6); i++)
			ans += calcRowScore(i);
		return ans;
	}
}
