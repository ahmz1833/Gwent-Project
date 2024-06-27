package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.controller.UserController;

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
	protected boolean onCreate(){
		if(!UserController.isLoggedIn())
		{
			LoginStage.getInstance().start();
			return false;
		}
		Pane pane = new Pane();
		Scene scene = new Scene(pane);
		pane.getChildren().add(new MFXButton("Logout"));
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
