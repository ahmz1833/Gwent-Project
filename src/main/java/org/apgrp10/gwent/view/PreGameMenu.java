package org.apgrp10.gwent.view;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apgrp10.gwent.*;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.CardViewPregame;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;

import java.util.ArrayList;
import java.util.Scanner;


public class PreGameMenu extends Application {
	private Stage stage;
	private Pane pane;
	private Scene scene;
	private GridPane[] deckLists = new GridPane[2];
	private Faction faction;
	private VBox infoVBox, factionInfo;
	private Text totalCardsText, totalUnitCadsText, totalSpecialCardsText, totalStrengthText, totalHeroText;
	private FivePlacePreGame fivePlacePreGame;
	private String leaderName;
	private ImageView leaderImage;
	private int currentIndexOfLeader, currentFactionIndex;


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
		fivePlacePreGame = new FivePlacePreGame(pane, this);
		addInfoBox();
		addGradePane();
		setSpoiler();
		loadFactionDeck(Faction.REALMS);
		stage.show();
		addLinkTexts();
	}

	private void addLinkTexts() {
		pane.getChildren().remove(factionInfo);
		factionInfo = new VBox();
		factionInfo.setLayoutX(screenWidth / 4.0 - 200);
		setFactionInfo(factionInfo);
		addFactionMassageToFactionInfo(factionInfo);
		addLinksToFactionInfo(factionInfo);
		pane.getChildren().add(factionInfo);
	}

	private void setFactionInfo(VBox factionInfo) {
		String text = "", address = "";
		switch (faction) {
			case REALMS -> {
				text = "Northern Realms";
				address = "deck_shield_realms";
			}
			case NILFGAARD -> {
				text = "Nilfgaardian Empire";
				address = "deck_shield_nilfgaard";
			}
			case MONSTERS -> {
				text = "Monsters";
				address = "deck_shield_monsters";
			}
			case SCOIATAEL -> {
				text = "Scoia'tael";
				address = "deck_shield_scoiatael";
			}
			case SKELLIGE -> {
				text = "Skellige";
				address = "deck_shield_skellige";
			}
		}
		HBox hBox = new HBox();
		StackPane imageStack = getStackPane(150, 50, Pos.BOTTOM_RIGHT);
		ImageView image = new ImageView(R.getImage("icons/" + address + ".png"));
		image.setFitWidth(40);
		image.setFitHeight(40);
		imageStack.getChildren().add(image);
		hBox.getChildren().add(imageStack);
		StackPane nameStack = getStackPane(200, 50, Pos.BOTTOM_CENTER);
		Text name = new Text(text);
		name.getStyleClass().add("linkedText");
		name.setFill(Color.WHITE);
		nameStack.getChildren().add(name);
		hBox.getChildren().add(nameStack);
		StackPane main = getStackPane(400, 50, Pos.BOTTOM_CENTER);
		main.getChildren().add(hBox);
		factionInfo.getChildren().add(main);
	}

	private void addFactionMassageToFactionInfo(VBox factionInfo) {
		HBox hBox = new HBox();
		String text = "";
		switch (faction) {
			case REALMS -> text = "Draw a card from your deck whenever you win a round.";
			case NILFGAARD -> text = "Wins any round that ends in a draw.";
			case MONSTERS -> text = "Keeps a random Unit Card out after each round.";
			case SCOIATAEL -> text = "Decides who takes first turn.";
			case SKELLIGE ->
					text = "2 random cards from the graveyard are placed on the battlefield at the start of the third round.";
		}
		Text name = new Text(text);
		name.getStyleClass().add("linkedText");
		name.setFill(Color.GOLD);
		StackPane main = getStackPane(400, 30, Pos.CENTER);
		main.getChildren().add(name);
		factionInfo.getChildren().add(main);
	}

	private void addLinksToFactionInfo(VBox factionInfo) {
		StackPane stack2 = getStackPane(400, 20, Pos.CENTER);
		HBox links = new HBox();
		links.getChildren().add(addCSSLinkedText("Upload Deck", k -> uploadDeck()));
		links.getChildren().add(addCSSLinkedText("Change Faction",
				k -> fivePlacePreGame.show(false, currentFactionIndex)));
		links.getChildren().add(addCSSLinkedText("Download Deck", k -> downloadDeck()));
		stack2.getChildren().add(links);
		factionInfo.getChildren().add(stack2);
	}

	private Pane addCSSLinkedText(String text, EventHandler<? super MouseEvent> event) {
		StackPane stackPane = getStackPane(133, 20, Pos.CENTER);
		Text label = new Text(text);
		label.getStyleClass().add("linkedText");
		label.setFill(Color.WHITE);
		label.setOnMouseClicked(event);
		stackPane.getChildren().add(label);
		return stackPane;
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

	public void loadFactionDeck(Faction faction) {
		switch (faction) {
			case REALMS -> currentFactionIndex = 0;
			case NILFGAARD -> currentFactionIndex = 1;
			case MONSTERS -> currentFactionIndex = 2;
			case SCOIATAEL -> currentFactionIndex = 3;
			case SKELLIGE -> currentFactionIndex = 4;
		}
		this.faction = faction;
		addLinkTexts();
		fivePlacePreGame.setFaction(faction);
		deckLists[0].getChildren().clear();
		deckLists[1].getChildren().clear();

		boolean hasLeader = false;
		for (CardInfo card : CardInfo.allCards) {
			if (card.faction.equals(faction) && card.row.equals(Row.LEADER) && !hasLeader) {
				hasLeader = true;
				changeLeader(card.name, 0);
			}
			if (card.faction.equals(faction) ||
					card.faction.equals(Faction.NATURAL) ||
					card.faction.equals(Faction.WEATHER) ||
					card.faction.equals(Faction.SPECIAL))
				if (card.row != Row.LEADER)
					for (int i = 0; i < card.count; i++)
						addCardToGridPane(card.pathAddress, false);
		}
		updateInfo();
	}

	private void setCursor() {
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}

	public void changeLeader(String name, int currentIndexOfLeader) {
		this.currentIndexOfLeader = currentIndexOfLeader;
		leaderName = name;
		String address = "";
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.name.equals(name)) {
				address = cardInfo.pathAddress;
				break;
			}
		}
		leaderImage.setImage(R.getImage("lg/" + address + ".jpg"));
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
			CardViewPregame card = new CardViewPregame(cardName);
			card.setOnMouseClicked(k -> {
				addCardToGridPane(cardName, !isGameDeck);
				deleteCardFromGridPane(cardName, isGameDeck);
				updateInfo();
			});
			deckLists[numberOfDeck].add(card, count % 3, count / 3);
			sortDeck(deckLists[numberOfDeck]);
		}
	}

	//USE ADDRESS OF FILE INSTEAD OF NAME OF CARD!
	private void deleteCardFromGridPane(String cardName, boolean isGameDeck) {
		int numberOfDeck = isGameDeck ? 1 : 0;
		boolean needSort = false;
		for (int i = 0; i < deckLists[numberOfDeck].getChildren().size(); i++) {
			CardViewPregame currentCard = (CardViewPregame) deckLists[numberOfDeck].getChildren().get(i);
			if (currentCard.getAddress().equals(cardName)) {
				currentCard.countMinusMinus();
				if (currentCard.getCount() <= 0)
					needSort = true;
				break;
			}
		}
		if (needSort)
			sortDeck(deckLists[numberOfDeck]);
	}
	public void accessptingChangeFaction(Faction faction){
		if(this.faction!= faction){
			loadFactionDeck(faction);
		}
	}

	private void sortDeck(GridPane pane) {
		//This function throws an exception in the first time!
		//So I have to try again! On the second try, it works correctly
		//we have "break" at the end of scope
		for (int tryCount = 0; tryCount < 100; tryCount++) {
			try {
				ArrayList<CardViewPregame> deck = new ArrayList<>();
				for (int i = 0; i < pane.getChildren().size(); i++) {
					if (((CardViewPregame) pane.getChildren().get(i)).getCount() > 0)
						deck.add((CardViewPregame) pane.getChildren().get(i));
				}
				String[] sortOrder = {"special", "weather"};
				pane.getChildren().clear();
				deck.sort((o1, o2) ->
				{
					int index1 = 3, index2 = 3;
					for (int i = 0; i < sortOrder.length; i++) {
						if (Faction.getEnum(sortOrder[i]).equals(o1.getFaction()))
							index1 = i;
						if (Faction.getEnum(sortOrder[i]).equals(o2.getFaction()))
							index2 = i;
					}
					if (index1 < index2)
						return -1;
					if (index1 > index2)
						return 1;
					if (o1.getStrength() == o2.getStrength())
						return o1.getName().compareTo(o2.getName());
					return (o1.getStrength() > o2.getStrength()) ? -1 : +1;
				});
				for (int i = 0; i < deck.size(); i++) {
					pane.add(deck.get(i), i % 3, i / 3);
				}
				break;
			} catch (Exception ignored) {
			}
		}
	}

	private void addInfoBox() {
		infoVBox = new VBox();
		infoVBox.setLayoutX(300);
		infoVBox.setLayoutY(130);
		textForInfo(infoVBox, "Leader");
		leaderImage = new ImageView();
		leaderImage.setFitWidth(100);
		leaderImage.setFitHeight(180);
		leaderImage.setOnMouseClicked(k -> fivePlacePreGame.show(true, currentIndexOfLeader));
		StackPane stackPane = getStackPane(150, 180, Pos.CENTER);
		stackPane.getChildren().add(leaderImage);
		infoVBox.getChildren().add(stackPane);
		textForInfo(infoVBox, "Total cards in deck:");
		totalCardsText = textWithImageInfo(infoVBox, "deck_stats_count");
		textForInfo(infoVBox, "Number of Unit cards:");
		totalUnitCadsText = textWithImageInfo(infoVBox, "deck_stats_unit");
		textForInfo(infoVBox, "Special cards:");
		totalSpecialCardsText = textWithImageInfo(infoVBox, "deck_stats_special");
		textForInfo(infoVBox, "Total unit cards strength:");
		totalStrengthText = textWithImageInfo(infoVBox, "deck_stats_strength");
		textForInfo(infoVBox, "Hero cards:");
		totalHeroText = textWithImageInfo(infoVBox, "deck_stats_hero");
		pane.getChildren().add(infoVBox);
	}

	private void textForInfo(VBox infoVBox, String text) {
		Text label = new Text(text);
		label.getStyleClass().add("textInfo");
		label.setFill(Color.rgb(182, 142, 20));
		StackPane stackPane = getStackPane(145, 30, Pos.CENTER);
		stackPane.getChildren().add(label);
		infoVBox.getChildren().add(stackPane);
	}

	private Text textWithImageInfo(VBox infoVBox, String imagePath) {
		ImageView image = new ImageView(R.getImage("icons/" + imagePath + ".png"));
		StackPane imagePane = getStackPane(75, 30, Pos.CENTER_RIGHT);
		imagePane.getChildren().add(image);
		Text label = new Text("0");
		label.getStyleClass().add("textInfo");
		label.setFill(Color.rgb(182, 142, 70));
		StackPane textPane = getStackPane(75, 30, Pos.CENTER_LEFT);
		textPane.getChildren().add(label);
		infoVBox.getChildren().add(new HBox(imagePane, textPane));
		return label;
	}

	private StackPane getStackPane(int width, int height, Pos pos) {
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(pos);
		stackPane.setMinWidth(width);
		stackPane.setMaxWidth(width);
		stackPane.setMinHeight(height);
		return stackPane;
	}

	private void updateInfo() {
		int totalCards = 0, totalUnitCads = 0, totalSpecialCards = 0, totalStrength = 0, totalHero = 0;
		for (int i = 0; i < deckLists[1].getChildren().size(); i++) {
			CardViewPregame card = (CardViewPregame) deckLists[1].getChildren().get(i);
			for (int j = 0; j < card.getCount(); j++) {
				totalCards++;
				totalStrength += card.getStrength();
				if (card.getFaction().equals(Faction.WEATHER) || card.getFaction().equals(Faction.SPECIAL))
					totalSpecialCards++;
				else
					totalUnitCads++;
				if (card.isHero())
					totalHero++;
			}
		}
		totalCardsText.setText(String.valueOf(totalCards));
		totalStrengthText.setText(String.valueOf(totalStrength));
		totalHeroText.setText(String.valueOf(totalHero));
		if (totalUnitCads < 22) {
			totalUnitCadsText.setText(totalUnitCads + "/22");
			totalUnitCadsText.setFill(Color.RED);
		} else {
			totalUnitCadsText.setText(String.valueOf(totalUnitCads));
			totalUnitCadsText.setFill(Color.rgb(182, 142, 70));
		}
		totalSpecialCardsText.setText(totalSpecialCards + "/10");
		if (totalSpecialCards > 10) {
			totalSpecialCardsText.setFill(Color.RED);
		} else {
			totalSpecialCardsText.setFill(Color.rgb(182, 142, 70));
		}
	}

	private void uploadDeck() {
		//TODO
	}

	private void downloadDeck() {
		//TODO
	}
}
