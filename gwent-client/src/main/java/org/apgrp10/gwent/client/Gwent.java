package org.apgrp10.gwent.client;

import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.base.Theme;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.utils.ANSI;

import java.util.Set;

public class Gwent extends Application {
	
	public static void main(String[] args) {
		System.out.println(ANSI.LGREEN.bd() + "Hello! Welcome to Gwent Game (Made by AP Group 10) !" + ANSI.RST);
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		// primaryStage.getIcons().add(R.icon.app_icon); // TODO: add icon
		primaryStage.setIconified(true);
		primaryStage.close();
		initMaterialTheme();
		MainStage.getInstance().start();
	}
	
	@Override
	public void stop() throws Exception {
		System.out.println();
		System.out.println(ANSI.LCYAN.bd() + "Goodbye! Thank you for playing Gwent!" + ANSI.RST);
		// TODO: disconnect from server
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void initMaterialTheme() {
		try {
			Class<?> themesEnum = Class.forName("io.github.palexdev.materialfx.theming.JavaFXThemes"),
					stylesEnum = Class.forName("io.github.palexdev.materialfx.theming.MaterialFXStylesheets");
			UserAgentBuilder.builder()
					.themes((Theme) Enum.valueOf((Class<Enum>) themesEnum, "MODENA"))
					.themes((Set<Theme>) stylesEnum.getMethod("forAssemble", boolean.class).invoke(null, true))
					.setDeploy(true)
					.setResolveAssets(true)
					.build()
					.setGlobal();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
