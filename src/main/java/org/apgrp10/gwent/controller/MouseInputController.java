package org.apgrp10.gwent.controller;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.controller.GameController.PlayerData;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.WaitExec;
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

	@Override
	public void veto() {
		// TODO: this function is pure shit! maybe do something better?
		// also note that if it's a local game player 2 needs to wait for player 1 before choosing

		org.apgrp10.gwent.view.GameMenu.Callback pickCb = obj1 -> {
			Card card1 = (Card)obj1;
			if (card1 == null) {
				new WaitExec(1000, () -> {
					controller.sendCommand(new Command.VetoCard(player, -1));
					controller.sendCommand(new Command.Sync());
				});
				return;
			}
			controller.sendCommand(new Command.VetoCard(player, card1.getGameId()));
			controller.sendCommand(new Command.Sync());

			controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, obj2 -> {
				Card card2 = (Card)obj2;
				if (card2 != null) {
					controller.sendCommand(new Command.VetoCard(player, card2.getGameId()));
					controller.sendCommand(new Command.Sync());
				}

				new WaitExec(1000, () -> {
					controller.sendCommand(new Command.VetoCard(player, -1));
					controller.sendCommand(new Command.Sync());
				});
			}, true);
		};

		if (player == 1 && controller.getPlayer(0).controller instanceof MouseInputController) {
			controller.addCommandListener(new GameController.CommandListener() {
				@Override
				public void call(Command cmd) {
					if (!controller.getPlayer(0).vetoDone)
						return;
					controller.removeCommandListener(this);
					controller.setActivePlayer(player);
					controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, pickCb, true);
				}
			});
			return;
		}

		controller.setActivePlayer(player);
		controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, pickCb, true);
	}

	@Override
	public void reviveCard() {
		controller.getGameMenu().pickCard(controller.getPlayer(player).usedCards, obj -> {
			Card card = (Card)obj;
			int row = 0;

			// TODO: what happens when an agile card gets revived?
			while (!controller.canPlace(player, row, card)) row++;

			controller.sendCommand(new Command.PlayCard(player, card.getGameId(), row));
			controller.sendCommand(new Command.Sync());
		}, false);
	}

	private void bgAction(Object obj) {
		controller.sendCommand(new Command.SetActiveCard(player, -1));
		controller.sendCommand(new Command.Sync());
	}

	private void rowAction(Object obj) {
		// when we get here the card can actually be placed there so no need to check
		int row = (int)obj;
		Card card = controller.getActiveCard();
		controller.sendCommand(new Command.PlayCard(player, card.getGameId(), row));
		controller.sendCommand(new Command.SetActiveCard(player, -1));
		controller.sendCommand(new Command.Sync());
	}

	private void cardAction(Object obj) {
		Card card = (Card)obj;
		PlayerData data = controller.getPlayer(player);
		Card active = controller.getActiveCard();

		if (active != null && controller.canSwap(player, active, card)) {
			controller.sendCommand(new Command.SwapCard(player, active.getGameId(), card.getGameId()));
			controller.sendCommand(new Command.SetActiveCard(player, -1));
			controller.sendCommand(new Command.Sync());
			return;
		}

		if (!data.handCards.contains(card))
			return;

		if (active != card) {
			controller.sendCommand(new Command.SetActiveCard(player, card.getGameId()));
			controller.sendCommand(new Command.Sync());
			return;
		}
	}

	private void buttonAction(Object obj) {
		String str = (String)obj;
		if (str.equals("hello")) {
			List<Card> deck = controller.getPlayer(player).deck.getDeck();
			if (!deck.isEmpty())
				controller.sendCommand(new Command.MoveToHand(player, deck.get(0).getGameId()));
		}
		if (str.equals("pass"))
			controller.sendCommand(new Command.Pass(player));
		controller.sendCommand(new Command.Sync());
	}
}
