package org.apgrp10.gwent.client.view;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

public class PreGameStage extends AbstractStage {
	private static PreGameStage INSTANCE;
	private Pane pane;

	private PreGameStage() {
		super("PreGame Menu", null);// TODO: set icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of MainStage");
	}

	public static PreGameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new PreGameStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		Scene scene = new Scene(pane);
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

	public Pane getPane() {
		pane = new Pane();
		this.setWidth(PreGameMenu.screenWidth);
		this.setHeight(PreGameMenu.screenHeight);
		return pane;
	}
}
