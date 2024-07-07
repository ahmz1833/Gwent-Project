package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.model.ChatPane;
import org.apgrp10.gwent.model.Message;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Random;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatMenuController {
	private ChatPane chatMenu;
	private Scene scene;
	private static final ChatMenuController instance = new ChatMenuController();

	private ChatMenuController() {}

	private void reset() {
		this.chatMenu = new ChatPane(this);
		scene = new Scene(chatMenu);
		scene.getStylesheets().add(R.get("css/chat.css").toExternalForm());

		Server.setListener("chatMessage", req -> {
			getMessage(req.getBody().get("msg").getAsString());
			return req.response(Response.OK_NO_CONTENT);
		});
	}

	public static void stop() {
		Server.setListener("chatMessage", null);
	}

	public static ChatMenuController cleanInstance() {
		instance.reset();
		return instance;
	}

	public void show(Stage stage){
		stage.setScene(scene);
	}

	public long getId() {
		return Random.nextId();
	}

	public void sendMessage(Message message) {
		Server.send(new Request("chatMessage", MGson.makeJsonObject("msg", message.toString())));
	}

	public void getMessage(String str) {
		chatMenu.addMessage(str);
	}
}
