package org.apgrp10.gwent.client.view;

import com.google.gson.JsonObject;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.SecurityUtils;

public class LoginStage extends AbstractStage {

	private static LoginStage INSTANCE;
	long toVerifyUser;

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
		MFXButton login = lookup("#login");
		MFXTextField username = lookup("#username");
		MFXPasswordField password = lookup("#password");
		MFXTextField email = lookup("#email");
		MFXTextField nickname = lookup("#nickname");
		MFXTextField code = lookup("#code");

		btn.setOnMouseClicked(event -> {
			User.RegisterInfo ureg = new User.RegisterInfo(
					new User.PublicInfo(0, username.getText(), nickname.getText(), Avatar.random()),
					User.hashPassword(password.getText()),
					email.getText(),
					User.hashSecurityQ("What is your name?", "Ahmad"));


			Server.send(new Request("register", (JsonObject) MGson.toJsonElement(ureg)), res -> {
				if (res.isOk()) {
					ANSI.log("Registered successfully");
				} else {
					ANSI.log("Failed to register " + res.getStatus());
				}
			});
		});

		login.setOnMouseClicked(event -> {
			if (toVerifyUser != 0) {
				JsonObject jsonn = MGson.makeJsonObject("userId", toVerifyUser, "code", code.getText());
				Server.send(new Request("verifyLogin", jsonn), res -> {
					if (res.isOk()) {
						ANSI.log("Verified successfully");
						// Print the JWT received from the server
						ANSI.log(res.getBody().get("jwt").getAsString());
					} else if (res.getStatus() == Response.INTERNAL_SERVER_ERROR)
						ANSI.printErrorResponse("Internal Server Error in verify login:", res);
				});
			} else {
				JsonObject jsonn = MGson.makeJsonObject("username", username.getText(), "passHash",
						User.hashPassword(password.getText()));
				Server.send(new Request("login", jsonn), res -> {
					if (res.isOk()) {
						ANSI.log("Code sent successfully");
						// received userID
						toVerifyUser = res.getBody().get("userId").getAsLong();
						ANSI.log("User ID: " + toVerifyUser);
					} else {
						ANSI.log("Failed to login, error code " + res.getStatus());
					}
				});
			}
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
