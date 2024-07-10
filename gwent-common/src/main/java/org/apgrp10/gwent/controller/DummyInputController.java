package org.apgrp10.gwent.controller;

import java.util.List;

import org.apgrp10.gwent.model.card.Card;

public class DummyInputController implements InputController {
	@Override public void start(GameController controller, int player) { }
	@Override public void play() { }
	@Override public void veto(int i) { }
	@Override public void pick(List<Card> list, String what) { }
}
