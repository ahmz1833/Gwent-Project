package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.ReplayInputController;
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
	private MFXButton gameBtn, replayBtn, playingBtn, profileBtn, friendsBtn, rankingsBtn;

	private MainStage() {
		super("Gwent Main", null);// TODO: set icon
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of MainStage");
	}

	public static MainStage getInstance() {
		if (INSTANCE == null) INSTANCE = new MainStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		setScene(R.scene.main);

		gameBtn = lookup("#gameBtn");
		replayBtn = lookup("#replayBtn");
		playingBtn = lookup("#playingBtn");
		profileBtn = lookup("#profileBtn");
		friendsBtn = lookup("#friendsBtn");
		rankingsBtn = lookup("#rankingsBtn");

		setOnPressListener(gameBtn, event -> PreGameStage.getInstance().start());

		setOnPressListener(replayBtn, event -> {
			//TODO: show a dialog , User specifies whether he wants to review a file record or a recording on server

			// TODO: if user chooses to review a file record, check if the file is valid and then start the replay
//			String path = Utils.chooseFileToUpload("Choose recording", getInstance());
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

//		Pane pane = new Pane();
//		Scene scene = new Scene(pane);
//		pane.setPrefWidth(400);
//		pane.setPrefHeight(300);
//		MFXButton btn = new MFXButton("Salam");
//		Server.send(new Request("fastPlay"), res -> {
//			playerId = res.getBody().get("playerId").getAsInt();
//			ANSI.log("we are player " + playerId);
//		});
//		Server.setListener("makeDeck", req -> {
//			start = true;
//			Server.setListener("makeDeck", null);
//			return req.response(Response.OK_NO_CONTENT);
//		});
//		btn.setOnMouseClicked(event -> {
//			if (!start)
//				return;
//			this.close();
//			User.PublicInfo publicInfo = new User.PublicInfo(1337, "user1", "nick1", Avatar.random());
//			User.PublicInfo publicInfo2 = new User.PublicInfo(1984, "user2", "nick2", Avatar.random());
//			new PreGameController(publicInfo, publicInfo2, playerId != 1, playerId != 0);
//		});
//		pane.getChildren().add(btn);
//
//
//		MFXButton btn2 = new MFXButton("Salam2");
//		btn2.setLayoutY(50);
//		btn2.setOnMouseClicked(event -> LoginStage.getInstance().start());
//		pane.getChildren().add(btn2);
//		setScene(scene);


//		MFXButton btn3 = new MFXButton("Replay");
//		btn3.setLayoutY(100);
//		btn3.setOnMouseClicked(event -> {
//			String path = Utils.chooseFileToUpload("Choose recording", getInstance());
//			if (path == null)
//				return;
//
//			GameRecord gr = MGson.fromJson(Utils.loadFile(path), GameRecord.class);
//			Deck deck1 = Deck.fromJsonString(gr.deck1());
//			Deck deck2 = Deck.fromJsonString(gr.deck2());
//			User.PublicInfo publicInfo1 = new User.PublicInfo(1337, "user1", "nick1", Avatar.random());
//			User.PublicInfo publicInfo2 = new User.PublicInfo(1984, "user2", "nick2", Avatar.random());
//			deck1.setUser(publicInfo1);
//			deck2.setUser(publicInfo2);
//			long seed = gr.seed();
//			ReplayInputController c1 = new ReplayInputController(gr.commands());
//			ReplayInputController c2 = new ReplayInputController(gr.commands());
//			GameMenu gameMenu = new GameMenu(getInstance());
//			new GameController(c1, c2, deck1, deck2, seed, gameMenu, dard -> { System.exit(0); }, 0, true);
//		});
		return true;
	}
}
