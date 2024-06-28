package org.apgrp10.gwent.client.controller;


import javafx.stage.Stage;
import org.apgrp10.gwent.client.view.PreGameMenu;
import org.apgrp10.gwent.client.model.Deck;
import org.apgrp10.gwent.model.User;

public class PreGameController  {
	private final Stage stage;
	private final User user1, user2;
	private Deck deck1 = null;
	private Deck deck2 = null;
	public PreGameController(User user1, User user2, Stage stage){
		this.stage = stage;
		this.user1 = user1;
		this.user2 = user2;
		new PreGameMenu(this, stage, true, user1);
	}
	public void setDeck1(Deck deck){
		deck1 = deck;
		new PreGameMenu(this, stage, false, user2);
	}
	public void setDeck2(Deck deck){
		deck2 = deck;
		System.gc();
		// TODO: deck 1 and 2 is ready. call game menu with stage and deck 1 and deck2
		// for now we just set a random game up;
		InputController c1 = new MouseInputController(), c2 = new MouseInputController();
		new GameController(stage, c1, c2, deck1, deck2, System.currentTimeMillis());
	}
}
