package org.apgrp10.gwent.client;

import javafx.stage.Window;
import org.apgrp10.gwent.client.view.AbstractStage;
import org.apgrp10.gwent.utils.ANSI;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ClientMain {
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		Server.connect();
		Server.connectionProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				ANSI.log("Connected to server", ANSI.LGREEN, false);
				for (Window window : Window.getWindows())
					if (window instanceof AbstractStage stage)
						stage.connectionEstablished();
			} else {
				ANSI.log("Disconnected from server", ANSI.LRED, false);
				for (Window window : Window.getWindows())
					if (window instanceof AbstractStage stage);
						// stage.connectionLost(); // TODO: temporary disabled
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						Server.connect();
						Server.run();
					}
				}, 500);
			}
		});

		Gwent.main(args);
	}
}
