package org.apgrp10.gwent.model.card;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.*;
import org.apgrp10.gwent.view.PreGameMenu;


public class CardViewPregame extends Pane {
	private String address;
	private String name;
	private Text countLabel;
	private int count = 1;
	private int strength;
	private Faction faction;
	private boolean hero;

	public CardViewPregame(String address) {
		this.address = address;
		fillInfoByName();
		addBackground();
		addSymbol();
		addCountLabel();
	}

	private void fillInfoByName() {
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.pathAddress.equals(address)) {
				faction = cardInfo.faction;
				name = cardInfo.name;
				strength = cardInfo.strength;
				hero = cardInfo.isHero;
			}
		}
	}

	private void addBackground() {
		Rectangle image = new Rectangle(PreGameMenu.screenWidth / (double) PreGameMenu.cardWidth,
				PreGameMenu.screenHeight / (double) PreGameMenu.cardHeight);
		image.setFill(new ImagePattern(R.getImage("lg/" + address + ".jpg")));
		image.setX(0);
		image.setY(0);
		this.getChildren().add(image);
	}

	private void addSymbol() {
		ImageView image = new ImageView(R.getImage("icons/preview_count.png"));
		image.setX(PreGameMenu.screenWidth / (double) PreGameMenu.cardWidth - 52);
		image.setY(PreGameMenu.screenHeight / (double) PreGameMenu.cardHeight - 30);
		this.getChildren().add(image);
	}

	private void addCountLabel() {
		countLabel = new Text(String.valueOf(count));
		countLabel.setStyle("-fx-font-size: 22px;");
		countLabel.setFill(Color.BLUE);
		countLabel.setX(PreGameMenu.screenWidth / (double) PreGameMenu.cardWidth - 20);
		countLabel.setY(PreGameMenu.screenHeight / (double) PreGameMenu.cardHeight - 10);
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
		if (!(obj instanceof CardViewPregame))
			return false;
		return ((CardViewPregame) obj).address.equals(this.address);
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
	public boolean isHero(){
		return hero;
	}
}
