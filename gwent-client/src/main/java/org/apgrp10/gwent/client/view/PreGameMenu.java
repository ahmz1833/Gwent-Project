package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.model.CardView;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;
import org.apgrp10.gwent.utils.Utils;

import java.util.ArrayList;
import java.util.Map;


// This is A class for handling view operations for deck-choosing
public class PreGameMenu {
	public static final int screenWidth = 1280, screenHeight = 720;
	public static final double cardWidth = 9, cardHeight = 2.8;
	private final GridPane[] deckLists = new GridPane[2];
	private final int primaryX;
	private final User.PublicInfo userInfo;
	private Pane pane;
	private Faction faction;
	private VBox factionInfo;
	private Text totalCardsText, totalUnitCadsText, totalSpecialCardsText, totalStrengthText, totalHeroText;
	private FivePlacePreGame fivePlacePreGame;
	private String leaderName;
	private ImageView leaderImage;
	private int currentIndexOfLeader, currentFactionIndex;

	public PreGameMenu(Pane pane, boolean isFirstOne, User.PublicInfo userInfo) {
		this.pane = pane;
		this.userInfo = userInfo;
		this.primaryX =  isFirstOne ? 0 : 150;
		load();
		addInfoBox();
		addUserInfo();
		addGradePane();
		setSpoiler();
		uploadDeck(R.getAbsPath("primaryDeck.gwent"));
		addLinkTexts();
	}

	private void addUserInfo() {
		StackPane stackPane = getStackPane(200, 50, Pos.CENTER_LEFT);
		Text text = new Text("Good luck \"" + userInfo.nickname() + "\"");
		text.setFill(Color.GREEN);
		text.setWrappingWidth(200);
		text.setStyle("-fx-font-size: 19px; -fx-font-family: 'Yrsa SemiBold'");
		stackPane.getChildren().add(text);
		stackPane.setLayoutX(primaryX + 20);
		stackPane.setLayoutY(10);
		pane.getChildren().add(stackPane);
	}

