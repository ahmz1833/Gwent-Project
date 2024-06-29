package org.apgrp10.gwent.client.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apgrp10.gwent.client.model.WaitExec;
import org.apgrp10.gwent.client.view.GameMenu;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.Ability;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;

import javafx.stage.Stage;

public class GameController {
	public static class PlayerData {
		public final User user;
		public final Deck deck;
		public final InputController controller;
		public final List<Card> handCards = new ArrayList<>();
		public final List<Card> usedCards = new ArrayList<>();
		public final List<Card> ownedCards = new ArrayList<>();
		public boolean vetoDone;
		public int hp = 2;
		public boolean leaderUsed;
		public boolean leaderCancelled;

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
	private final List<Card> weather = new ArrayList<>();
	private int turn = 0;
	private int activePlayer = 0;
	private Card activeCard;
	private Map<Integer, Card> cardIdMap = new HashMap<>();
	private boolean lastPassed;
	private Random rand;

	public boolean leaderAbilityInUse(int player, Ability ability) {
		boolean ans = false;
		for (int p = 0; p < 2; p++) {
			if (player != -1 && player != p)
				continue;
			ans |= playerData[p].leaderUsed
					&& !playerData[p].leaderCancelled
					&& playerData[p].deck.getLeader().ability == ability;
		}
		return ans;
	}

	public boolean isPassed() {
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
		rand = new Random(seed);
		for (PlayerData p : playerData) {
			Deck d = p.deck;

			p.ownedCards.addAll(d.getDeck());

			id = d.assignGameIds(id);
			d.shuffle(rand);

			for (Card card : d.getDeck())
				cardIdMap.put(card.getGameId(), card);

			int handSize = p.deck.getLeader().ability == Ability.FRANCESCA_DAISY? 11: 10;
			for (int i = 0; i < handSize; i++) {
				p.handCards.add(p.deck.getDeck().get(0));
				p.deck.getDeck().remove(0);
			}
		}

		// must be last so GameController initialization is complete
		gameMenu = new GameMenu(this, stage);

		// must be after instanciating GameMenu
		c0.start(this, 0);
		c1.start(this, 1);
		c0.veto();
		c1.veto();
	}

	private void beginRound() {
		turn = 0;
		nextTurnDelay = 1000;
		playerData[0].controller.beginTurn();
	}

	private long nextTurnDelay = 1000;

	public Card cardById(int cardId) {
		return cardIdMap.get(cardId);
	}
	public int ownerOfCard(Card card) {
		return playerData[1].ownedCards.contains(card)? 1: 0;
	}
	public int rowOfCard(Card card) {
		for (int i = 0; i < 6; i++)
			if (row.get(i).contains(card) || special.get(i).contains(card))
				return i;
		return -1;
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
		} else if (idx < 13) { // weather
			gameMenu.animationToWeather(card);
			weather.add(card);

			if (card.ability == Ability.CLEAR) {
				nextTurnDelay += 600;
				new WaitExec(600, () -> {
					for (Card c : weather)
						gameMenu.animationToUsed(c, ownerOfCard(c));
					weather.clear();
					gameMenu.redraw();
				});
			}
		}
	}

	private List<Card> strongest(List<Card> list) {
		List<Card> ans = new ArrayList<>();
		for (Card card : list) {
			if (card.isHero)
				continue;
			if (!ans.isEmpty()) {
				int x = calcCardScore(ans.get(0));
				int y = calcCardScore(card);
				if (x > y)
					continue;
				if (x < y)
					ans.clear();
			}
			ans.add(card);
		}
		return ans;
	}

	private List<Card> strongestRow(int i) { return strongest(row.get(i)); }
	private List<Card> strongestAll() {
		List<Card> list = new ArrayList<>();
		for (int i = 0; i < 6; i++)
			list.addAll(row.get(i));
		return strongest(list);
	}

	private void scorchWithDelay(List<Card> list, long delay) {
		nextTurnDelay += 1000 + delay;
		new WaitExec(delay, () -> {
			gameMenu.setScorchCards(list);
			gameMenu.redraw();
			new WaitExec(1000, () -> {
				gameMenu.setScorchCards(new ArrayList<>());
				for (Card card : list)
					gameMenu.animationToUsed(card, ownerOfCard(card));
				for (int i = 0; i < 6; i++)
					row.get(i).removeAll(list);
				gameMenu.redraw();
			});
		});
	}

	private void scorchWhenPlaced(List<Card> list) { scorchWithDelay(list, 500); }
	private void scorchNow(List<Card> list) { scorchWithDelay(list, 0); }

