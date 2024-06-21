package org.apgrp10.gwent.controller;


import javafx.stage.Stage;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.PreGameMenu;

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
		//TODO deck 1 and 2 is ready. call game menu with stage and deck 1 and deck2
		// but know i have to exit the app
		System.exit(0);
	}
}
