package org.apgrp10.gwent.client.view;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.ChatMenuController;
import org.apgrp10.gwent.client.model.ChatMenu;

import java.security.PrivateKey;
import java.util.PropertyPermission;

public class MessageStage extends AbstractStage{
	private static MessageStage instance;
	private MessageStage() {
		super("chat", R.getImage("chat/icon.jpeg"));
	}

	public static MessageStage getInstance(){
		if(instance == null)
			instance = new MessageStage();
		return instance;
	}
	@Override
	protected boolean onCreate() {
//		if(!GameStage.getInstance().isShowing())
//			return false;
		this.setWidth(300);
		this.setHeight(740);
		ChatMenuController controller = new ChatMenuController();
		controller.show(this);
		return true;
	}
}
