package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.MouseInputController;
import org.apgrp10.gwent.client.controller.ServerInputController;
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

	private PreGameStage() {
		super("PreGame Menu", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of PreGameMenu");
	}

	public static PreGameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new PreGameStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		this.setWidth(PreGameMenu.screenWidth);
		this.setHeight(PreGameMenu.screenHeight);
		deck1 = deck2 = null;
		pane = new Pane();
		setScene(new Scene(pane));
		pane.getStylesheets().add(R.get("css/preGame.css").toExternalForm());
		new PreGameMenu(pane, true, new User.PublicInfo(0, "salam", "khobi", Avatar.random()));
		return true;
	}

	// This method will be called when we click on Start game button (And the deck is correct)
	public void startClicked(Deck deck) {
		// TODO: determine that if I am first player or second


		// TODO; this is Tof:
		if(deck1 == null)
		{
			deck1 = deck;
			new PreGameMenu(pane, false, new User.PublicInfo(0, "droud", "badi", Avatar.random()));
		}
		else
		{
			deck2 = deck;
			GameStage.setLocal(deck1, deck2);
			GameStage.getInstance().start();
			this.close();
		}
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
		event.consume();
		showExitDialog();
	}
}
