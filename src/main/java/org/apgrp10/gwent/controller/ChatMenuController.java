package org.apgrp10.gwent.controller;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.ChatMenu;

public class ChatMenuController {
	private final ChatMenu chatMenu;
	private final User user;
	private final Pane pane;

	public ChatMenuController(Pane pane, User user, double screenWidth) {
		this.chatMenu = new ChatMenu(screenWidth);
		this.user = user;
		this.pane = pane;
	}

	public void show() {
		pane.getChildren().add(chatMenu);
	}

	public void endShow() {
		pane.getChildren().remove(chatMenu);
	}

}
