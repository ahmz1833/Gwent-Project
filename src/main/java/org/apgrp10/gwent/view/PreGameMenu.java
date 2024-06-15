package org.apgrp10.gwent.view;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Application;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apgrp10.gwent.*;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.CardViewPregame;

import java.util.ArrayList;

public class PreGameMenu extends Application {
	private Stage stage;
	private Pane pane;
	private Scene scene;
	private GridPane[] deckLists = new GridPane[2];

	public static PreGameMenu currentMenu;
	public static final int screenWidth = 1500, cardWidth = 18;
	public static final int screenHeight = 800, cardHeight = 5;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		currentMenu = this;
		this.stage = stage;
		load();
		setTitle();
		addGradePane();
		setSpoiler();
		stage.show();
	}

	private void setTitle() {
		stage.setTitle("pre game menu");
		stage.getIcons().add(R.getImage("icons/profile.png"));
	}

	private void setSpoiler() {
		ImageView imageView = new ImageView(R.getImage("icons/preGame_selectImage.png"));
		imageView.setX(screenWidth / 2.0);
		imageView.setY(0);
		imageView.setFitWidth(screenWidth / 2.0);
		imageView.setFitHeight(screenHeight);
		pane.getChildren().add(imageView);
	}

	private void load() {
		this.scene = R.getFXML("preGame.fxml");
		stage.setScene(scene);
		pane = (Pane) scene.getRoot();
		stage.setResizable(false);
		stage.setMinWidth(screenWidth);
		stage.setMaxWidth(screenWidth);
		stage.setMinHeight(screenHeight);
		stage.setMaxHeight(screenHeight);
		setCursor();
	}

	private void addGradePane() {
		for (int i = 0; i < 2; i++) {
			GridPane gridPane = new GridPane();
			gridPane.setMinWidth(3 * screenWidth / (double) cardWidth + 0);
			gridPane.setMaxWidth(gridPane.getMinWidth());
			gridPane.setMaxHeight(3 * (double) screenHeight / cardHeight + 30);
			gridPane.setMaxHeight(gridPane.getMinHeight());
			gridPane.setVgap(5);
			gridPane.setHgap(5);
			for (int j = 0; j < 2; j++) {
				gridPane.getColumnConstraints().add(
						new ColumnConstraints(screenWidth / (double) cardWidth));
			}
			MFXScrollPane scroll = new MFXScrollPane(gridPane);
			scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			switch (i) {
				case 0 -> scroll.setLayoutX(10);
				case 1 -> scroll.setLayoutX(460);
			}
			scroll.setLayoutY(130);
			pane.getChildren().add(scroll);
			deckLists[i] = gridPane;
		}
	}

	public void updateLists(ArrayList<ArrayList<CardViewPregame>> arrayList) {
		for (int k = 0; k < 2; k++) {
			int i = 0, j = 0;
			deckLists[k].getChildren().clear();
			for (CardViewPregame cardImage : arrayList.get(k)) {
				CardViewPregame cardView = new CardViewPregame(CardInfo.allCards.get((int) (Math.random() * CardInfo.allCards.size())).name);
				addCardToGridPane(cardView, deckLists[k], i, j);

				if (i == 2) {
					i = 0;
					j++;
				} else
					i++;
			}
		}
	}

	private void setCursor() {
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}

	private void addCardToGridPane(CardViewPregame card, GridPane pane, int i, int j) {
		pane.add(new Pane(), 0,0);
		card.setOnMouseClicked(k -> {
			card.countMinusMinus();
		});
		pane.add(card, i, j);
	}
}
