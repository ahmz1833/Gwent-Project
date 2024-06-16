package org.apgrp10.gwent.view;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;

import java.util.ArrayList;

public class FivePlacePreGame extends Pane {
	private final Pane gamePane;
	private final ImageView[] images = new ImageView[5];
	private final Text textFiled;
	private final PreGameMenu preGameMenu;
	private Faction faction;
	private ArrayList<String> nameList = new ArrayList<>();
	private ArrayList<Image> imageList = new ArrayList<>();
	private int currentIndex = 0;
	private boolean isLeaderChange;

	public FivePlacePreGame(Pane pane, PreGameMenu preGameMenu) {
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
				case 0 -> stackPane = getStackPane(PreGameMenu.screenWidth / 10.0,
						PreGameMenu.screenHeight / 10.0 * 6,
						Pos.TOP_RIGHT);
				case 1, 3 -> stackPane = getStackPane(PreGameMenu.screenWidth / 10.0,
						PreGameMenu.screenHeight / 10.0 * 6,
						Pos.CENTER);
				case 2 -> stackPane = getStackPane(PreGameMenu.screenWidth / 10.0,
						PreGameMenu.screenHeight / 10.0 * 6,
						Pos.BOTTOM_CENTER);
				case 4 -> stackPane = getStackPane(PreGameMenu.screenWidth / 10.0,
						PreGameMenu.screenHeight / 10.0 * 6,
						Pos.TOP_LEFT);
			}

