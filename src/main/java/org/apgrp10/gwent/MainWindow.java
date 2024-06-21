package org.apgrp10.gwent;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apgrp10.gwent.controller.PreGameController;
import org.apgrp10.gwent.model.User;

public class MainWindow extends Application {
	@Override
	public void start(Stage stage) {
		stage.show();
		//creat an instance of PreGameController to show it.
		new PreGameController(new User("a", "a", "a", "a"),
				new User("b", "b", "b", "b"), stage);
	}
}
