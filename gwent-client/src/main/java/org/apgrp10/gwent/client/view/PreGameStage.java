package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.MouseInputController;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.ServerInputController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import java.util.Map;

public class PreGameStage extends AbstractStage {
	private static PreGameStage INSTANCE;
	private Pane pane;
	private Deck deck1, deck2;
	private GameMode gameMode = GameMode.CHOICE;

	private PreGameStage() {
		super("PreGame Menu", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of PreGameMenu");
	}

	public static PreGameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new PreGameStage();
		return INSTANCE;
	}

	public void setupChoice() {
		gameMode = GameMode.CHOICE;
	}

	public void setupFriendMode() {
		gameMode = GameMode.FRIEND;
	}

	@Override
	protected boolean onCreate() {
		this.setWidth(PreGameMenu.screenWidth);
		this.setHeight(PreGameMenu.screenHeight);
		pane = new Pane();
		setScene(new Scene(pane));
		pane.getStylesheets().add(R.get("css/preGame.css").toExternalForm());

		switch (gameMode) {
			case CHOICE -> new PreGameMenu(pane, true, UserController.getCurrentUser().publicInfo());
			case LOCAL -> new PreGameMenu(pane, false, new User.PublicInfo(0,
					"anonymous", "anonymous", Avatar.random()));
			case FRIEND -> new PreGameMenu(pane, false, UserController.getCurrentUser().publicInfo());
		}
		return true;
	}

	// This method will be called when we click on Start game button (And the deck is correct)
	public void startClicked(Deck deck) {
		switch (gameMode) {
			case CHOICE -> {
				deck1 = deck;
				Dialogs.showDialogAndWait(this, MFXDialogs.warn(), "choose", "How you want to play?\n", Orientation.VERTICAL,
						Map.entry("Make an Offline play", e -> {
							gameMode = GameMode.LOCAL;
							new PreGameMenu(pane, false, UserController.getCurrentUser().publicInfo());
						}),
						Map.entry("Make an online play with a friend", e -> {
							// TODO: choose a friend, send a request (contains deck) to server, and wait for response
						}),
						Map.entry("Make an online play with a random user", e -> {
							// send a request of random play to server (contains deck) and wait for response
							Platform.runLater(this::randomPlayingRequest);
						}));
			}
			case LOCAL -> {
				deck2 = deck;
				GameStage.setCommonData(
						UserController.getCurrentUser().publicInfo(),
						new User.PublicInfo(0, "anonymous", "anonymous", Avatar.random()),
						deck1,
						deck2,
						System.currentTimeMillis()
				);
				GameStage.setLocal();
				GameStage.getInstance().start();
				this.close();
			}
			case FRIEND -> {

			}
		}
	}

	private void randomPlayingRequest() {
		PreGameController.randomPlayRequest(deck1, res -> {
			if (res.isOk()) {
				Platform.runLater(()->{
					do {
						showDialogAndWait(Dialogs.INFO(), "Waiting for opponent", "Waiting for a random opponent to join the game ...",
								Map.entry("Cancel", e -> {
									PreGameController.cancelRandomPlayRequest();
									setupChoice();
									onCreate();
								}));
					} while (PreGameController.isWaitingForOpponent());
				});
			} else {
				ANSI.log("Failed to start game: " + res.getStatus());
				showAlert(Dialogs.ERROR(), "Failed to start game", "Failed to start game with random opponent");
			}
		});
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
		super.onCloseRequest(event);
		gameMode = GameMode.CHOICE;
	}

	private enum GameMode {CHOICE, LOCAL, FRIEND}
}
