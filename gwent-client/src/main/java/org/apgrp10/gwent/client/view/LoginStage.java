package org.apgrp10.gwent.client.view;

import com.google.gson.JsonObject;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;

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
		MFXButton btn = lookup("#register");
		User.RegisterInfo ureg = new User.RegisterInfo(
				new User.PublicInfo(666, "user", "nick", Avatar.random()),
				User.hashPassword("12344321"),
				"ahmz.ut@gmail.com",
				User.hashSecurityQ("What is your name?", "Ahmad"));
		btn.setOnMouseClicked(event -> {
			Server.send(new Request("register", (JsonObject) MGson.toJsonElement(ureg)), res -> {
				if (res.isOk()) {
					ANSI.log("Registered successfully");
				} else {
					ANSI.log("Failed to register");
				}
			});
		});
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