	private void transformCard(Card card, CardInfo info) {
		Card newCard = new Card(info.name, info.pathAddress, info.strength, info.row, info.faction, info.ability, info.isHero);
		newCard.setGameId(card.getGameId());

		List<List<Card>> lists = new ArrayList<>();
		lists.addAll(row);
		lists.addAll(special);
		lists.add(weather);
		for (PlayerData data : playerData) {
			lists.add(data.deck.getDeck());
			lists.add(data.handCards);
			lists.add(data.usedCards);
			lists.add(data.ownedCards);
		}

		for (List<Card> list : lists) {
			if (!list.contains(card))
				continue;
			int i = list.indexOf(card);
			list.set(i, newCard);
		}
	}

	private void checkBerserker() {
		for (int i = 0; i < 6; i++) {
			if (!row.get(i).stream().anyMatch(card -> card.ability == Ability.MARDROEME)
					&& !special.get(i).stream().anyMatch(card -> card.ability == Ability.MARDROEME))
				continue;

			for (Card card : row.get(i)) {
				if (card.ability == Ability.BERSERKER)
					transformCard(card, CardInfo.byPathAddress(card.pathAddress.replace("berserker", "vildkaarl")));
			}
		}
	}

	private void playCardImpl(int player, Card card, int rowIdx) {
		// must be before placing so the card itself isn't considered for being strongest
		// other things needed for scorch will be done further down
		if (card.ability == Ability.SCORCH)
			scorchWhenPlaced(strongestAll());

		placeCard(card, rowIdx);

		if (card.ability == Ability.SPY) {
			List<Card> deck = playerData[player].deck.getDeck();
			if (!deck.isEmpty()) moveCardToHand(player, deck.get(0));
			if (!deck.isEmpty()) moveCardToHand(player, deck.get(0));
		}

		if (card.ability == Ability.MUSTER) {
			List<Card> toBeMustered = new ArrayList<>();
			for (Card c : playerData[player].deck.getDeck())
				if (card.name.equals(c.name))
					toBeMustered.add(c);
			for (Card c : playerData[player].handCards)
				if (card.name.equals(c.name))
					toBeMustered.add(c);
			for (Card c : toBeMustered)
				placeCard(c, rowIdx);
		}

		if (card.ability == Ability.MEDIC) {
			nextTurnDelay = -1;
			List<Card> list = playerData[player].usedCards.stream()
				.filter(c -> c.row != Row.NON)
				.collect(Collectors.toList());

			if (leaderAbilityInUse(-1, Ability.EMHYR_INVADER)) {
				pickRevive(player, list.isEmpty()? null: list.get(rand.nextInt(list.size())));
				return;
			}

			playerData[player].controller.pick(list, "revive");
		}

		if (player == 0) {
			if (card.ability == Ability.SCORCH_C && calcRowScore(2) >= 10) scorchWhenPlaced(strongestRow(2));
			if (card.ability == Ability.SCORCH_R && calcRowScore(1) >= 10) scorchWhenPlaced(strongestRow(1));
			if (card.ability == Ability.SCORCH_S && calcRowScore(0) >= 10) scorchWhenPlaced(strongestRow(0));
		} else {
			if (card.ability == Ability.SCORCH_C && calcRowScore(3) >= 10) scorchWhenPlaced(strongestRow(3));
			if (card.ability == Ability.SCORCH_R && calcRowScore(4) >= 10) scorchWhenPlaced(strongestRow(4));
			if (card.ability == Ability.SCORCH_S && calcRowScore(5) >= 10) scorchWhenPlaced(strongestRow(5));
		}
		if (card.ability == Ability.SCORCH) {
			if (card.row == Row.NON) new WaitExec(600, () -> {
				for (int i = 0; i < 6; i++)
					special.get(i).remove(card);
				playerData[player].usedCards.add(card);
				gameMenu.animationToUsed(card, player);
				gameMenu.redraw();
			});
		}

		if (card.ability == Ability.MARDROEME || card.ability == Ability.BERSERKER)
			checkBerserker();
	}

	private void playCard(Command.PlayCard cmd) {
		playCardImpl(cmd.player(), cardById(cmd.cardId()), cmd.row());
		nextTurn();
	}

