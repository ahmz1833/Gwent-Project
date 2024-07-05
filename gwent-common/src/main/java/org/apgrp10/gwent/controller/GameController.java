package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.utils.WaitExec;
import org.apgrp10.gwent.view.GameMenuInterface;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GameController {
	public static class PlayerData {
		public final User.PublicInfo user;
		public final Deck deck;
		public final InputController controller;
		public final List<Card> handCards = new ArrayList<>();
		public final List<Card> usedCards = new ArrayList<>();
		public final List<Card> ownedCards = new ArrayList<>();
		public boolean vetoDone;
		public int hp = 2;
		public boolean leaderUsed;
		public boolean leaderCancelled;
		public boolean cheatHorn;
		public final String originalDeckJson;

		public PlayerData(Deck deck, InputController controller) {
			originalDeckJson = deck.toJsonString();
			this.user = deck.getUser();
			this.deck = deck;
			this.controller = controller;
		}
	}
	private final GameMenuInterface gameMenu;
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
	private int currentRound = 0;
	public final WaitExec waitExec;

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

	private Consumer<GameRecord> onEnd;
	private long seed;

	public GameController(
			InputController c0,
			InputController c1,
			Deck d0,
			Deck d1,
			long seed,
			GameMenuInterface gameMenu,
			Consumer<GameRecord> onEnd
	) {
		this.onEnd = onEnd;
		this.seed = seed;
		playerData[0] = new PlayerData(d0, c0);
		playerData[1] = new PlayerData(d1, c1);
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

		this.waitExec = new WaitExec(gameMenu == null);

		// must be last so GameController initialization is complete
		this.gameMenu = gameMenu;
		if (gameMenu != null) {
			gameMenu.setController(this);
			gameMenu.start();
		}

		// must be after instanciating GameMenu
		c0.start(this, 0);
		c1.start(this, 1);
		c0.veto();
		c1.veto();
	}

	private void beginRound() {
		currentRound++;

		Faction f0 = playerData[0].deck.getFaction();
		Faction f1 = playerData[1].deck.getFaction();
		if (f0 == Faction.SCOIATAEL && f1 != Faction.SCOIATAEL)
			turn = 0;
		else if (f1 == Faction.SCOIATAEL && f0 != Faction.SCOIATAEL)
			turn = 1;
		else
			turn = rand.nextInt(2);

		if (currentRound >= 3) {
			for (int p = 0; p < 2; p++) {
				Faction f = p == 1? f1: f0;
				if (f != Faction.SKELLIGE)
					continue;
				for (int i = 0; i < 2; i++) {
					// TODO: medic requires "pick"ing which is a pain and currently is not possible when it's not your turn
					Card card = chooseRandom(playerData[p].usedCards.stream()
							.filter(c -> c.row != Row.NON)
							.filter(c -> c.ability != Ability.MEDIC)
							.collect(Collectors.toList()));
					if (card == null)
						continue;
					int row = 0;
					// TODO: what happens when an agile card gets revived?
					while (!canPlace(p, row, card)) row++;
					playCardImpl(p, card, row);
				}
			}
		}

		if (gameMenu != null)
			gameMenu.beginRound();

		nextTurnDelay = 1000;
		playerData[turn].controller.beginTurn();
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

	public GameMenuInterface getGameMenu() { return gameMenu; }

	public PlayerData getPlayer(int player) { return playerData[player]; }

	private void moveCard(Card card, List<Card> to) {
		if (card == null)
			return;

		if (gameMenu != null) {
			for (int p = 0; p < 2; p++) {
				if (to == playerData[p].deck.getDeck()) gameMenu.animationToDeck(card, p);
				if (to == playerData[p].handCards && activePlayer == p) gameMenu.animationToHand(card);
				if (to == playerData[p].usedCards) {
					gameMenu.animationToUsed(card, p);
					gameMenu.setHaveNewDeath(true);
				}
			}
			for (int i = 0; i < 6; i++) {
				if (to == row.get(i)) gameMenu.animationToRow(card, i);
				if (to == special.get(i)) gameMenu.animationToSpecial(card, i);
			}
			if (to == weather) gameMenu.animationToWeather(card);
		}

		for (int p = 0; p < 2; p++) {
			playerData[p].deck.removeCard(card);
			playerData[p].handCards.remove(card);
			playerData[p].usedCards.remove(card);
		}
		for (int i = 0; i < 6; i++) {
			row.get(i).remove(card);
			special.get(i).remove(card);
		}
		weather.remove(card);

		to.add(card);
	}

	private void placeCard(Card card, int idx) {
		if (idx < 6) { // normal row
			moveCard(card, row.get(idx));
		} else if (idx < 12) { // special place
			idx -= 6;
			moveCard(card, special.get(idx));
		} else if (idx < 13) { // weather
			moveCard(card, weather);

			if (card.ability == Ability.CLEAR) {
				nextTurnDelay += 600;
				waitExec.run(600, () -> {
					for (Card c : new ArrayList<>(weather))
						moveCard(c, playerData[ownerOfCard(c)].usedCards);
					if (gameMenu != null)
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
		waitExec.run(delay, () -> {
			if (gameMenu != null) {
				gameMenu.setScorchCards(list);
				gameMenu.redraw();
			}
			waitExec.run(1000, () -> {
				if (gameMenu != null)
					gameMenu.setScorchCards(new ArrayList<>());
				for (Card card : list)
					moveCard(card, playerData[ownerOfCard(card)].usedCards);
				if (gameMenu != null)
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

	private<T> T chooseRandom(List<T> list) {
		return list.isEmpty()? null: list.get(rand.nextInt(list.size()));
	}

	private void playCardImpl(int player, Card card, int rowIdx) {
		// must be before placing so the card itself isn't considered for being strongest
		// other things needed for scorch will be done further down
		if (card.ability == Ability.SCORCH)
			scorchWhenPlaced(strongestAll());

		placeCard(card, rowIdx);

		if (card.ability == Ability.SPY) {
			List<Card> deck = playerData[player].deck.getDeck();
			if (!deck.isEmpty()) moveCard(deck.get(0), playerData[player].handCards);
			if (!deck.isEmpty()) moveCard(deck.get(0), playerData[player].handCards);
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

			if (leaderAbilityInUse(-1, Ability.EMHYR_INVADER))
				pickRevive(player, chooseRandom(list));
			else
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
			if (card.row == Row.NON) waitExec.run(600, () -> {
				moveCard(card, playerData[player].usedCards);
				if (gameMenu != null)
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
			if (gameMenu != null)
				gameMenu.userTurn(getTurn());
			playerData[turn].controller.endTurn();
			waitExec.run(nextTurnDelay, () -> {
				turn = 1 - turn;
				playerData[turn].controller.beginTurn();
			});
			nextTurnDelay = 1000;
		} else if (nextTurnDelay > 1000) {
			playerData[turn].controller.pauseTurn();
			waitExec.run(nextTurnDelay, () -> { playerData[turn].controller.resumeTurn(); });
		}
	}

	private void swapCard(Command.SwapCard cmd) {
		Card c1 = cardById(cmd.cardId1());
		Card c2 = cardById(cmd.cardId2());
		if (gameMenu != null)
			gameMenu.animationSwap(c1, c2);

		// there are some guarantees because of canSwap and we rely on them
		int player = cmd.player();
		playerData[player].handCards.set(playerData[player].handCards.indexOf(c1), c2);
		for (int i = 0; i < 6; i++) {
			if (row.get(i).contains(c2)) {
				row.get(i).set(row.get(i).indexOf(c2), c1);
				break;
			}
		}

		nextTurn();
	}

	private void moveToHand(Command.MoveToHand cmd) {
		Card card = cardById(cmd.cardId());
		moveCard(card, playerData[cmd.player()].handCards);
	}

	private void setActiveCard(Command.SetActiveCard cmd) {
		activeCard = cardById(cmd.cardId());
	}

	public boolean gonnaWin(int player) {
		int us = calcPlayerScore(player);
		int them = calcPlayerScore(1 - player);
		Faction ourFaction /* Soyuz nerushimy... */ = playerData[player].deck.getFaction();
		Faction theirFaction = playerData[1 - player].deck.getFaction();
		return us > them || (us == them && ourFaction == Faction.NILFGAARD && theirFaction != Faction.NILFGAARD);
	}

	private List<Integer> roundWinner = new ArrayList<>();
	private List<Integer> p1Sc = new ArrayList<>();
	private List<Integer> p2Sc = new ArrayList<>();

	private void nextRound() {
		playerData[turn].controller.endTurn();
		p1Sc.add(calcPlayerScore(0));
		p2Sc.add(calcPlayerScore(1));
		boolean end = false;
		int winner = -1;
		for (int i = 0; i < 2; i++) {
			boolean win = gonnaWin(i);
			if (win) {
				winner = i;
				if (gameMenu != null)
					gameMenu.showWinner(i);
			}
			playerData[i].hp -= win? 0: 1;
			if (win && playerData[i].deck.getFaction() == Faction.REALMS && !playerData[i].deck.getDeck().isEmpty())
				moveCard(playerData[i].deck.getDeck().get(0), playerData[i].handCards);
			end |= playerData[i].hp == 0;
		}
		roundWinner.add(winner);
		if (winner == -1 && gameMenu != null)
			gameMenu.showDraw();
		if (end) {
			int gameWinner = -1;
			if (playerData[0].hp > 0) gameWinner = 0;
			if (playerData[1].hp > 0) gameWinner = 1;
			GameRecord gr = new GameRecord(playerData[0].user.id(),
			                               playerData[1].user.id(),
			                               seed,
			                               playerData[0].originalDeckJson,
			                               playerData[1].originalDeckJson,
			                               new ArrayList<>(cmdHistory),
			                               gameWinner,
			                               new ArrayList<>(roundWinner),
			                               new ArrayList<>(p1Sc),
			                               new ArrayList<>(p2Sc));
			onEnd.accept(gr);
			return;
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
				CardInfo info = chooseRandom(CardInfo.allCards.stream()
					.filter(i -> i.row == card.row)
					.filter(i -> i.strength == 8)
					.filter(i -> !i.isHero)
					.collect(Collectors.toList()));
				waitExec.run(500, () -> transformCard(card, info));
				it.remove();
			}
		}

		for (int player = 0; player < 2; player++) {
			int p = player;
			if (playerData[p].deck.getFaction() == Faction.MONSTERS) {
				Card card = chooseRandom(toBeRemoved.stream()
					.filter(c -> ownerOfCard(c) == p)
					.filter(c -> c.row != Row.NON)
					.collect(Collectors.toList()));
				toBeRemoved.remove(card);
			}
		}

		for (Card card : toBeRemoved)
			moveCard(card, playerData[ownerOfCard(card)].usedCards);

		if (gameMenu != null)
			gameMenu.redraw();
		waitExec.run(600, () -> beginRound());
	}

	private void pass(Command.Pass cmd) {
		if (gameMenu != null)
			gameMenu.userPassed(cmd.player());
		if (!lastPassed) {
			nextTurn();
			lastPassed = true;
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

	private void pickViewEnemyHand(int player) { }
	private void pickCheatEnemyHand(int player) { nextTurnDelay = -1; }
	private void pickStealUsed(int player, Card card) {
		if (card == null) return;
		moveCard(card, playerData[player].handCards);
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
		moveCard(card, playerData[player].handCards);
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
		moveCard(card, playerData[player].usedCards);
		playerData[player].controller.pick(i == 0? playerData[player].handCards: deck, i == 0? "discard_2": "deck_to_hand");
	}
	private void pickDeckToHand(int player, Card card) {
		if (card == null) return;
		moveCard(card, playerData[player].handCards);
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
			case "view_enemy_hand" -> pickViewEnemyHand(player);
			case "steal_used" -> pickStealUsed(player, card);
			case "restore_to_hand" -> pickRestoreToHand(player, card);
			case "discard_1" -> pickDiscard(player, card, 0);
			case "discard_2" -> pickDiscard(player, card, 1);
			case "deck_to_hand" -> pickDeckToHand(player, card);
			case "weather" -> pickWeather(player, card);
			case "cheat_enemy_hand" -> pickCheatEnemyHand(player);
			default -> { assert false; }
		}
		nextTurn();
	}

	private void cheat(Command.Cheat cmd) {
		int p = cmd.player();
		switch (cmd.cheatId()) {
			case 0 -> {
				if (!playerData[p].deck.getDeck().isEmpty())
					moveCard(playerData[p].deck.getDeck().get(0), playerData[p].handCards);
			}
			case 1 -> {
				playerData[p].hp++;
			}
			case 2 -> {
				playerData[p].leaderUsed = false;
				playerData[p].leaderCancelled = false;
			}
			case 3 -> {
				for (Card card : new ArrayList<>(weather))
					moveCard(card, playerData[ownerOfCard(card)].usedCards);
			}
			case 4 -> {
				for (Card card : new ArrayList<>(playerData[p].usedCards))
					moveCard(card, playerData[p].handCards);
			}
			case 5 -> {
				playerData[p].controller.pick(playerData[1 - p].handCards, "cheat_enemy_hand");
			}
			case 6 -> {
				playerData[p].cheatHorn = !playerData[p].cheatHorn;
			}
			default -> { assert false; }
		}
	}

	private final List<Consumer<Command>> commandListeners = new ArrayList<>();

	public void addCommandListener(Consumer<Command> cb) { commandListeners.add(cb); }
	public void removeCommandListener(Consumer<Command> cb) { commandListeners.remove(cb); }

	private List<Command> commandQueue = new ArrayList<>();

	// this lock is not for multi-threading
	// it is for when precessing commands causes for new commands to arrive
	private boolean syncLock = false;
	private void syncCommands() {
		syncLock = true;
		for (int i = 0; i < commandQueue.size(); i++) {
			Command cmd = commandQueue.get(i);
			if (cmd instanceof Command.VetoCard) vetoCard((Command.VetoCard)cmd);
			if (cmd instanceof Command.PlayCard) playCard((Command.PlayCard)cmd);
			if (cmd instanceof Command.SwapCard) swapCard((Command.SwapCard)cmd);
			if (cmd instanceof Command.PlayLeader) playLeader((Command.PlayLeader)cmd);
			if (cmd instanceof Command.MoveToHand) moveToHand((Command.MoveToHand)cmd);
			if (cmd instanceof Command.Pass) pass((Command.Pass)cmd);
			if (cmd instanceof Command.SetActiveCard) setActiveCard((Command.SetActiveCard)cmd);
			if (cmd instanceof Command.PickResponse) pickResponse((Command.PickResponse)cmd);
			if (cmd instanceof Command.Cheat) cheat((Command.Cheat)cmd);
		}
		syncLock = false;
		commandQueue.clear();
		if (gameMenu != null)
			gameMenu.redraw();
	}

	private List<Command> cmdHistory = new ArrayList<>();

	public void sendCommand(Command cmd) {
		System.out.println(cmd);
		cmdHistory.add(cmd);

		if (cmd instanceof Command.Sync) {
			if (!syncLock)
				syncCommands();
		} else {
			commandQueue.add(cmd);
		}

		// we make a deep copy because some listeners might remove themselves while we are iterating
		for (Consumer<Command> cb : new ArrayList<>(commandListeners))
			cb.accept(cmd);
	}

	public void setActivePlayer(int player) {
		activePlayer = player;
		if (gameMenu != null)
			gameMenu.redraw();
	}
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
		horn |= playerData[ownerOfCard(card)].cheatHorn;
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
					playCardImpl(player, card, 12);
			}

			case FOLTEST_STEELFORGED -> {
				for (Card card : new ArrayList<>(weather))
					moveCard(card, playerData[ownerOfCard(card)].usedCards);
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
				List<List<Card>> add = new ArrayList<>();
				for (int i = 0; i < 6; i++)
					add.add(new ArrayList<>());
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
						add.get(mx).add(card);
					}
				}
				for (int i = 0; i < 6; i++) {
					for (Card card : add.get(i))
						moveCard(card, row.get(i));
				}
			}

			case CRACH_AN_CRAITE -> {
				for (int p = 0; p < 2; p++) {
					PlayerData data = playerData[p];

					for (Card c : new ArrayList<>(data.usedCards))
						moveCard(c, data.deck.getDeck());

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
