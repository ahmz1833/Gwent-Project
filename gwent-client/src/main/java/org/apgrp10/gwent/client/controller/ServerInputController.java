package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class ServerInputController implements InputController {
	private GameController controller;
	private int player;

	private List<Command> commandQueue = new ArrayList<>();

	public void addCommand(Command cmd) {
		synchronized (commandQueue) {
			commandQueue.add(cmd);
		}
	}

	private boolean send = true;

	private void trySend() {
		if (!send)
			return;
		List<Command> copy;
		synchronized (commandQueue) {
			copy = new ArrayList<>(commandQueue);
			commandQueue.clear();
		}
		for (Command cmd : copy)
			controller.sendCommand(player, cmd);
		Platform.runLater(this::trySend);
	}

	@Override public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
		trySend();
	}

	@Override public void beginTurn() { }
	@Override public void endTurn() { }
	@Override public void pauseTurn() { }
	@Override public void resumeTurn() { }
	@Override public void endGame() { send = false; }
	@Override public void veto() { }
	@Override public void pick(List<Card> list, String what) { }
}
