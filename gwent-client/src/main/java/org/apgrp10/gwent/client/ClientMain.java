package org.apgrp10.gwent.client;

import javafx.stage.Window;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.client.view.LoginStage;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.WaitExec;

import java.util.ArrayList;
import java.util.Locale;

public class ClientMain {
	private static final WaitExec connectionTimer = new WaitExec(false);

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		Server.connectionProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue) onConnect();
			else onDisconnect();
		});
		Gwent.main(args);
	}

	private static void onConnect() {
		ANSI.log("Connected to server", ANSI.LGREEN, false);
		LoginStage.getInstance().connectionEstablished();
		UserController.performAuthentication(true);
	}

	private static void onDisconnect() {
		ANSI.log("Disconnected from server", ANSI.LRED, false);
		if (UserController.loadJWTFromFile() == null && !LoginStage.getInstance().isShowing())
			LoginStage.getInstance().start();
		UserController.onDisconnect();
		Gwent.forEachAbstractStage(AbstractStage::connectionLost);
		connectionTimer.run(2500, ClientMain::connect);
	}

	public static void connect() {
		Server.connect();
		Server.run();
	}
}
