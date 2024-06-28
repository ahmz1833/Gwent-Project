package org.apgrp10.gwent.client.view;

import javafx.scene.image.Image;
import javafx.stage.WindowEvent;

public class ProfileStage extends AbstractStage {
	
	private static ProfileStage INSTANCE;
	
	private ProfileStage() {
		super("Profile", null);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of ProfileStage");
	}
	
	public static ProfileStage getInstance() {
		if (INSTANCE == null) INSTANCE = new ProfileStage();
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
