package org.apgrp10.gwent.client;

import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.base.Theme;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.view.MainStage;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Gwent extends Application {
	
	public static final boolean USE_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	public static final String APP_DATA = System.getProperty("user.home") + "/.gwentdata/";
	
	public static void main(String[] args) {
		System.out.println(ANSI.LGREEN.bd() + "Hello! Welcome to Gwent Game (Made by AP Group 10) !" + ANSI.RST);
		Path path = Paths.get(APP_DATA);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
				System.out.println(ANSI.LYELLOW.bd() + "app directory created at: " + APP_DATA + ANSI.RST + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
