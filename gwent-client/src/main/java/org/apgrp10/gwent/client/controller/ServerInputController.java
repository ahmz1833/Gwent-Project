package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

import java.util.List;

public class ServerInputController implements InputController {
	private GameController controller;
	private int player;

	public void sendCommand(Command cmd) {
		controller.sendCommand(player, cmd);
	}

	@Override public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
	}

	@Override public void beginTurn() { }
	@Override public void endTurn() { }
	@Override public void pauseTurn() { }
	@Override public void resumeTurn() { }
	@Override public void endGame() { }
	@Override public void veto() { }
	@Override public void pick(List<Card> list, String what) { }
}
