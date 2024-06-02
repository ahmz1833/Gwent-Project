package org.apgrp10.gwent;

import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import io.github.palexdev.materialfx.theming.*;
import javafx.stage.Stage;

public class MainWindow extends Application {
	public static void main(String[] args) {
		System.out.println("Hello world!");
		launch(args);
	}
	
	@Override
	public void start(Stage stage) {
		UserAgentBuilder.builder()
				.themes(JavaFXThemes.MODENA)
				.themes(MaterialFXStylesheets.forAssemble(true))
				.setDeploy(true).setResolveAssets(true).build().setGlobal();
		stage.setScene(R.scene.login);
		stage.show();
	}
}
