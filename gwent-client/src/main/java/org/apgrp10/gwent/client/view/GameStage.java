package org.apgrp10.gwent.client.view;

import javafx.stage.WindowEvent;

public class GameStage extends AbstractStage
{
	private static GameStage INSTANCE;
	
	private GameStage() {
		super("Gwent Game", null);  // TODO: icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of GameStage");
	}
	
	public static GameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new GameStage();
		return INSTANCE;
	}
	
	@Override
	protected boolean onCreate() {
		return false;
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
