package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.AvatarView;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;


public class MainStage extends AbstractStage {
	private static MainStage INSTANCE;
	private boolean waitingForAuth = true;

	private MainStage() {
		super("Gwent Main", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of MainStage");
	}

	public static MainStage getInstance() {
		if (INSTANCE == null) INSTANCE = new MainStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		setScene(R.scene.main);
		updateInformation();

		setOnPressListener("#loggedInText", event -> {
			boolean logout = showConfirmDialog(Dialogs.WARN(), "Logout", "Are you sure you want to logout?", "Logout", "Cancel");
			if (logout) {
				UserController.logout();
				Platform.runLater(this::close);
				LoginStage.getInstance().start();
			}
		});

		setOnPressListener("#profileBtn", event -> ProfileStage.getInstance().start());

		setOnPressListener("#friendsBtn", event -> FriendshipStage.getInstance().start());

		setOnPressListener("#historyBtn", event -> showGameHistory());

		setOnPressListener("#gameBtn", event -> {
			Platform.runLater(this::close);
			PreGameStage.getInstance().setupChoice();
			PreGameStage.getInstance().start();
		});

		setOnPressListener("#replayBtn", event -> {
			String path = Utils.chooseFileToUpload("Choose recording", getInstance());
			if (path == null) return;
			GameRecord gr = MGson.fromJson(Utils.loadFile(path), GameRecord.class);
			AtomicInteger number = new AtomicInteger();
			PreGameController.startGame(gr.createReplayRequest(id -> (number.getAndIncrement() == 0) ?
					new User.PublicInfo(1, "player1", "Player 1", Avatar.random()) :
					new User.PublicInfo(2, "player2", "Player 2", Avatar.random())));
		});

		setOnPressListener("#liveBtn", event -> showCurrentGames());

		setOnPressListener("#rankingsBtn", event -> ScoreboardStage.getInstance().start());

		return true;
	}

	private void showGameHistory() {
		PreGameController.getMyDoneGameList(gameRecords -> {
			if (gameRecords.isEmpty()) {
				showAlert(Dialogs.INFO(), "Game History", "You have no games yet");
				return;
			}
			var l1 = gameRecords.values().stream().map(GameRecord::player1ID).toList();
			var l2 = gameRecords.values().stream().map(GameRecord::player2ID).toList();
			var l = new ArrayList<>(l1);
			l.addAll(l2);

			for (var entry : gameRecords.entrySet()) {
				ANSI.log("Game: " + entry.getKey());
				ANSI.log("Player1: " + entry.getValue().player1ID());
				ANSI.log("Player2: " + entry.getValue().player2ID());
				ANSI.log("");
			}
			UserController.cacheUserInfo(() -> showListDialog(gameRecords.entrySet().stream().toList(), entry -> {
				var p1Info = UserController.getCachedInfo(entry.getValue().player1ID());
				var p2Info = UserController.getCachedInfo(entry.getValue().player2ID());
				// TODO : time is milis in entry.getKey()
//				var time = Instant.ofEpochMilli(entry.getKey()).atOffset(ZoneOffset.of("+03:30")).toLocalDateTime();
				StringBuilder sb = new StringBuilder();
				sb.append(p1Info.nickname()).append(" (").append(p1Info.username()).append(") vs ");
				sb.append(p2Info.nickname()).append(" (").append(p2Info.username()).append(")");
				// TODO: handle multiline
//				sb.append("\n").append("Winner: ").append(entry.getValue().gameWinner() == 0 ? p1Info.nickname() : p2Info.nickname());
				return sb.toString();
			}, entry -> {
				boolean replay = showConfirmDialog(Dialogs.INFO(), "Game replay",
						"Do you want to replay this game?", "Yes", "No");
				if (!replay) return;
				PreGameController.replayGame(entry.getKey(), response -> {
					if (!response.isOk())
						showAlert(Dialogs.ERROR(), "Error", "Failed to replay the game");
				});
			}, "Game History"), false, l.toArray(new Long[0]));
		});
	}

	private void showCurrentGames() {
		PreGameController.getCurrentGames(gameInCurrents -> {
			if (gameInCurrents.isEmpty()) {
				showAlert(Dialogs.INFO(), "Game History", "No games found");
				return;
			}
			// cache all p1 and p2 s in gameInCurrents
			var l1 = gameInCurrents.stream().map(PreGameController.GameInCurrent::p1).toList();
			var l2 = gameInCurrents.stream().map(PreGameController.GameInCurrent::p2).toList();
			var l = new ArrayList<>(l1);
			l.addAll(l2);
			UserController.cacheUserInfo(() -> showListDialog(gameInCurrents, g -> {
				var p1 = UserController.getCachedInfo(g.p1());
				var p2 = UserController.getCachedInfo(g.p2());
				return "%s (%s)  vs  %s (%s)".formatted(p1.nickname(), p1.username(), p2.nickname(), p2.username());
			}, g -> PreGameController.attendLiveWatching(g, res -> {
				if (!res.isOk())
					showAlert(Dialogs.ERROR(), "Error", "Failed to attend the game");
			}), "Current Games"), false, l.toArray(new Long[0]));
		});
	}

	public <T> void showListDialog(List<T> list, Function<T, String> factory, Consumer<T> onItemClick, String title) {
		MFXListView<T> listView = new MFXListView<>();
		listView.setPrefWidth(600);
		listView.setPrefHeight(400);
		listView.setCellFactory(param -> {
			var cell = new MFXListCell<>(listView, param);
			cell.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2) onItemClick.accept(cell.getData());
			});
			return cell;
		});
		listView.setConverter(FunctionalStringConverter.to(factory));
		listView.setItems(FXCollections.observableList(list));
		listView.getStyleClass().add("list");
		StackPane container = new StackPane(listView);
		container.setPrefWidth(650);
		container.setPrefHeight(450);
		container.setAlignment(Pos.CENTER);
		Dialogs.showDialogAndWait(this, Dialogs.INFO(), title, container,
				Orientation.HORIZONTAL, Map.entry("*Close", k -> {}));
	}

	public boolean isWaitingForAuth() {
		return waitingForAuth;
	}

	@Override
	protected void updateInformation() {
		if (!Server.isConnected() || UserController.getCurrentUser() == null) {
			disable();
			waitingForAuth = true;
			return;
		}

		enable();
		waitingForAuth = false;

		//Set avatar
		this.<AvatarView>lookup("#avatar").setAvatar(UserController.getCurrentUser().avatar());

		// Set nickname
		this.<Label>lookup("#nickname").setText(UserController.getCurrentUser().nickname());

		// Set information
		UserController.getUserExperience(UserController.getCurrentUser().id(), experience -> {
			ANSI.log("Experience: " + experience);
			this.<Label>lookup("#info").setText("Rank: " + experience.rankByWins() + "\n" +
			                                    "Wins: " + experience.wins() + "\n" +
			                                    "Total: " + (experience.wins() + experience.losses() + experience.draws()));
		});

		// Set LoggedInText to "Logged in as: " + username
		this.<Label>lookup("#loggedInText").setText("Logged in as: " + UserController.getCurrentUser().username());
	}
}
