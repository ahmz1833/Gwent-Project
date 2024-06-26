package org.apgrp10.gwent.controller;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.controller.GameController.PlayerData;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

public class MouseInputController implements InputController {
	private GameController controller;
	private int player;

	@Override
	public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
	}

	private final List<Object> listeners = new ArrayList<>();

	@Override
	public void beginTurn() {
		controller.setActivePlayer(player);
		listeners.clear();
		listeners.add(controller.getGameMenu().addButtonListener(this::buttonAction));
		listeners.add(controller.getGameMenu().addCardListener(this::cardAction));
		listeners.add(controller.getGameMenu().addRowListener(this::rowAction));
		listeners.add(controller.getGameMenu().addBgListener(this::bgAction));
	}

	@Override
	public void endTurn() {
		for (Object listener : listeners)
			controller.getGameMenu().removeListener(listener);
	}

	@Override
	public void endGame() {
	}

	private void bgAction(Object obj) {
		controller.sendCommand(new Command.SetActiveCard(player, null));
	}

	private void rowAction(Object obj) {
		int row = (int)obj;
		Card card = controller.getActiveCard();
		if (card == null || !controller.getPlayer(player).handCards.contains(card))
			return;
		if (!controller.canPlace(player, row, card))
			return;
		controller.sendCommand(new Command.PlayCard(player, card, row));
		controller.sendCommand(new Command.SetActiveCard(player, null));
	}

	private void cardAction(Object obj) {
		Card card = (Card)obj;
		if (controller.getActiveCard() != card) {
			controller.sendCommand(new Command.SetActiveCard(player, card));
			return;
		}

		PlayerData data = controller.getPlayer(player);
		if (data.deck.getDeck().contains(card))
			controller.sendCommand(new Command.MoveToHand(player, card));
	}

	private void buttonAction(Object obj) {
		String str = (String)obj;
		if (str.equals("hello")) {
			List<Card> deck = controller.getPlayer(player).deck.getDeck();
			if (!deck.isEmpty())
				controller.sendCommand(new Command.MoveToHand(player, deck.get(0)));
		}
	}
}
