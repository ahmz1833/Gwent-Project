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
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;


public class PreGameMenu extends Application {
	private Stage stage;
	private Pane pane;
	private Scene scene;
	private GridPane[] deckLists = new GridPane[2];
	private Faction faction = Faction.SCOIATAEL;

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
		loadFactionDeck();
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

	private void loadFactionDeck() {
		deckLists[0].getChildren().clear();
		deckLists[1].getChildren().clear();
		for (CardInfo card : CardInfo.allCards)
			if (card.faction.equals(faction) ||
					card.faction.equals(Faction.NATURAL) ||
					card.faction.equals(Faction.WEATHER) ||
					card.faction.equals(Faction.SPECIAL))
				if (card.row != Row.LEADER)
					for (int i = 0; i < card.count; i++)
						addCardToGridPane(card.pathAddress, false);
	}

	private void setCursor() {
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}

	//USE ADDRESS OF FILE INSTEAD OF NAME OF CARD!
	private void addCardToGridPane(String cardName, boolean isGameDeck) {
		int numberOfDeck = isGameDeck ? 1 : 0;
		boolean founded = false;
		for (int i = 0; i < deckLists[numberOfDeck].getChildren().size(); i++) {
			CardViewPregame currentCard = (CardViewPregame) deckLists[numberOfDeck].getChildren().get(i);
			if (currentCard.getAddress().equals(cardName)) {
				currentCard.countPlusPlus();
				founded = true;
				break;
			}
		}
		if (!founded) {
			int count = deckLists[numberOfDeck].getChildren().size();
			deckLists[numberOfDeck].add(new CardViewPregame(cardName), count % 3, count / 3);

		}
	}
	private void deleteCardFromGridPane(String cardName, boolean isGameDeck) {
		//TODO
	}

}
