package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.User;

public class MainStage extends AbstractStage {
	private static MainStage INSTANCE;

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
		// TODO
		// if(!UserController.isLoggedIn())
		// {
		// 	LoginStage.getInstance().start();
		// 	return false;
		// }
		Pane pane = new Pane();
		Scene scene = new Scene(pane);
		pane.setPrefWidth(400);
		pane.setPrefHeight(300);
		MFXButton btn = new MFXButton("Salam");
		Platform.runLater(() -> {
			btn.setOnMouseClicked(event -> {
				User user2 = new User(2, "user2", "pass2", "nick2", "email2", "secQ2", null);
				User user1 = new User(1, "user1", "pass1", "nick1", "email1", "secQ1", null);
				new PreGameController(user1, user2);
				this.close();
			});
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
