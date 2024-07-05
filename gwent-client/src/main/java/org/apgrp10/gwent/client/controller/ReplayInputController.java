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

	private boolean run;

	private void doRunHelper() {
		for (;;) {
			if (cmdPos >= cmds.size()) {
				run = false;
				break;
			}
			Command cmd = cmds.get(cmdPos++);
			if (cmd.player() != player)
				continue;
			if (cmd instanceof Command.SetActiveCard) {
				if ((cmdPos == 1 || cmds.get(cmdPos - 2) instanceof Command.Sync)
						&& (cmdPos == cmds.size() || cmds.get(cmdPos) instanceof Command.Sync))
					cmdPos++;
				continue;
			}
			controller.sendCommand(cmd);
			if (!run)
				break;
			if (cmd instanceof Command.Sync) {
				run = false;
				doRun();
				break;
			}
		}
	}

	private void doRun() {
		if (controller.getGameMenu().isAnimationPlaying()) {
			Platform.runLater(this::doRun);
			return;
		}
		if (run)
			return;
		run = true;
		controller.waitExec.run(500, this::doRunHelper);
	}

	private void doRunPick() {
		for (;;) {
			if (controller.getGameMenu().isAnimationPlaying()) {
				Platform.runLater(this::doRunPick);
				return;
			}
			if (cmdPos >= cmds.size())
				break;
			Command cmd = cmds.get(cmdPos++);
			if (cmd.player() != player)
				continue;
			if (!(cmd instanceof Command.PickResponse || cmd instanceof Command.Sync)) {
				cmdPos--;
				break;
			}
			controller.sendCommand(cmd);
		}
	}

	private void doRunVeto() {
		for (;;) {
			if (cmdPos >= cmds.size())
				break;
			Command cmd = cmds.get(cmdPos++);
			if (cmd.player() != player)
				continue;
			if (!(cmd instanceof Command.VetoCard || cmd instanceof Command.Sync)) {
				cmdPos--;
				break;
			}
			controller.sendCommand(cmd);
		}
	}

	@Override public void beginTurn() { doRun(); }
	@Override public void endTurn() { run = false; }
	@Override public void pauseTurn() { run = false; }
	@Override public void resumeTurn() { doRun(); }
	@Override public void endGame() { run = false; }
	@Override public void veto() { doRunVeto(); }
	@Override public void pick(List<Card> list, String what) { doRunPick(); }
}
