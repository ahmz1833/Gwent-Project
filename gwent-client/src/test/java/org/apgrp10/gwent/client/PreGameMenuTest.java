package org.apgrp10.gwent.client;

import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.model.Deck;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class PreGameMenuTest {
	@Test
	public void canclePlaying(){
		PreGameController.randomPlayRequest(Objects.requireNonNull(Deck.loadDeckFromFile(R.getAbsPath("primaryDeck.gwent"))),
				null);
		PreGameController.cancelRandomPlayRequest();
		Assertions.assertFalse(PreGameController.isWaitingForOpponent());
	}
}
