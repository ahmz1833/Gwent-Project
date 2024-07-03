package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;

public class MainStage extends AbstractStage {
	private static MainStage INSTANCE;
	private int playerId = -1;
	private boolean start;

	private MainStage() {
		super("Gwent Main", null);// TODO: set icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of MainStage");
	}

	public static MainStage getInstance() {
		if (INSTANCE == null) INSTANCE = new MainStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		Pane pane = new Pane();
		Scene scene = new Scene(pane);
		pane.setPrefWidth(400);
		pane.setPrefHeight(300);
		MFXButton btn = new MFXButton("Salam");
		Server.instance().sendRequest(new Request("fastPlay"), res -> {
			playerId = res.getBody().get("player").getAsInt();
			System.out.println("we are player " + playerId);
		});
		Server.instance().setListener("makeDeck", req -> {
			start = true;
			Server.instance().sendResponse(new Response(req.getId(), 200));
			Server.instance().setListener("makeDeck", null);
		});
		btn.setOnMouseClicked(event -> {
			if (!start)
				return;
			this.close();
			User user2 = new User(2, new User.UserInfo("user2", "pass2", "nick2", "email2", "secQ2", null));
			User user1 = new User(1, new User.UserInfo("user1", "pass1", "nick1", "email1", "secQ1", null));
			new PreGameController(user1, user2, playerId != 1, playerId != 0);
		});
		pane.getChildren().add(btn);
		setScene(scene);
		return true;
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {

	}

	@Override
	protected void onGetFocus() {

	}

	@Override
	protected void onLostFocus() {

	}
}