	private void nextTurn() {
		if (nextTurnDelay == -1) {
			nextTurnDelay = 1000;
			return;
		}
		if (!lastPassed) {
			playerData[turn].controller.endTurn();
			new WaitExec(nextTurnDelay, () -> {
				turn = 1 - turn;
				playerData[turn].controller.beginTurn();
			});
			nextTurnDelay = 1000;
		} else if (nextTurnDelay > 1000) {
			playerData[turn].controller.pauseTurn();
			new WaitExec(nextTurnDelay, () -> { playerData[turn].controller.resumeTurn(); });
		}
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

		nextTurn();
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

	private void nextRound() {
		playerData[turn].controller.endTurn();
		boolean end = false;
		for (int i = 0; i < 2; i++) {
			boolean lose = calcPlayerScore(i) <= calcPlayerScore(1 - i);
			playerData[i].hp -= lose? 1: 0;
			end |= playerData[i].hp == 0;
		}
		if (end) {
			// TODO
			System.exit(0);
		}

		lastPassed = false;
		activeCard = null;

		List<Card> toBeRemoved = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			toBeRemoved.addAll(row.get(i));
			toBeRemoved.addAll(special.get(i));
		}
		toBeRemoved.addAll(weather);

		Iterator<Card> it = toBeRemoved.iterator();
		while (it.hasNext()) {
			Card card = it.next();

			if (card.ability == Ability.AVENGER || card.ability == Ability.AVENGER_KAMBI) {
				// TODO: use a better method for choosing the new card e.g. using the Random object
				CardInfo info = CardInfo.allCards.stream()
					.filter(i -> i.row == card.row)
					.filter(i -> i.strength == 8)
					.filter(i -> !i.isHero)
					.findAny()
					.get();
				new WaitExec(500, () -> transformCard(card, info));
				it.remove();
			}
		}

		for (int i = 0; i < 6; i++) {
			row.get(i).removeAll(toBeRemoved);
			special.get(i).removeAll(toBeRemoved);
		}
		weather.removeAll(toBeRemoved);
		for (Card card : toBeRemoved) {
			gameMenu.animationToUsed(card, ownerOfCard(card));
			playerData[ownerOfCard(card)].usedCards.add(card);
		}

		gameMenu.redraw();
		new WaitExec(600, () -> beginRound());
	}

	private void pass(Command.Pass cmd) {
		if (!lastPassed) {
			lastPassed = true;
			nextTurnDelay = 100;
			nextTurn();
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
				beginRound();
			return;
		}

		List<Card> deck = data.deck.getDeck();
		List<Card> hand = data.handCards;
		hand.set(hand.indexOf(card), deck.get(0));
		deck.remove(0);
		deck.add(card);
	}

	private void playLeader(Command.PlayLeader cmd) {
		PlayerData data = playerData[cmd.player()];
		data.leaderUsed = true;
		doLeaderAbility(cmd.player(), data.deck.getLeader().ability);
		nextTurn();
	}

	private void pickView(int player, Card card) { }
	private void pickStealUsed(int player, Card card) {
		if (card == null) return;

		PlayerData us = playerData[player];
		PlayerData them = playerData[1 - player];

		them.ownedCards.remove(card);
		them.usedCards.remove(card);
		us.ownedCards.add(card);
		us.handCards.add(card);

		if (player == activePlayer)
			gameMenu.animationToHand(card);
	}
	private void pickRevive(int player, Card card) {
		if (card == null) return;
		int row = 0;
		// TODO: what happens when an agile card gets revived?
		while (!canPlace(player, row, card)) row++;
		playCardImpl(player, card, row);
	}
	private void pickRestoreToHand(int player, Card card) {
		if (card == null) return;
		gameMenu.animationToHand(card);
		playerData[player].usedCards.remove(card);
		playerData[player].handCards.add(card);
	}
	private void pickDiscard(int player, Card card, int i) {
		nextTurnDelay = -1;
		List<Card> deck = new ArrayList<>();
		deck.addAll(playerData[player].deck.getDeck());
		Collections.shuffle(deck, rand);
		if (card == null) {
			playerData[player].controller.pick(deck, "deck_to_hand");
			return;
		}
		gameMenu.animationToUsed(card, player);
		playerData[player].handCards.remove(card);
		playerData[player].usedCards.add(card);
		playerData[player].controller.pick(i == 0? playerData[player].handCards: deck, i == 0? "discard_2": "deck_to_hand");
	}
	private void pickDeckToHand(int player, Card card) {
		if (card == null) return;
		gameMenu.animationToHand(card);
		playerData[player].deck.removeCard(card);
		playerData[player].handCards.add(card);
	}
	private void pickWeather(int player, Card card) {
		if (card == null) return;
		playCardImpl(player, card, 12);
	}

