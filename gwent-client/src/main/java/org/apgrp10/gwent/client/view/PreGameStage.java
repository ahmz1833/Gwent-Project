package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;

import java.util.Map;

public class PreGameStage extends AbstractStage {
	private static PreGameStage INSTANCE;
	private Pane pane;

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
		pane = new Pane();
		Scene scene = new Scene(pane);
		setScene(scene);
		pane.getStylesheets().add(R.get("css/preGame.css").toExternalForm());

		new PreGameMenu(pane, true, new User.PublicInfo(0, "salam", "khobi", Avatar.random()));
		return true;
	}

	// This method will be called when we click on Start game button (And the deck is correct)
	public void startClicked(Deck deck) {
		// TODO: determine that if I am first player or second
		// (Do we have to open GameStage or make a new Deck?)
		showDialogAndWait(MFXDialogs.info(), "Start Game", "", Map.entry("#OK", e->{}));
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
//		event.consume();
		// TODO:  show Exit Confirmation
	}

	@Override
	protected void onGetFocus() {

	}

	@Override
	protected void onLostFocus() {

	}
}
