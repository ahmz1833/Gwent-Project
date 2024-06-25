package org.apgrp10.gwent;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apgrp10.gwent.controller.ChatMenuController;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.ChatMenu;

public class MainWindow extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Pane pane = new Pane();
		pane.setPrefWidth(1024);
		pane.setPrefHeight(720);
		stage.setResizable(false);
		stage.setScene(new Scene(pane));
		ChatMenuController controller = new ChatMenuController(pane,
				new User("a", "a", "a", "a"), 1024);
		controller.show();
		stage.show();
	}
}
