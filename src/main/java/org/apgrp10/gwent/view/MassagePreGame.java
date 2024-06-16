package org.apgrp10.gwent.view;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apgrp10.gwent.model.card.Faction;


public class MassagePreGame extends Pane {
	private final PreGameMenu preGameMenu;
	private final Pane gamePane;
	private final Faction faction;
	private final int cornerX = PreGameMenu.screenWidth / 4 - 200;
	private final int cornerY = PreGameMenu.screenHeight / 2 - 80;

	public MassagePreGame(PreGameMenu preGameMenu, Pane gamePane, Faction faction) {
		this.faction = faction;
		this.gamePane = gamePane;
		this.preGameMenu = preGameMenu;
		this.setMinWidth(PreGameMenu.screenWidth / 2.0);
		this.setMinHeight(PreGameMenu.screenHeight);
		this.setLayoutX(0);
		this.setLayoutY(0);
		gamePane.getChildren().add(this);
		this.setOnMouseClicked(k -> endShow());
		addBackground();
		addBackground();
		addButton("OK", Color.GREEN, 260, k -> {
			preGameMenu.loadFactionDeck(faction);
			endShow();
		});
		addButton("Cancel", Color.RED, 330, k -> endShow());
		addText();
	}

	private void addText() {
		Text text = new Text("Changing factions will clear the current deck.\nContinue? ");
		text.setStyle("-fx-font-size: 19px");
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(Color.YELLOW);
		text.setX(cornerX + 18);
		text.setY(cornerY + 35);
		this.getChildren().add(text);
	}

	private void addBackground() {
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0);
		dropShadow.setOffsetY(0);
		dropShadow.setRadius(60);
		dropShadow.setColor(Color.rgb(225, 250, 0));
		Rectangle background = new Rectangle(400, 160, Color.GRAY);
		background.setArcWidth(50);
		background.setArcHeight(50);
		background.setX(cornerX);
		background.setY(cornerY);
		background.setEffect(dropShadow);
		this.getChildren().add(background);
		background.setOnMouseClicked(Event::consume);
	}

	private void addButton(String text, Color color, int x, EventHandler<? super MouseEvent> event) {
		Button button = new Button(text);
		button.setTextFill(Color.WHITE);
		button.setBackground(Background.fill(color));
		button.setLayoutX(cornerX + x);
		button.setLayoutY(cornerY + 110);
		button.setMinWidth(50);
		button.setMinHeight(30);
		button.setOnMouseClicked(event);
		this.getChildren().add(button);
	}

	private void endShow() {
		gamePane.getChildren().remove(this);
	}
}
