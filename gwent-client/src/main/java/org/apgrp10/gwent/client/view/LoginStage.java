package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

public class LoginStage extends AbstractStage {
	
	private static LoginStage INSTANCE;
	
	private LoginStage() {
		super("Login Gwent", null);  // TODO: icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of LoginStage");
	}
	
	public static LoginStage getInstance() {
		if (INSTANCE == null) INSTANCE = new LoginStage();
		return INSTANCE;
	}
	
	@Override
	protected boolean onCreate() {
		Pane pane = new Pane();
		Scene scene = new Scene(pane);
		pane.setPrefWidth(400);
		pane.setPrefHeight(300);
		pane.getChildren().add(new MFXButton("Login"));
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
