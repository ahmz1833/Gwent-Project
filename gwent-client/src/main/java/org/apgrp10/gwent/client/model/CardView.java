package org.apgrp10.gwent.client.model;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.model.card.Ability;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;

public class CardView extends Pane {
	private String address;
	private String name;
	private Text countLabel;
	private int count = 1;
	private int strength;
	private Faction faction;
	private boolean hero;
	private double width;
	private double height;
	private String rootPath;
	private boolean simpleEquals;
	private Row row;
	private Ability ability;

	private CardView(String address, boolean large, boolean withCount, boolean simpleEquals, int strength, boolean showStrength, double width, double height) {
		this.address = address;
		this.width = width;
		this.height = height;
		this.strength = strength;
		this.simpleEquals = simpleEquals;
		rootPath = large ? "lg/" : "sm/";
		fillInfoByName();
		if (strength == -1)
			strength = this.strength;
		addBackground();
		if (withCount) {
			addSymbol();
			addCountLabel();
		}
		if (showStrength && (strength != 0 || name.equals("Havekar Healer") || name.equals("Kambi") ||
				name.equals("Cow") || name.equals("Siege Technician") || name.equals("Mysterious Elf"))) {
			// TODO: make this better
			addScoreText(strength);
			addPlaceImage();
			addAbilityImage();
		}
		if (showStrength && (faction == Faction.WEATHER || faction == Faction.SPECIAL)) {
			addWeatherScore();
		}
		this.strength = strength;

	}

	private void addWeatherScore() {
		String path = "icons/" + switch (name) {
			case "Torrential Rain" -> "power_rain.png";
			case "Skellige Storm", "kellige Storm" -> "power_storm.png";
			case "Biting Frost" -> "power_frost.png";
			case "Clear Weather" -> "power_clear.png";
			case "Impenetrable Fog" -> "power_fog.png";
			case "Mardroeme" -> "power_mardroeme.png";
			case "Decoy" -> "power_decoy.png";
			case "Commander's Horn" -> "power_horn.png";
			case "Scorch" -> "power_scorch.png";
			default -> "";
		};
		getChildren().add(getImageView(width / 2 + 13, height / 2 + 5, path));
	}

	private void addScoreText(int strength) {
		getChildren().add(getImageView(width / 2 + 13, height / 2 + 5, "icons/" + (isHero() ? "power_hero.png" : "power_normal.png")));
		StackPane numberContainer = getStackPane(16, 16, 3, 3, Pos.CENTER);
		Text text = new Text(String.valueOf(strength));
		text.setStyle("-fx-font-size: 12px");
		//TODO change font
		text.setStyle("-fx-font-family: 'Comfortaa SemiBold' ");
		text.setLayoutY(height);
		if (strength < this.strength) text.setFill(Color.color(1, 0, 0));
		if (strength == this.strength) text.setFill(!isHero() ? Color.color(0, 0, 0) : Color.color(1, 1, 1));
		if (strength > this.strength) text.setFill(isHero() ? Color.color(0, 1, 0) : Color.rgb(20, 100, 20));
		text.setX(5);
		text.setY(-height + 16);
		numberContainer.getChildren().add(text);
		getChildren().add(numberContainer);
	}

	private void addPlaceImage() {
		ImageView image = getImageView(17, 17, "icons/" + switch (row) {
			case RANGED -> "card_row_ranged.png";
			case CLOSED -> "card_row_close.png";
			case AGILE -> "card_row_agile.png";
			case SIEGE -> "card_row_siege.png";
			default -> "";
		});
		image.setX(width - 17);
		image.setY(height - 17);
		getChildren().add(image);
	}

	private void addAbilityImage() {
		String name = switch (ability) {
			case SPY -> "card_ability_spy.png";
			case MUSTER -> "card_ability_muster.png";
			case MORALE -> "card_ability_morale.png";
			case MEDIC -> "card_ability_medic.png";
			case MARDROEME -> "card_ability_mardroeme.png";
			case BOND -> "card_ability_bond.png";
			case BERSERKER -> "card_ability_berserker.png";
			default -> "";
		};
		if (name.isEmpty() && row == Row.AGILE)
			name = "card_ability_agile.png";
		if (name.isEmpty())
			return;
		ImageView image = getImageView(17, 17, "icons/" + name);
		image.setX(width - 35);
		image.setY(height - 17);
		getChildren().add(image);
	}

	private ImageView getImageView(double width, double height, String path) {
		ImageView image = new ImageView(R.getImage(path));
		image.setFitWidth(width);
		image.setFitHeight(height);
		image.setY(-2);
		image.setX(-2);
		return image;
	}

	private StackPane getStackPane(double width, double height, double x, double y, Pos pos) {
		StackPane pane = new StackPane();
		pane.setPrefWidth(width);
		pane.setPrefHeight(height);
		pane.setLayoutY(y);
		pane.setLayoutX(x);
		pane.setAlignment(pos);
		return pane;
	}

	public static CardView newSelection(String address, double width, double height) {
		return new CardView(address, true, true, false, -1, false, width, height);
	}

	public static CardView newHand(String address, double width, double height) {
		return new CardView(address, false, false, true, -1, true, width, height);
	}

	public static CardView newInBoard(String address, int strength, double width, double height) {
		return new CardView(address, false, false, true, strength, true, width, height);
	}

	public static CardView newInfo(String address, double width, double height) {
		return new CardView(address, true, false, true, -1, false, width, height);
	}

	private void fillInfoByName() {
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.pathAddress.equals(address)) {
				faction = cardInfo.faction;
				name = cardInfo.name;
				strength = cardInfo.strength;
				hero = cardInfo.isHero;
				row = cardInfo.row;
				ability = cardInfo.ability;
			}
		}
	}

	private void addBackground() {
		Rectangle image = new Rectangle(width, height);
		image.setFill(new ImagePattern(R.getImage(rootPath + address + ".jpg")));
		image.setX(0);
		image.setY(0);
		this.getChildren().add(image);
	}

	private void addSymbol() {
		ImageView image = new ImageView(R.getImage("icons/preview_count.png"));
		image.setX(width - 52);
		image.setY(height - 46);
		this.getChildren().add(image);
	}

	private void addCountLabel() {
		countLabel = new Text(String.valueOf(count));
		countLabel.setStyle("-fx-font-size: 22px; " +
		                    "-fx-font-family: 'Comfortaa Bold';");
		countLabel.setFill(Color.rgb(71,31,19));
		countLabel.setX(width - 20);
		countLabel.setY(height - 26);
		this.getChildren().add(countLabel);
	}

	public void countPlusPlus() {
		countLabel.setText(String.valueOf(++count));
	}

	public void countMinusMinus() {
		countLabel.setText(String.valueOf(--count));
	}

	public int getCount() {
		return count;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public boolean equals(Object obj) {
		if (simpleEquals)
			return this == obj;
		if (!(obj instanceof CardView))
			return false;
		return ((CardView) obj).address.equals(this.address);
	}

	public String getName() {
		return name;
	}

	public int getStrength() {
		return strength;
	}

	public Faction getFaction() {
		return faction;
	}

	public boolean isHero() {
		return hero;
	}
}

