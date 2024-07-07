package org.apgrp10.gwent.client.controller;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.model.ChatMenu;
import org.apgrp10.gwent.model.Message;
import org.apgrp10.gwent.model.User;

public class ChatMenuController {
	private final ChatMenu chatMenu;
	private final Scene scene;

	public ChatMenuController() {
		this.chatMenu = new ChatMenu(this);
		scene = new Scene(chatMenu);
		scene.getStylesheets().add(R.get("css/chat.css").toExternalForm());
	}
	public void show(Stage stage){
		stage.setScene(scene);
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
