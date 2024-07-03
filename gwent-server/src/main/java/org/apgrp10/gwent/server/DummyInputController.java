package org.apgrp10.gwent.server;

import java.util.List;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.card.Card;

public class DummyInputController implements InputController {
	@Override public void start(GameController controller, int player) { }
	@Override public void beginTurn() { }
	@Override public void endTurn() { }
	@Override public void pauseTurn() { }
	@Override public void resumeTurn() { }
	@Override public void endGame() { }
	@Override public void veto() { }
	@Override public void pick(List<Card> list, String what) { }
}
