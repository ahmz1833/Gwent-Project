package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.card.Card;

import java.util.List;

public class FFInputController implements InputController {
	private final InputController actualIC;

	public FFInputController(InputController actualInputController) { actualIC = actualInputController; }
	public InputController getActualInputController() { return actualIC; }

	public static enum State { NONE, PLAY, VETO, PICK }
	public State state = State.NONE;
	public int vetoI;
	public List<Card> pickList;
	public String pickWhat;

	@Override public void start(GameController controller, int player) { }
	@Override public void end() { state = State.NONE; }
	@Override public void play() { state = State.PLAY; }

	@Override public boolean vetoShouldWait(InputController c) {
		if (c instanceof FFInputController)
			return actualIC.vetoShouldWait(((FFInputController)c).actualIC);
		return actualIC.vetoShouldWait(c);
	}

	@Override public void veto(int i) {
		state = State.VETO;
		vetoI = i;
	}

	@Override
	public void pick(List<Card> list, String what) {
		state = State.PICK;
		pickList = list;
		pickWhat = what;
	}
}
