package org.apgrp10.gwent.client.controller;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

import javafx.application.Platform;

public class ReplayInputController implements InputController {
	private GameController controller;
	private int player;
	private List<Command> cmds;
	private int cmdPos = 0;

	public ReplayInputController(List<Command> cmds) {
		this.cmds = new ArrayList<>(cmds);
	}

	@Override public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
	}

	private void runOne() {
		if (controller.getGameMenuNonnull().isAnimationPlaying()) {
			Platform.runLater(this::runOne);
			return;
		}

		for (;;) {
			Command cmd = cmds.get(cmdPos++);
			if (cmd.player() != player)
				continue;
			controller.sendCommand(cmd);
			return;
		}
	}

	@Override public void veto(int i) { runOne(); }
	@Override public void pick(List<Card> list, String what) { runOne(); }
	@Override public void play() { runOne(); }
}
