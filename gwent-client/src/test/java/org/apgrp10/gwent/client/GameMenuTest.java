package org.apgrp10.gwent.client;

import org.apgrp10.gwent.client.controller.ReplayInputController;
import org.apgrp10.gwent.client.view.GameMenu;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.MGson;
import org.junit.jupiter.api.Test;

public class GameMenuTest {

	private void testSample(String name) throws Exception {
		GameRecord gr = MGson.fromJson(new String(R.getAsStream(name).readAllBytes()), GameRecord.class);
		Deck deck1 = Deck.fromJsonString(gr.deck1());
		Deck deck2 = Deck.fromJsonString(gr.deck2());
		User.PublicInfo publicInfo1 = new User.PublicInfo(gr.player1ID(), "user1", "nick1", Avatar.random());
		User.PublicInfo publicInfo2 = new User.PublicInfo(gr.player2ID(), "user2", "nick2", Avatar.random());
		long seed = gr.seed();

		GameController gameController = new GameController(
				new ReplayInputController(gr.commands()),
				new ReplayInputController(gr.commands()),
				publicInfo1,
				publicInfo2,
				deck1,
				deck2,
				seed,
				new GameMenu(null, false),
				gr2 -> { assert gr.equals(gr2); },
				0,
				false,
				null,
				true
		);
	}

	@Test
	public void test() throws Exception {
		testSample("samplegame1.gwent");
		testSample("samplegame2.gwent");
	}
}
