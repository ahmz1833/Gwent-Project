package org.apgrp10.gwent.controller;

public interface InputController {
	public void start(GameController controller, int player);
	public void beginTurn();
	public void endTurn();
	public void endGame();
}
