package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.card.Card;

import java.util.List;

public interface InputController {
	void start(GameController controller, int player);
	void play();
	void veto(int i);
	void pick(List<Card> list, String what);
	default void end() {}
	default boolean vetoShouldWait(InputController other) { return false; }
}
