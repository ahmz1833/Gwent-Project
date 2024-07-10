package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MouseInputController implements InputController {
	private final List<Object> listeners = new ArrayList<>();
	private GameController controller;
	private int player;

	@Override
	public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
	}

	private void addListeners() {
		listeners.add(controller.getGameMenu().addButtonListener(this::buttonAction));
		listeners.add(controller.getGameMenu().addCardListener(this::cardAction));
		listeners.add(controller.getGameMenu().addRowListener(this::rowAction));
		listeners.add(controller.getGameMenu().addBgListener(this::bgAction));
	}

	private void removeListeners() {
		controller.getGameMenu().cancelPossiblePick();
		for (Object listener : listeners)
			controller.getGameMenu().removeListener(listener);
		listeners.clear();
	}

	@Override
	public void play() {
		controller.setActivePlayer(player);
		addListeners();
	}

	@Override
	public void end() {
		removeListeners();
	}

	@Override public boolean vetoShouldWait(InputController c) { return c instanceof MouseInputController; }

	@Override
	public void veto(int i) {
		if (i == 2) {
			controller.sendCommand(new Command.VetoCard(player, -1));
			return;
		}

		controller.setActivePlayer(player);
		controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, card -> {
			controller.sendCommand(new Command.VetoCard(player, card != null? card.getGameId(): -1));
		}, true);
	}

	@Override
	public void pick(List<Card> list, String what) {
		boolean nullPossible = what.equals("view_enemy_hand") || what.equals("cheat_enemy_hand");
		controller.getGameMenu().pickCard(list, card -> {
			controller.sendCommand(new Command.PickResponse(player, card != null ? card.getGameId() : -1, what));
		}, nullPossible);
	}

	private void bgAction(Object obj) {
		controller.sendCommand(new Command.SetActiveCard(player, -1));
	}

	private void rowAction(Object obj) {
		// when we get here the card can actually be placed there so no need to check
		int row = (int) obj;
		Card card = controller.getActiveCard();
		controller.sendCommand(new Command.PlayCard(player, card.getGameId(), row));
	}

	private void cardAction(Object obj) {
		Card card = (Card) obj;
		GameController.PlayerData data = controller.getPlayer(player);
		Card active = controller.getActiveCard();

		if (data.deck.getLeader() == card && !data.leaderUsed) {
			controller.sendCommand(new Command.PlayLeader(player));
			return;
		}

		if (active != null && controller.canSwap(player, active, card)) {
			controller.sendCommand(new Command.SwapCard(player, active.getGameId(), card.getGameId()));
			return;
		}

		if (!data.handCards.contains(card))
			return;

		if (active != card) {
			controller.sendCommand(new Command.SetActiveCard(player, card.getGameId()));
			return;
		}
	}

	private long lastReact = 0;

	private void buttonAction(Object obj) {
		String str = (String) obj;
		if (str.equals("pass")) {
			controller.sendCommand(new Command.Pass(player));
		}
		if (str.equals("resign")) {
			controller.sendCommand(new Command.Resign(player, "explicit"));
		}
		if (str.startsWith("cheat_")) {
			controller.sendCommand(new Command.Cheat(player, Integer.parseInt(str.substring(6))));
		}
		if (str.startsWith("react_") && System.currentTimeMillis() - lastReact > 2000 && controller.lastPlayed() != null) {
			lastReact = System.currentTimeMillis();
			controller.sendCommand(new Command.React(player, Integer.parseInt(str.substring(6))));
		}
	}
}