	private void addLinkTexts() {
		pane.getChildren().remove(factionInfo);
		factionInfo = new VBox();
		factionInfo.setLayoutX(primaryX + screenWidth / 2.0 - 280);
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
		links.getChildren().add(addCSSLinkedText("Upload Deck", k ->
				uploadDeck(Utils.chooseFileToUpload("Choose deck to upload", PreGameStage.getInstance()))));
		links.getChildren().add(addCSSLinkedText("Change Faction", k ->
				fivePlacePreGame.show(false, currentFactionIndex)));
		links.getChildren().add(addCSSLinkedText("Download Deck", k ->
				Utils.choosePlaceAndDownload("Choose place to download deck", "deck.gwent",
						PreGameStage.getInstance(), downloadDeck())));
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

	private void setSpoiler() {
		ImageView imageView = new ImageView(R.getImage("icons/preGame_selectImage.png"));
		imageView.setX(primaryX > 50 ? 0 : 1131);
		imageView.setY(0);
		imageView.setFitWidth(screenWidth - 1131);
		imageView.setFitHeight(screenHeight - 35);
		pane.getChildren().add(imageView);
	}

	private void load() {
		pane.getChildren().clear();
		deckLists[0] = null;
		deckLists[1] = null;
		factionInfo = null;
		leaderName = null;
		leaderImage = null;
		totalCardsText = totalUnitCadsText = totalSpecialCardsText = totalStrengthText = totalHeroText = null;
		fivePlacePreGame = new FivePlacePreGame(pane, this, primaryX);
		Rectangle rec = new Rectangle(0, 0, screenWidth, screenHeight);
		rec.setFill(Color.BLACK);
		pane.getChildren().add(rec);
		rec.setOpacity(0.7);
		setCursor();
	}

	private void addGradePane() {
		for (int i = 0; i < 2; i++) {
			GridPane gridPane = new GridPane();
			gridPane.setVgap(3);
			gridPane.setHgap(3);
			for (int j = 0; j < 3; j++) {
				gridPane.getColumnConstraints().add(new ColumnConstraints(screenWidth / cardWidth + 3));
			}
			ScrollPane scroll = new ScrollPane(gridPane);
			scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			scroll.setPrefSize(3 * screenWidth / cardWidth + 30, 550);
			switch (i) {
				case 0 -> scroll.setLayoutX(primaryX + 9);
				case 1 -> scroll.setLayoutX(primaryX + 655);
			}
			scroll.setLayoutY(117);
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
			if (card.faction.equals(faction) || card.faction.equals(Faction.NATURAL) || card.faction.equals(Faction.WEATHER) || card.faction.equals(Faction.SPECIAL))
				if (card.row != Row.LEADER) for (int i = 0; i < card.count; i++)
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
			CardView currentCard = (CardView) deckLists[numberOfDeck].getChildren().get(i);
			if (currentCard.getAddress().equals(cardName)) {
				currentCard.countPlusPlus();
				founded = true;
				break;
			}
		}
		if (!founded) {
			int count = deckLists[numberOfDeck].getChildren().size();
			CardView card = CardView.newSelection(cardName,
					(double) screenWidth / cardWidth, (double) screenHeight / cardHeight);
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
			CardView currentCard = (CardView) deckLists[numberOfDeck].getChildren().get(i);
			if (currentCard.getAddress().equals(cardName)) {
				currentCard.countMinusMinus();
				if (currentCard.getCount() <= 0) needSort = true;
				break;
			}
		}
		if (needSort) sortDeck(deckLists[numberOfDeck]);
	}

	public void accessioningChangeFaction(Faction faction) {
		if (this.faction != faction) {
			AbstractStage stage = PreGameStage.getInstance();
			stage.showDialogAndWait(MFXDialogs.warn(), "Caution!", "Changing factions will clear the current deck.\nContinue?",
					Map.entry("#OK", e -> this.loadFactionDeck(faction)),
					Map.entry("*Cancel", e -> {}));
		}
	}

	private void sortDeck(GridPane pane) {
		//This function throws an exception in the first time!
		//So I have to try again! On the second try, it works correctly
		//we have "break" at the end of scope
		for (int tryCount = 0; tryCount < 100; tryCount++) {
			try {
				ArrayList<CardView> deck = new ArrayList<>();
				for (int i = 0; i < pane.getChildren().size(); i++) {
					if (((CardView) pane.getChildren().get(i)).getCount() > 0)
						deck.add((CardView) pane.getChildren().get(i));
				}
				String[] sortOrder = {"special", "weather"};
				pane.getChildren().clear();
				deck.sort((o1, o2) -> {
					int index1 = 3, index2 = 3;
					for (int i = 0; i < sortOrder.length; i++) {
						if (Faction.getEnum(sortOrder[i]).equals(o1.getFaction())) index1 = i;
						if (Faction.getEnum(sortOrder[i]).equals(o2.getFaction())) index2 = i;
					}
					if (index1 < index2) return -1;
					if (index1 > index2) return 1;
					if (o1.getStrength() == o2.getStrength()) return o1.getName().compareTo(o2.getName());
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
		VBox infoVBox = new VBox();
		infoVBox.setLayoutX(primaryX + 489);
		infoVBox.setLayoutY(117);
		textForInfo(infoVBox, "Leader");
		leaderImage = new ImageView();
		leaderImage.setFitWidth(100);
		leaderImage.setFitHeight(172);
		leaderImage.setOnMouseClicked(k -> fivePlacePreGame.show(true, currentIndexOfLeader));
		StackPane stackPane = getStackPane(142, 172, Pos.CENTER);
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
		StackPane buttonBorder = getStackPane(150, 35, Pos.CENTER);
		Rectangle button = new Rectangle(120, 35, Color.GRAY);
		button.setStyle("-fx-border-width: 2px; -fx-border-color: white");
		button.setArcWidth(20);
		button.setArcHeight(20);
		button.setOnMouseClicked(k -> startGame());
		Text text = new Text("Start game");
		text.setStyle("-fx-font-family: 'Comfortaa SemiBold'; -fx-font-size: 18px");
		text.setOnMouseClicked(k -> startGame());
		text.setFill(Color.WHITE);
		buttonBorder.getChildren().add(button);
		buttonBorder.getChildren().add(text);
		infoVBox.getChildren().add(buttonBorder);
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
			CardView card = (CardView) deckLists[1].getChildren().get(i);
			for (int j = 0; j < card.getCount(); j++) {
				totalCards++;
				totalStrength += card.getStrength();
				if (card.getFaction().equals(Faction.WEATHER) || card.getFaction().equals(Faction.SPECIAL))
					totalSpecialCards++;
				else totalUnitCads++;
				if (card.isHero()) totalHero++;
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

	private void uploadDeck(String path) {
		if (path == null)
			return;
		Deck deck = Deck.loadDeckFromFile(path);
		if (deck != null) {
			loadFactionDeck(deck.getFaction());
			int index = 0;
			for (CardInfo card : CardInfo.allCards) {
				if (card.faction.equals(faction) && card.row.equals(Row.LEADER)) {
					if (card.pathAddress.equals(deck.getLeader().pathAddress)) break;
					index++;
				}
			}
			changeLeader(deck.getLeader().name, index);
			for (Card card : deck.getDeck()) {
				deleteCardFromGridPane(card.pathAddress, false);
				addCardToGridPane(card.pathAddress, true);
			}
			updateInfo();
		} else {
			AbstractStage stage = PreGameStage.getInstance();
			stage.showAlert(MFXDialogs.error(), "Error!", "Uploaded deck is not formatted correctly!");
		}
	}

	private String downloadDeck() {
		return createDeckFromPane(deckLists[1]).toJsonString();
	}

	private void startGame() {
		int totalUnitCads = 0, totalSpecialCards = 0;
		for (int i = 0; i < deckLists[1].getChildren().size(); i++) {
			CardView card = (CardView) deckLists[1].getChildren().get(i);
			for (int j = 0; j < card.getCount(); j++) {
				if (card.getFaction().equals(Faction.WEATHER) || card.getFaction().equals(Faction.SPECIAL))
					totalSpecialCards++;
				else totalUnitCads++;
			}
		}
		if (totalSpecialCards <= 10 && totalUnitCads >= 22) {
			Deck deck = createDeckFromPane(deckLists[1]);
			PreGameStage.getInstance().startClicked(deck);
		}
		else
		{
			AbstractStage stage = PreGameStage.getInstance();
			stage.showAlert(MFXDialogs.error(), "Error!", "Your Deck is not ready yet!");
		}
	}

	private Deck createDeckFromPane(GridPane gridPane) {
		Deck deck = new Deck(currentFactionIndex, leaderName, userInfo);
		for (int i = 0; i < gridPane.getChildren().size(); i++) {
			try {
				CardView cardView = (CardView) gridPane.getChildren().get(i);
				for (CardInfo cardInfo : CardInfo.allCards)
					if (cardInfo.pathAddress.equals(cardView.getAddress())) {
						for (int j = 0; j < cardView.getCount(); j++)
							deck.addCard(deck.convertCortInfoToCard(cardInfo));
					}
			} catch (Exception ignored) {
			}
		}
		return deck;
	}

	public static class FivePlacePreGame extends Pane {
		private final Pane gamePane;
		private final ImageView[] images = new ImageView[5];
		private final Text textFiled;
		private final PreGameMenu preGameMenu;
		private final int primaryX;
		private Faction faction;
		private ArrayList<String> nameList = new ArrayList<>();
		private ArrayList<Image> imageList = new ArrayList<>();
		private int currentIndex = 0;
		private boolean isLeaderChange;

		public FivePlacePreGame(Pane pane, PreGameMenu preGameMenu, int primaryX) {
			this.primaryX = primaryX;
			gamePane = pane;
			this.preGameMenu = preGameMenu;
			HBox[] places = addPlaces();
			addImages(places);
			textFiled = addText(places);
		}

		private void addImages(HBox[] places) {
			//adding size and place
			for (int i = 0; i < 5; i++) {
				StackPane stackPane = new StackPane();
				switch (i) {
					case 0 ->
							stackPane = getStackPane(screenWidth / 5.0 - 20, screenHeight / 10.0 * 6 + 20 , Pos.TOP_RIGHT);
					case 1, 3 ->
							stackPane = getStackPane(screenWidth / 5.0 - 20, screenHeight / 10.0 * 6 + 20, Pos.CENTER);
					case 2 ->
							stackPane = getStackPane(screenWidth / 5.0 - 20, screenHeight / 10.0 * 6 + 20, Pos.BOTTOM_CENTER);
					case 4 ->
							stackPane = getStackPane(screenWidth / 5.0 - 20, screenHeight / 10.0 * 6 + 20, Pos.TOP_LEFT);
				}

				images[i] = new ImageView(R.getImage("lg/faction_monsters.jpg"));
				images[i].setFitWidth(screenWidth / 6.0 - 20 - 5 * Math.abs(i - 2));
				images[i].setFitHeight(screenHeight / 2.5 + 35 - 10 * Math.abs(i - 2) );
				images[i].setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-padding: 2;");
				//adding shadow
				DropShadow dropShadow = new DropShadow();
				dropShadow.setOffsetX(0);
				dropShadow.setOffsetY(0);
				if (i == 2) {
					dropShadow.setRadius(60);
					dropShadow.setColor(Color.rgb(225, 169, 50));
				} else {
					dropShadow.setRadius(40);
					dropShadow.setColor(Color.rgb(50, 225, 210));
				}
				images[i].setEffect(dropShadow);
				stackPane.getChildren().add(images[i]);
				places[1].getChildren().add(stackPane);
				int finalI = i;
				images[i].setOnMouseClicked(k -> {
					clickedOn(finalI);
					k.consume();
				});
			}
		}

		private HBox[] addPlaces() {
			VBox box = new VBox();
			this.getChildren().add(box);
			box.setOnMouseClicked(k -> endShow());
			box.setPrefWidth(screenWidth / 2.0);
			box.setPrefHeight(screenHeight);
			box.setLayoutX(primaryX);
			box.setLayoutY(0);
			DropShadow dropShadow = new DropShadow();
			dropShadow.setOffsetX(0);
			dropShadow.setOffsetY(0);
			dropShadow.setRadius(40);
			dropShadow.setColor(Color.rgb(225, 169, 50));
			box.setEffect(dropShadow);
			HBox place1 = new HBox();
			place1.setMinHeight(screenHeight / 5.0 - 90);
			HBox place2 = new HBox();
			place2.setMinHeight(screenHeight / 10.0 * 6 + 60);
			place2.setMaxHeight(place2.getMinHeight());
			HBox place3 = new HBox();
			place3.setMinHeight(screenHeight / 5.0);
			place3.setMaxHeight(place3.getMinHeight());
			HBox space = new HBox();
			space.setMinHeight(15);
			box.getChildren().addAll(place1, place2, space, place3);
			return new HBox[]{place1, place2, place3};
		}

		private StackPane getStackPane(double width, double height, Pos pos) {
			StackPane stackPane = new StackPane();
			stackPane.setAlignment(pos);
			stackPane.setMinWidth(width);
			stackPane.setMaxWidth(width);
			stackPane.setMinHeight(height);
			stackPane.setMaxHeight(height);
			return stackPane;
		}

		private Text addText(HBox[] places) {
			StackPane stackPane = getStackPane(1130, screenHeight / 5.0, Pos.TOP_CENTER);
			Text text = new Text("\n");
			text.setTextAlignment(TextAlignment.CENTER);
			text.setStyle("-fx-font-size: 15px; -fx-font-family: 'Yrsa SemiBold'");
			text.setFill(Color.WHITE);
			text.setWrappingWidth(400);
			Rectangle clip = new Rectangle(400, 100);
			Rectangle background = new Rectangle(400, 120, Color.GRAY);
			background.setArcWidth(40);
			background.setArcHeight(40);
			background.setStyle("-fx-padding: 10px");
			text.setClip(clip);
			stackPane.getChildren().addAll(background, text);
			places[2].getChildren().add(stackPane);
			return text;
		}

		public void show(boolean isLeaderChange, int index) {
			this.isLeaderChange = isLeaderChange;
			nameList = new ArrayList<>();
			imageList = new ArrayList<>();
			currentIndex = index;
			if (this.isLeaderChange) {
				for (CardInfo card : CardInfo.allCards) {
					if (card.faction.equals(faction) && card.row.equals(Row.LEADER)) {
						nameList.add(card.name);
						imageList.add(R.getImage("lg/" + card.pathAddress + ".jpg"));
					}
				}
			} else {
				nameList.add("REALMS");
				nameList.add("NILFGAARD");
				nameList.add("MONSTERS");
				nameList.add("SCOIATAEL");
				nameList.add("SKELLIGE");
				imageList.add(R.getImage("lg/faction_realms.jpg"));
				imageList.add(R.getImage("lg/faction_nilfgaard.jpg"));
				imageList.add(R.getImage("lg/faction_monsters.jpg"));
				imageList.add(R.getImage("lg/faction_scoiatael.jpg"));
				imageList.add(R.getImage("lg/faction_skellige.jpg"));
			}
			setCurrentImage(currentIndex);
			try {
				gamePane.getChildren().add(this);
			} catch (Exception ignored) {
			}
		}

		private void endShow() {
			try {
				gamePane.getChildren().remove(this);
			} catch (Exception ignored) {
			}
		}

		private void clickedOn(int index) {
			currentIndex += index - 2;
			setCurrentImage(currentIndex);
			if (index == 2) {
				if (isLeaderChange) {
					preGameMenu.changeLeader(nameList.get(currentIndex), currentIndex);
				} else {
					switch (currentIndex) {
						case 0 -> preGameMenu.accessioningChangeFaction(Faction.REALMS);
						case 1 -> preGameMenu.accessioningChangeFaction(Faction.NILFGAARD);
						case 2 -> preGameMenu.accessioningChangeFaction(Faction.MONSTERS);
						case 3 -> preGameMenu.accessioningChangeFaction(Faction.SCOIATAEL);
						case 4 -> preGameMenu.accessioningChangeFaction(Faction.SKELLIGE);
					}
				}
				endShow();
			}
		}

		private void changeText() {
			String text = nameList.get(currentIndex);
			switch (nameList.get(currentIndex).trim()) {
				case "Foltest - The Siegemaster" ->
						text = "Leader Ability\nPick an Impenetrable Fog card from your deck and play it instantly.";
				case "Foltest - The Steel-Forged" ->
						text = "Leader Ability\nClear any weather effects (resulting from Biting Frost, Torrential " + "Rain or Impenetrable Fog cards) in play.";
				case "Foltest - King of Temeria" ->
						text = "Leader Ability\nDoubles the strength of all your Siege units (unless a Commander's " + "Horn is also present on that row).";
				case "Foltest - Lord Commander of the North" ->
						text = "Leader Ability\nDestroy your enemy's strongest Siege unit(s) if the combined strength " + "of all his or her Siege units is 10 or more.";
				case "Foltest - Son of Medell" ->
						text = "Leader Ability\nDestroy your enemy's strongest Ranged Combat unit(s) if the combined " + "strength of all his or her Ranged Combat units is 10 or more.";
				case "Emhyr var Emreis - the White Flame" ->
						text = "Leader Ability\nPick a Torrential Rain card from your deck and play it instantly.";
				case "Emhyr var Emreis - His Imperial Majesty" ->
						text = "Leader Ability\nLook at 3 random cards from your opponent's hand.";
				case "Emhyr var Emreis - Emperor of Nilfgaard" ->
						text = "Leader Ability\nCancel your opponent's Leader Ability.";
				case "Emhyr var Emreis - The Relentless" ->
						text = "Leader Ability\nDraw a card from your opponent's discard pile.";
				case "Emhyr var Emreis - Invader of the North" ->
						text = "Leader Ability\nAbilities that restore a unit to the battlefield restore a randomly-chosen" + " unit. Affects both players.";
				case "Eredin - Bringer of Death" ->
						text = "Leader Ability\nDouble the strength of all your Close Combat units (unless a Commander's" + " horn is also present on that row).";
				case "Eredin - King of the Wild Hunt" ->
						text = "Leader Ability\nRestore a card from your discard pile to your hand.";
				case "Eredin - Destroyer of Worlds" ->
						text = "Leader Ability\nDiscard 2 card and draw 1 card of your choice from your deck.";
				case "Eredin - Commander of the Red Riders" ->
						text = "Leader Ability\nPick any weather card from your deck and play it instantly.";
				case "Eredin Bréacc Glas - The Treacherous" ->
						text = "Leader Ability\nDoubles the strength of all spy cards (affects both players).";
				case "Francesca Findabair - Queen of Dol Blathanna" ->
						text = "Leader Ability\nDestroy your enemy's strongest Close Combat unit(s) if the combined " + "strength of all his or her Close Combat units is 10 or more.";
				case "Francesca Findabair - the Beautiful" ->
						text = "Leader Ability\nDoubles the strength of all your Ranged Combat units (unless a" + " Commander's Horn is also present on that row).";
				case "Francesca Findabair - Daisy of the Valley" ->
						text = "Leader Ability\nDraw an extra card at the beginning of the battle.";
				case "Francesca Findabair - Pureblood Elf" ->
						text = "Leader Ability\nPick a Biting Frost card from your deck and play it instantly.";
				case "Francesca Findabair - Hope of the Aen Seidhe" ->
						text = "Leader Ability\nMove agile units to whichever valid row maximizes their strength " + "(don't move units already in optimal row).";
				case "Crach an Craite" ->
						text = "Leader Ability\nShuffle all cards from each player's graveyard back into their decks.";
				case "King Bran" -> text = "Leader Ability\nUnits only lose half their Strength in bad weather conditions.";
				case "REALMS" -> text = "Northern Realms\nDraw a card from your deck whenever you win a round.";
				case "NILFGAARD" -> text = "Nilfgaardian Empire\nWins any round that ends in a draw.";
				case "MONSTERS" -> text = "Monsters\nKeeps a random Unit Card out after each round.";
				case "SCOIATAEL" -> text = "Scoia'tael\nDecides who takes first turn.";
				case "SKELLIGE" ->
						text = "Skellige\n2 random cards from the graveyard are placed on the battlefield at the start of the third round.";
			}
			textFiled.setText("\n" + text);
		}

		public void setFaction(Faction faction) {
			currentIndex = 0;
			this.faction = faction;
		}

		private void setCurrentImage(int index) {
			for (int i = -2; i <= 2; i++) {
				addImageToIndex(index + i, 2 + i);
			}
			changeText();
		}

		private void addImageToIndex(int arrayIndex, int placeIndex) {
			try {
				images[placeIndex].setImage(null);
				images[placeIndex].setImage(imageList.get(arrayIndex));
			} catch (Exception ignored) {
			}
		}
	}
}
