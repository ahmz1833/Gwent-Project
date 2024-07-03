package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.card.Card;

import java.util.List;

public interface InputController {
	void start(GameController controller, int player);
	void beginTurn();
	void endTurn();
	void pauseTurn();
	void resumeTurn();
	void endGame();
	void veto();
	void pick(List<Card> list, String what);
}