			images[i] = new ImageView(R.getImage("lg/faction_monsters.jpg"));
			images[i].setFitWidth(PreGameMenu.screenWidth / 12.0 - 20 * (Math.abs(i - 2)));
			images[i].setFitHeight(PreGameMenu.screenHeight / 2.5 - 60 * (Math.abs(i - 2)));
			images[i].setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-padding: 2;");
			//adding shadow
			DropShadow dropShadow = new DropShadow();
			dropShadow.setOffsetX(0);
			dropShadow.setOffsetY(0);
			if (i == 2) {
				dropShadow.setRadius(40);
				dropShadow.setColor(Color.rgb(225, 169, 50));
			} else {
				dropShadow.setRadius(20);
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
		box.setPrefWidth(PreGameMenu.screenWidth / 2.0);
		box.setPrefHeight(PreGameMenu.screenHeight);
		box.setLayoutX(0);
		box.setLayoutY(0);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0);
		dropShadow.setOffsetY(0);
		dropShadow.setRadius(40);
		dropShadow.setColor(Color.rgb(225, 169, 50));
		box.setEffect(dropShadow);
		HBox place1 = new HBox();
		place1.setMinHeight(PreGameMenu.screenHeight / 5.0);
		HBox place2 = new HBox();
		place2.setMinHeight(PreGameMenu.screenHeight / 10.0 * 6);
		place2.setMaxHeight(place2.getMinHeight());
		HBox place3 = new HBox();
		place3.setMinHeight(PreGameMenu.screenHeight / 5.0);
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
		StackPane stackPane = getStackPane(PreGameMenu.screenWidth / 2.0, PreGameMenu.screenHeight / 5.0, Pos.TOP_CENTER);
		Text text = new Text("\n");
		text.setTextAlignment(TextAlignment.CENTER);
		text.setStyle("-fx-font-size: 16px");
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
					case 0 -> preGameMenu.accessptingChangeFaction(Faction.REALMS);
					case 1 -> preGameMenu.accessptingChangeFaction(Faction.NILFGAARD);
					case 2 -> preGameMenu.accessptingChangeFaction(Faction.MONSTERS);
					case 3 -> preGameMenu.accessptingChangeFaction(Faction.SCOIATAEL);
					case 4 -> preGameMenu.accessptingChangeFaction(Faction.SKELLIGE);
				}
			}
			endShow();
		}
	}

	private void changeText() {
		String text = nameList.get(currentIndex);
		switch (nameList.get(currentIndex).trim()) {
			case "Foltest - King of Temeria" ->
					text = "Leader Ability\nPick an Impenetrable Fog card from your deck and play it instantly.";
			case "Foltest - Lord Commander of the North" ->
					text = "Leader Ability\nClear any weather effects (resulting from Biting Frost, Torrential " +
							"Rain or Impenetrable Fog cards) in play.";
			case "Foltest - The Siegemaster" ->
					text = "Leader Ability\nDoubles the strength of all your Siege units (unless a Commander's " +
							"Horn is also present on that row).";
			case "Foltest - The Steel-Forged" ->
					text = "Leader Ability\nDestroy your enemy's strongest Siege unit(s) if the combined strength " +
							"of all his or her Siege units is 10 or more.";
			case "Foltest - Son of Medell" ->
					text = "Leader Ability\nDestroy your enemy's strongest Ranged Combat unit(s) if the combined " +
							"strength of all his or her Ranged Combat units is 10 or more.";
			case "Emhyr var Emreis - His Imperial Majesty" ->
					text = "Leader Ability\nPick a Torrential Rain card from your deck and play it instantly.";
			case "Emhyr var Emreis - Emperor of Nilfgaard" ->
					text = "Leader Ability\nLook at 3 random cards from your opponent's hand.";
			case "Emhyr var Emreis - the White Flame" ->
					text = "Leader Ability\nCancel your opponent's Leader Ability.";
			case "Emhyr var Emreis - The Relentless" ->
					text = "Leader Ability\nDraw a card from your opponent's discard pile.";
			case "Emhyr var Emreis - Invader of the North" ->
					text = "Leader Ability\nAbilities that restore a unit to the battlefield restore a randomly-chosen" +
							" unit. Affects both players.";
			case "Eredin - Commander of the Red Riders" ->
					text = "Leader Ability\nDouble the strength of all your Close Combat units (unless a Commander's" +
							" horn is also present on that row).";
			case "Eredin - Bringer of Death" ->
					text = "Leader Ability\nRestore a card from your discard pile to your hand.";
			case "Eredin - Destroyer of Worlds" ->
					text = "Leader Ability\nDiscard 2 card and draw 1 card of your choice from your deck.";
			case "Eredin - King of the Wild Hunt" ->
					text = "Leader Ability\nPick any weather card from your deck and play it instantly.";
			case "Eredin Bréacc Glas - The Treacherous" ->
					text = "Leader Ability\nDoubles the strength of all spy cards (affects both players).";
			case "Francesca Findabair - Queen of Dol Blathanna" ->
					text = "Leader Ability\nDestroy your enemy's strongest Close Combat unit(s) if the combined " +
							"strength of all his or her Close Combat units is 10 or more.";
			case "Francesca Findabair - the Beautiful" ->
					text = "Leader Ability\nDoubles the strength of all your Ranged Combat units (unless a" +
							" Commander's Horn is also present on that row).";
			case "Francesca Findabair - Daisy of the Valley" ->
					text = "Leader Ability\nDraw an extra card at the beginning of the battle.";
			case "Francesca Findabair - Pureblood Elf" ->
					text = "Leader Ability\nPick a Biting Frost card from your deck and play it instantly.";
			case "Francesca Findabair - Hope of the Aen Seidhe" ->
					text = "Leader Ability\nMove agile units to whichever valid row maximizes their strength " +
							"(don't move units already in optimal row).";
			case "Crach an Craite" ->
					text = "Leader Ability\nShuffle all cards from each player's graveyard back into their decks.";
			case "King Bran" -> text = "Leader Ability\nUnits only lose half their Strength in bad weather conditions.";
			case "REALMS" -> text = "Northern Realms\nDraw a card from your deck whenever you win a round.";
			case "NILFGAARD" -> text = "Nilfgaardian Empire\nWins any round that ends in a draw.";
			case "MONSTERS" -> text = "Monsters\nKeeps a random Unit Card out after each round.";
			case "SCOIATAEL" -> text = "Scoia'tael\nDecides who takes first turn.";
			case "SKELLIGE" -> text = "Skellige\n2 random cards from the graveyard are placed on the battlefield" +
					" at the start of the third round.";
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
