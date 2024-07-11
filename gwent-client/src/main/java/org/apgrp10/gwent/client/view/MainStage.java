package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.AvatarView;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Random;
import org.apgrp10.gwent.utils.Utils;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.List;
import java.util.Map;
import java.util.Stack;


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

		setOnPressListener("#historyBtn", event -> {
			ObservableList<String> names = FXCollections.observableArrayList("Engineering", "MCA", "MBA", "Graduation", "MTECH", "Mphil", "Phd", "a", "b", "c", "d", "e", "f", "h", "k");
			showListView(names);
			// TODO: show dialog
		});

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

		setOnPressListener("#liveBtn", event -> {
			// TODO: implement Dialog
		});

		setOnPressListener("#rankingsBtn", event -> ScoreboardStage.getInstance().start());

		return true;
	}

	public void showListView(ObservableList<String> list){
		MFXListView<String > listView = new MFXListView<>(list);
		listView.setPrefWidth(600);
		listView.setPrefHeight(400);
		listView.getStyleClass().add("list");
		StackPane container = new StackPane(listView);
		container.setPrefWidth(650);
		container.setPrefHeight(450);
		container.setAlignment(Pos.CENTER);
		Dialogs.showDialogAndWait(this, MFXDialogs.info(), "game history", container, Orientation.HORIZONTAL, Map.entry("cancel", k -> {})
				, Map.entry("select", k -> {
					if(!listView.getSelectionModel().getSelection().isEmpty())
						System.out.println(listView.getSelectionModel().getSelection());
				}));
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
