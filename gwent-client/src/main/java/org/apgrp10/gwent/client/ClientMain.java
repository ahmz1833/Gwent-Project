package org.apgrp10.gwent.client;

import javafx.stage.Window;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.client.view.LoginStage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.utils.ANSI;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ClientMain {
	private static final Timer connectionTimer = new Timer("connectionTimer", true);

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		Server.connect();
		Server.connectionProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue) onConnect();
			else onDisconnect();
		});
		Gwent.main(args);
	}

	public static void performAuthentication() {
		if (Server.isConnected()) {
			UserController.authenticate(response -> {
				if (response.isOk()) {
					ANSI.log("Authenticated; Username: " + UserController.getCurrentUser().publicInfo().username(), ANSI.LGREEN, false);
					for (Window window : new ArrayList<>(Window.getWindows()))
						if (window instanceof AbstractStage stage)
							stage.connectionEstablished();
					if (MainStage.getInstance().isWaitingForAuth())
						MainStage.getInstance().start();
				} else {
					ANSI.log("Failed to authenticate, Please Login again.", ANSI.LRED, false);
					for (Window window : new ArrayList<>(Window.getWindows()))
						if (window instanceof AbstractStage stage)
							stage.close();
					if (!LoginStage.getInstance().isShowing())
						LoginStage.getInstance().start();
				}
			});
		}
	}

	private static void onConnect() {
		ANSI.log("Connected to server", ANSI.LGREEN, false);
		performAuthentication();
	}

	private static void onDisconnect() {
		ANSI.log("Disconnected from server", ANSI.LRED, false);
		if (UserController.loadJWTFromFile() == null && !LoginStage.getInstance().isShowing())
			LoginStage.getInstance().start();
		UserController.onDisconnect();
		for (Window window : new ArrayList<>(Window.getWindows()))
			if (window instanceof AbstractStage stage)
				stage.connectionLost();
		connectionTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Server.connect();
				Server.run();
			}
		}, 500);
	}
}
