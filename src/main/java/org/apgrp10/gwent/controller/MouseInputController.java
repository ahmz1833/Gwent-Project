package org.apgrp10.gwent.controller;

import java.util.List;

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

	Object listener;

	@Override
	public void beginTurn() {
		listener = controller.getGameMenu().addButtonListener(this::buttonAction);
	}

	@Override
	public void endTurn() {
		controller.getGameMenu().removeListener(listener);
	}

	@Override
	public void endGame() {
	}

	private void buttonAction(Object btn) {
		String str = (String)btn;
		if (str.equals("hello")) {
			List<Card> deck = controller.getPlayer(player).deck.getDeck();
			if (!deck.isEmpty())
				controller.sendCommand(new Command.MoveToHand(player, deck.get(0)));
		}
	}
}
