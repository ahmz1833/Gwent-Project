package org.apgrp10.gwent.client;

import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.base.Theme;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.TerminalAsyncReader;
import org.apgrp10.gwent.client.view.*;
import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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

	public static void exit() {
		Platform.runLater(() -> {
			forEachStage(Stage::close);
			Server.disconnect();
			Platform.exit();
		});
	}

	public static void forEachStage(Consumer<Stage> action) {
		HashSet<Stage> toApply = new HashSet<>();
		for (Window window : new ArrayList<>(Window.getWindows()))
			if (window instanceof Stage stage)
				toApply.add(stage);
		toApply.add(LoginStage.getInstance());
		toApply.add(MainStage.getInstance());
		toApply.add(ProfileStage.getInstance());
		toApply.add(FriendshipStage.getInstance());
		toApply.add(ScoreboardStage.getInstance());
		toApply.add(PreGameStage.getInstance());
		toApply.add(GameStage.getInstance());
		toApply.add(MessageStage.getInstance());
		toApply.forEach(action);
	}

	public static void forEachAbstractStage(Consumer<AbstractStage> action) {
		forEachStage(stage -> {
			if(stage instanceof AbstractStage abstractStage)
				action.accept(abstractStage);
		});
	}

	@Override
	public void start(Stage primaryStage) {
		TerminalAsyncReader.instanceRun();
		primaryStage.getIcons().add(R.icon.app_icon);
		primaryStage.setIconified(true);
		primaryStage.close();
		initMaterialTheme();
		if (UserController.loadJWTFromFile() == null)
			LoginStage.getInstance().start();
		else
			MainStage.getInstance().start();

		ClientMain.connect(); // Start the connection javafx is fully ready
	}

	@Override
	public void stop() throws Exception {
		System.out.println();
		System.out.println(ANSI.LCYAN.bd() + "Goodbye! Thank you for playing Gwent!" + ANSI.RST);
		Server.disconnect();
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
