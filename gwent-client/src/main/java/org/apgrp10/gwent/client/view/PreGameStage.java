package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.WaitExec;

import java.util.Map;

public class PreGameStage extends AbstractStage {
	private static PreGameStage INSTANCE;
	private Pane pane;
	private Deck lastDeck;
	private GameMode gameMode = GameMode.CHOICE;
	private boolean isPublic = false;

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

	public void setupFriendMode(boolean isPublic) {
		gameMode = GameMode.FRIEND;
		this.isPublic = isPublic;
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
				lastDeck = deck;
				Dialogs.showDialogAndWait(this, Dialogs.WARN(), "choose", "How you want to play?\n", Orientation.VERTICAL,
						Map.entry("Make an Offline play", e -> {
							gameMode = GameMode.LOCAL;
							new PreGameMenu(pane, false, new User.PublicInfo(0, "anonymous", "anonymous", Avatar.random()));
						}),
						Map.entry("Make an online play with a friend", e -> {
							// choose a friend, send a request (contains deck) to server, and wait for response
							Platform.runLater(this::chooseFriendForPlaying);
						}),
						Map.entry("Make an online play with a random user", e -> {
							// send a request of random play to server (contains deck) and wait for response
							Platform.runLater(() -> requestForPlay(-1, "a random user"));
						}));
			}
			case LOCAL -> {
				GameStage.setCommonData(
						UserController.getCurrentUser().publicInfo(),
						new User.PublicInfo(0, "anonymous", "anonymous", Avatar.random()),
						lastDeck,
						deck,
						System.currentTimeMillis()
				);
				GameStage.setLocal();
				GameStage.getInstance().start();
				this.close();
			}
			case FRIEND -> {
				// RequestPlay with our own id means accepting the friend playRequest
				PreGameController.requestPlay(deck, UserController.getCurrentUser().id(), isPublic, res -> {
					if (!res.isOk()) {
						ANSI.log("Failed to start game: " + res.getStatus());
						if (res.getStatus() == Response.NOT_FOUND)
							showAlert(Dialogs.ERROR(), "Failed to start game", "Your friend is offline, or possibly canceled the request.");
						else
							showAlert(Dialogs.ERROR(), "Failed to start game", "Failed to start game with friend");
					}

				});
			}
		}
	}

	private void chooseFriendForPlaying() {
		// TODO: show a dialog listing and searching friends and send a request to the selected friend


		long testTarget = 184837367028312630L;
		requestForPlay(testTarget, "'online2'");
	}

	private void requestForPlay(long target, String showingName) {
		isPublic = target == -1 ||
		           showConfirmDialog(Dialogs.INFO(), "Make Game Public", "Do you want to make this game public?", "Yes", "No");

		PreGameController.requestPlay(lastDeck, target, isPublic, res -> {
			if (res.isOk()) {
				Platform.runLater(() -> {
					WaitExec waitForPlayDialogLoop = new WaitExec(false);
					waitForPlayDialogLoop.run(100, new Runnable() {
						@Override
						public void run() {
							int state = PreGameController.getLastRequestState();
							if (state == PreGameController.WAITING) {
								showDialogAndWait(Dialogs.INFO(), "Waiting for opponent", "Waiting for " + showingName + " to join the game ...",
										Map.entry("Cancel", e -> PreGameController.cancelPlayRequest()));
								waitForPlayDialogLoop.run(500, this);
							} else if (state == PreGameController.CANCELED)
								showAlert(Dialogs.INFO(), "Play Request Canceled", "Your play request has been canceled.");
							else if (state == PreGameController.DECLINED)
								showAlert(Dialogs.WARN(), "Play Request Declined", showingName + " declined your play request!");
						}
					});
				});
			} else {
				ANSI.log("Failed to start game: " + res.getStatus());
				if (res.getStatus() == Response.NOT_FOUND)
					showAlert(Dialogs.ERROR(), "Failed to start game", "Your friend is offline.");
				else if (res.getStatus() == Response.CONFLICT)
					showAlert(Dialogs.ERROR(), "Failed to start game", "Your friend is already has a play request, or is in a game.");
				else
					showAlert(Dialogs.ERROR(), "Failed to start game", "Failed to start game with friend");
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
