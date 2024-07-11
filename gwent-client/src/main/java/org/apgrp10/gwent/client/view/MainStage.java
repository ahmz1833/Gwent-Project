package org.apgrp10.gwent.client.view;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.AvatarView;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;


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
			// TODO: show dialog
		});

		setOnPressListener("#gameBtn", event -> {
			Platform.runLater(this::close);
			PreGameStage.getInstance().setupChoice();
			PreGameStage.getInstance().start();
		});

		setOnPressListener("#replayBtn", event -> {
			String path = Utils.chooseFileToUpload("Choose recording", getInstance());
			if (path == null) {return;}

			GameRecord gr = MGson.fromJson(Utils.loadFile(path), GameRecord.class);
			Deck deck1 = gr.getDeck1();
			Deck deck2 = gr.getDeck2();
			User.PublicInfo publicInfo1 = new User.PublicInfo(1, "user1", "nick1", Avatar.random());
			User.PublicInfo publicInfo2 = new User.PublicInfo(2, "user2", "nick2", Avatar.random());
			long seed = gr.seed();

			GameStage.setCommonData(publicInfo1, publicInfo2, deck1, deck2, seed);
			GameStage.setReplay(0, gr.commands());
			GameStage.getInstance().start();
		});

		setOnPressListener("#liveBtn", event -> {
			// TODO: implement Dialog
		});

		setOnPressListener("#rankingsBtn", event -> ScoreboardStage.getInstance().start());

		return true;
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
