package org.apgrp10.gwent.client.view;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.model.CardView;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.card.*;
import org.apgrp10.gwent.utils.ANSI;

import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class PreGameMenu extends Application {
	public static final int screenWidth = 1280, cardWidth = 18;
	public static final int screenHeight = 720, cardHeight = 5;
	private final GridPane[] deckLists = new GridPane[2];
	private final int primaryX;
	private final PreGameController preGameController;
	private final User user;
	private final boolean isUserOne;
	private Pane pane;
	private Faction faction;
	private VBox factionInfo;
	private Text totalCardsText, totalUnitCadsText, totalSpecialCardsText, totalStrengthText, totalHeroText;
	private FivePlacePreGame fivePlacePreGame;
	private String leaderName;
	private ImageView leaderImage;
	private int currentIndexOfLeader, currentFactionIndex;
	
	
	public PreGameMenu(PreGameController preGameController, boolean isFirstOne, User user) {
		isUserOne = isFirstOne;
		this.user = user;
		this.preGameController = preGameController;
		if (isFirstOne)
			primaryX = 0;
		else
			primaryX = screenWidth / 2;
		start(PreGameStage.getInstance());
	}
	
	@Override
	public void start(Stage stage) {
		load();
		fivePlacePreGame = new FivePlacePreGame(pane, this, primaryX);
		addInfoBox();
		addUserInfo();
		addGradePane();
		setSpoiler();
		uploadDeck(R.getAbsPath("primaryDeck.gwent"));
		addLinkTexts();
		PreGameStage.getInstance().start();
	}
	
	private void addUserInfo() {
		StackPane stackPane = getStackPane(200, 50, Pos.CENTER_LEFT);
		Text text = new Text("Good luck \"" + user.getNickname() + "\"");
		text.setFill(Color.GREEN);
		text.setWrappingWidth(200);
		text.setStyle("-fx-font-size: 13px");
		stackPane.getChildren().add(text);
		stackPane.setLayoutX(primaryX + 5);
		stackPane.setLayoutY(3);
		pane.getChildren().add(stackPane);
	}
	
	private void addLinkTexts() {
		pane.getChildren().remove(factionInfo);
		factionInfo = new VBox();
		factionInfo.setLayoutX(primaryX + screenWidth / 4.0 - 200);
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
		links.getChildren().add(addCSSLinkedText("Upload Deck", k -> chooseFileToUpload()));
		links.getChildren().add(addCSSLinkedText("Change Faction", k -> fivePlacePreGame.show(false, currentFactionIndex)));
		links.getChildren().add(addCSSLinkedText("Download Deck", k -> choosePlaceToDownload()));
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
		imageView.setX(screenWidth / 2.0 - primaryX);
		imageView.setY(0);
		imageView.setFitWidth(screenWidth / 2.0);
		imageView.setFitHeight(screenHeight);
		pane.getChildren().add(imageView);
	}
	
	private void load() {
		pane = PreGameStage.getInstance().getPane();
		pane.getStylesheets().add(R.get("css/preGame.css").toExternalForm());
		setCursor();
	}
	
	private void addGradePane() {
		for (int i = 0; i < 2; i++) {
			GridPane gridPane = new GridPane();
			gridPane.setVgap(5);
			gridPane.setHgap(5);
			for (int j = 0; j < 3; j++) {
				gridPane.getColumnConstraints().add(new ColumnConstraints(screenWidth / (double) cardWidth - 5));
			}
			ScrollPane scroll = new ScrollPane(gridPane);
			scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			scroll.setPrefSize(3 * screenWidth / (double) cardWidth + 12, 550);
			switch (i) {
				case 0 -> scroll.setLayoutX(primaryX + 9);
				case 1 -> scroll.setLayoutX(primaryX + 398);
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
			new MessagePreGame(this, pane, faction, primaryX);
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
		infoVBox.setLayoutX(primaryX + 245);
		infoVBox.setLayoutY(117);
		textForInfo(infoVBox, "Leader");
		leaderImage = new ImageView();
		leaderImage.setFitWidth(85);
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
		Text text = new Text("start game");
		text.setOnMouseClicked(k -> startGame());
		text.setFill(Color.WHITE);
		text.setStyle("-fx-font-size: 18px");
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
		Deck deck = Deck.loadDeck(path);
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
			new MessagePreGame(pane, primaryX);
		}
	}
	
	private String downloadDeck() {
		Deck deck = createDeckFromPane(deckLists[1]);
		return Deck.saveDeck(deck);
	}
	
	private void chooseFileToUpload() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gwent Files", "*.gwent"));
		File downloadFolder = new File(System.getProperty("user.home"), "Downloads");
		fileChooser.setInitialDirectory(downloadFolder);
		File selectedFile = fileChooser.showOpenDialog(PreGameStage.getInstance());
		if (selectedFile != null) {
			uploadDeck(selectedFile.getAbsolutePath());
		}
	}
	
	private void choosePlaceToDownload() {
		String downloadedFileName = "deck.gwent";
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.setInitialFileName(downloadedFileName);
		String home = System.getProperty("user.home");
		File downloadFolder = new File(home, "Downloads");
		fileChooser.setInitialDirectory(downloadFolder);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gwent Files", "*.gwent"));
		File selectedDir = fileChooser.showSaveDialog(PreGameStage.getInstance());
		if (selectedDir != null) {
			try {
				File myFile = new File(selectedDir.getAbsolutePath());
				FileWriter myWriter = new FileWriter(myFile);
				myWriter.write(downloadDeck());
				myWriter.close();
			} catch (Exception ignored) {
			}
		}
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
			pane.getChildren().clear();
			pane = null;
			deckLists[0] = null;
			deckLists[1] = null;
			factionInfo = null;
			totalCardsText = totalUnitCadsText = totalSpecialCardsText = totalStrengthText = totalHeroText = null;
			fivePlacePreGame = null;
			leaderName = null;
			leaderImage = null;
			if (isUserOne)
				preGameController.setDeck1(deck);
			else
				preGameController.setDeck2(deck);
		}
	}
	
	public Deck createDeckFromPane(GridPane gridPane) {
		Deck deck = new Deck(currentFactionIndex, leaderName, user);
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
}
