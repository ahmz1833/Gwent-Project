package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.ReplayInputController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
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
		MFXButton gameBtn, replayBtn, playingBtn, profileBtn, friendsBtn, rankingsBtn;

		setScene(R.scene.main);

		// TODO: enable these
		if (!Server.isConnected() || UserController.getCurrentUser() == null) {
			disable();  // Disable the buttons until the user is authenticated
			waitingForAuth = UserController.getCurrentUser() == null;
		}
		else {
			enable();
			waitingForAuth = false;
		}

		gameBtn = lookup("#gameBtn");
		replayBtn = lookup("#replayBtn");
		playingBtn = lookup("#playingBtn");
		profileBtn = lookup("#profileBtn");
		friendsBtn = lookup("#friendsBtn");
		rankingsBtn = lookup("#rankingsBtn");

		setOnPressListener(gameBtn, event -> {
			Platform.runLater(this::close);
			PreGameStage.getInstance().setupChoice();
			PreGameStage.getInstance().start();
		});

		setOnPressListener(replayBtn, event -> {
			//TODO: show a dialog , User specifies whether he wants to review a file record or a recording on server

			// TODO: if user chooses to review a file record, check if the file is valid and then start the replay

			// TODO: this is a temporary implementation. see previous TODOs

			String path = Utils.chooseFileToUpload("Choose recording", getInstance());
			if (path == null)
				return;

			GameRecord gr = MGson.fromJson(Utils.loadFile(path), GameRecord.class);
			Deck deck1 = Deck.fromJsonString(gr.deck1());
			Deck deck2 = Deck.fromJsonString(gr.deck2());
			User.PublicInfo publicInfo1 = new User.PublicInfo(1337, "user1", "nick1", Avatar.random());
			User.PublicInfo publicInfo2 = new User.PublicInfo(1984, "user2", "nick2", Avatar.random());
			long seed = gr.seed();

			GameStage.setCommonData(publicInfo1, publicInfo2, deck1, deck2, seed);
			GameStage.setReplay(0, gr.commands());
			GameStage.getInstance().start();
		});

		setOnPressListener(playingBtn, event -> {
			// TODO: implement
		});

		setOnPressListener(profileBtn, event -> ProfileStage.getInstance().start());

		setOnPressListener(friendsBtn, event -> {
			// TODO: show a dialog , User specifies whether he wants to add a friend or view friends list
		});

		setOnPressListener(rankingsBtn, event -> {
			// TODO: start the rankings stage
		});

		return true;
	}

	public boolean isWaitingForAuth() {
		return waitingForAuth;
	}
}
