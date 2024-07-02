package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;

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
		setScene(R.scene.login);
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