	private void pickResponse(Command.PickResponse cmd) {
		Card card = cardById(cmd.cardId());
		int player = cmd.player();
		switch (cmd.what()) {
			case "revive" -> pickRevive(player, card);
			case "view_enemy_hand" -> pickView(player, card);
			case "steal_used" -> pickStealUsed(player, card);
			case "restore_to_hand" -> pickRestoreToHand(player, card);
			case "discard_1" -> pickDiscard(player, card, 0);
			case "discard_2" -> pickDiscard(player, card, 1);
			case "deck_to_hand" -> pickDeckToHand(player, card);
			case "weather" -> pickWeather(player, card);
			default -> { assert false; }
		}
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
			if (cmd instanceof Command.PlayLeader) playLeader((Command.PlayLeader)cmd);
			if (cmd instanceof Command.MoveToHand) moveToHand((Command.MoveToHand)cmd);
			if (cmd instanceof Command.Pass) pass((Command.Pass)cmd);
			if (cmd instanceof Command.SetActiveCard) setActiveCard((Command.SetActiveCard)cmd);
			if (cmd instanceof Command.PickResponse) pickResponse((Command.PickResponse)cmd);
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
	public List<Card> getWeather() { return weather; }
	
	public boolean canPlace(int player, int row, Card card) {
		if (card.ability == Ability.SCORCH && card.row == Row.NON)
			return true;
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
		if (card.ability == Ability.SCORCH && card.row == Row.NON)
			return true;
		if (card.ability == Ability.DECOY)
			return false;
		if ((player == 1) != (row < 3))
			return false;
		if (card.ability == Ability.HORN && special.get(row).stream().anyMatch(c -> c.ability == Ability.HORN))
			return false;

		return card.faction == Faction.SPECIAL;
	}
	public boolean canPlaceWeather(int player, Card card) {
		return card.faction == Faction.WEATHER;
	}
	public boolean canSwap(int player, Card c1, Card c2) {
		if (c1.ability != Ability.DECOY)
			return false;

		boolean inRow = false;
		boolean side = player != (c2.ability == Ability.SPY? 1: 0);
		for (int i = (side? 0: 3); i < (side? 3: 6); i++)
			inRow |= row.get(i).contains(c2);
		return playerData[player].handCards.contains(c1) && inRow;
	}

	public boolean hasFrost() { return weather.stream().anyMatch(c -> c.ability == Ability.FROST); }
	public boolean hasFog() { return weather.stream().anyMatch(c -> c.ability == Ability.FOG || c.ability == Ability.RAIN_FOG); }
	public boolean hasRain() { return weather.stream().anyMatch(c -> c.ability == Ability.RAIN || c.ability == Ability.RAIN_FOG); }

	private int calcCardScoreRow(Card card, int row) {
		int score = card.strength;

		// TODO: round up or down?
		if ((row == 0 || row == 5) && hasRain()) score = leaderAbilityInUse(-1, Ability.KING_BRAN)? score / 2: 1;
		if ((row == 1 || row == 4) && hasFog()) score = leaderAbilityInUse(-1, Ability.KING_BRAN)? score / 2: 1;
		if ((row == 2 || row == 3) && hasFrost()) score = leaderAbilityInUse(-1, Ability.KING_BRAN)? score / 2: 1;
		
		score += this.row.get(row).stream().filter(c -> c.ability == Ability.MORALE).count();
		if (card.ability == Ability.MORALE)
			score -= 1;

		if (card.ability == Ability.BOND) {
			int x = 0;
			for (Card c : this.row.get(row)) {
				if (card.name.equals(c.name))
					x += score;
			}
			score = x;
		}

		boolean horn = false;
		horn |= this.row.get(row).stream().anyMatch(c -> c.ability == Ability.HORN);
		horn |= special.get(row).stream().anyMatch(c -> c.ability == Ability.HORN);
		horn |= (row == 5 || row == 0) && leaderAbilityInUse(row < 3? 1: 0, Ability.FOLTEST_KING);
		horn |= (row == 4 || row == 1) && leaderAbilityInUse(row < 3? 1: 0, Ability.FRANCESCA_BEAUTIFUL);
		horn |= (row == 3 || row == 2) && leaderAbilityInUse(row < 3? 1: 0, Ability.EREDIN_BRINGER_OF_DEATH);
		if (card.ability != Ability.HORN && horn)
			score *= 2;

		if (card.ability == Ability.SPY && leaderAbilityInUse(-1, Ability.EREDIN_TREACHEROUS))
			score *= 2;

		return score;
	}

	public int calcCardScore(Card card) {
		if (card.isHero || card.row == Row.NON)
			return card.strength;

		int row = rowOfCard(card);
		if (row == -1)
			return card.strength;

		return calcCardScoreRow(card, row);
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

	private void doLeaderAbility(int player, Ability ability) {
		PlayerData us = playerData[player], them = playerData[1 - player];
		switch (ability) {
			case FOLTEST_SIEGEMASTER -> {
				Card card = us.deck.getDeck().stream().filter(c -> c.ability == Ability.FOG).findFirst().orElse(null);
				if (card != null)
					playCard(new Command.PlayCard(player, card.getGameId(), 12));
			}

			case FOLTEST_STEELFORGED -> {
				for (Card card : weather)
					gameMenu.animationToUsed(card, ownerOfCard(card));
				weather.clear();
			}

			case FOLTEST_KING -> {}

			case FOLTEST_LORD -> {
				if (calcRowScore(player == 1? 5: 0) >= 10)
					scorchNow(strongestRow(player == 1? 5: 0));
			}

			case FOLTEST_SON -> {
				if (calcRowScore(player == 1? 4: 1) >= 10)
					scorchNow(strongestRow(player == 1? 4: 1));
			}

			case EMHYR_WHITEFLAME -> {
				Card card = us.deck.getDeck().stream().filter(c -> c.ability == Ability.RAIN).findFirst().orElse(null);
				if (card != null)
					playCardImpl(player, card, 12);
			}

			case EMHYR_IMPERIAL -> {
				nextTurnDelay = -1;
				List<Card> list = new ArrayList<>();
				list.addAll(them.handCards);
				Collections.shuffle(list, rand);
				while (list.size() > 3)
					list.removeLast();
				us.controller.pick(list, "view_enemy_hand");
			}

			case EMHYR_EMPEROR -> {
				them.leaderCancelled = true;
			}

			case EMHYR_RELENTLESS -> {
				nextTurnDelay = -1;
				us.controller.pick(them.usedCards, "steal_used");
			}

			case EMHYR_INVADER -> {}

			case EREDIN_BRINGER_OF_DEATH -> {}

			case EREDIN_KING -> {
				nextTurnDelay = -1;
				us.controller.pick(us.usedCards, "restore_to_hand");
			}

			case EREDIN_DESTROYER -> {
				nextTurnDelay = -1;
				us.controller.pick(us.handCards, "discard_1");
			}

			case EREDIN_COMMANDER -> {
				nextTurnDelay = -1;
				List<Card> list = us.deck.getDeck().stream()
					.filter(c -> canPlace(player, 12, c))
					.collect(Collectors.toList());
				us.controller.pick(list, "weather");
			}

			case EREDIN_TREACHEROUS -> {}

			case FRANCESCA_QUEEN -> {
				if (calcRowScore(player == 1? 3: 2) >= 10)
					scorchNow(strongestRow(player == 1? 3: 2));
			}

			case FRANCESCA_BEAUTIFUL -> {}

			case FRANCESCA_DAISY -> {}

			case FRANCESCA_PUREBLOOD -> {
				Card card = us.deck.getDeck().stream().filter(c -> c.ability == Ability.FROST).findFirst().orElse(null);
				if (card != null)
					playCardImpl(player, card, 12);
			}

			case FRANCESCA_HOPE -> {
				List<List<Card>> rem = new ArrayList<>();
				List<List<Card>> add = new ArrayList<>();
				for (int i = 0; i < 6; i++) {
					rem.add(new ArrayList<>());
					add.add(new ArrayList<>());
				}
				for (int i = 0; i < 6; i++) {
					for (Card card : row.get(i)) {
						// TODO: does it actualy move enemy's units as well?
						if (ownerOfCard(card) != player)
							continue;
						int mx = i;
						for (int j = 0; j < 6; j++) {
							if (!canPlace(player, j, card))
								continue;
							if (calcCardScoreRow(card, mx) < calcCardScoreRow(card, j))
								mx = j;
						}
						if (mx == i)
							continue;
						gameMenu.animationToRow(card, mx);
						rem.get(i).add(card);
						add.get(mx).add(card);
					}
				}
				for (int i = 0; i < 6; i++) {
					row.get(i).removeAll(rem.get(i));
					row.get(i).addAll(add.get(i));
				}
			}

			case CRACH_AN_CRAITE -> {
				for (int p = 0; p < 2; p++) {
					PlayerData data = playerData[p];

					for (Card card : data.usedCards)
						gameMenu.animationToDeck(card, p);

					data.deck.getDeck().addAll(data.usedCards);
					data.deck.shuffle(rand);
					data.usedCards.clear();
				}
			}

			case KING_BRAN -> {}

			default -> {
				assert false;
			}
		}
	}
}
