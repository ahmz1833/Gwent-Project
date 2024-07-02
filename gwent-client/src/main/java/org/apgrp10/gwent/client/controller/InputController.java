package org.apgrp10.gwent.client.controller;

import java.util.List;

import org.apgrp10.gwent.model.card.Card;

public interface InputController {
	public void start(GameController controller, int player);
	public void beginTurn();
	public void endTurn();
	public void pauseTurn();
	public void resumeTurn();
	public void endGame();
	public void veto();
	public void pick(List<Card> list, String what);
}
