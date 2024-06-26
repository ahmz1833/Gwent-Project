package org.apgrp10.gwent.controller;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.model.Massage.Message;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.ChatMenu;

public class ChatMenuController {
	private final ChatMenu chatMenu;
	private final Pane pane;

	public ChatMenuController(Pane pane, User user, double screenWidth, Scene scene) {
		scene.getStylesheets().add(R.class.getResource("css/chat.css").toExternalForm());
		this.chatMenu = new ChatMenu(screenWidth, this, user);
		this.pane = pane;
	}

	public void show() {
		pane.getChildren().add(chatMenu);
	}

	public void endShow() {
		pane.getChildren().remove(chatMenu);
	}
	private int lastId = 0;
	public int getId(){
		//TODO server should send a uniq id (synchronized) instead of calculating locally
		//index should be ( > 0) && ( != 0)
		return ++lastId;
	}
	public void sendMessage(Message message){
		getMessage(message);
		//TODO should send this message to server instead of calling getMessage
		//server should call all users "getMessage(message)"
	}
	public void getMessage(Message message){
		chatMenu.addMessage(message);
	}

}
