package org.apgrp10.gwent.client.view;

import javafx.stage.Stage;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.ChatMenuController;
import org.apgrp10.gwent.utils.WaitExec;

public class MessageStage extends AbstractStage {
	private static MessageStage instance;
	private ChatMenuController controller;

	private MessageStage() {
		super("chat", R.getImage("ic_chat.png"));
		controller = ChatMenuController.cleanInstance();
	}

	public static MessageStage getInstance() {
		if (instance == null)
			instance = new MessageStage();
		return instance;
	}

	@Override
	protected boolean onCreate() {
		GameStage.getInstance().xProperty().addListener((obs, oldVal, newVal) -> {
			this.setX(newVal.doubleValue() + GameStage.getInstance().getWidth() + 5);
		});
		GameStage.getInstance().yProperty().addListener((obs, oldVal, newVal) -> {
			this.setY(newVal.doubleValue());
		});
		GameStage.getInstance().setOnCloseRequest(e -> this.close());
		setOnCloseRequest(e -> setPlaceGameWithoutChat());
		setPlaceGameWithChat();
		new WaitExec(false).run(50, () -> {
			MessageStage.instance.setX(GameStage.getInstance().getX() + GameStage.getInstance().getWidth() + 5);
			MessageStage.instance.setY(GameStage.getInstance().getY());
		});
		this.setWidth(250);
		this.setHeight(720);
		controller.show(this);
		return GameStage.getInstance().isShowing();
	}

	private void setPlaceGameWithChat() {
		Stage primaryStage = GameStage.getInstance();
		primaryStage.setX(primaryStage.getX() - 125);
	}

	private void setPlaceGameWithoutChat() {
		Stage primaryStage = GameStage.getInstance();
		primaryStage.setX(primaryStage.getX() + 125);
	}
	public static void deleteInstance(){
		if (instance != null) {
			instance.controller.stop();
			instance = null;
		}
	}
}