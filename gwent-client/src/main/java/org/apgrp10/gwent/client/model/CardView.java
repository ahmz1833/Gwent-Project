package org.apgrp10.gwent.client.model;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;

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

	private CardView(String address, boolean large, boolean withCount, boolean simpleEquals, int strength, double width, double height) {
		this.address = address;
		this.width = width;
		this.height = height;
		this.strength = strength;
		this.simpleEquals = simpleEquals;
		rootPath = large? "lg/": "sm/";
		fillInfoByName();
		addBackground();
		addSymbol();
		if (withCount)
			addCountLabel();
	}

	public static CardView newSelection(String address, double width, double height) {
		return new CardView(address, true, true, false, -1, width, height);
	}
	public static CardView newHand(String address, double width, double height) {
		return new CardView(address, false, false, true, -1, width, height);
	}
	public static CardView newInfo(String address, double width, double height) {
		return new CardView(address, true, false, true, -1, width, height);
	}

	private void fillInfoByName() {
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.pathAddress.equals(address)) {
				faction = cardInfo.faction;
				name = cardInfo.name;
				if (strength == -1)
					strength = cardInfo.strength;
				hero = cardInfo.isHero;
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
		image.setY(height - 30);
		this.getChildren().add(image);
	}

	private void addCountLabel() {
		countLabel = new Text(String.valueOf(count));
		countLabel.setStyle("-fx-font-size: 22px;");
		countLabel.setFill(Color.BLUE);
		countLabel.setX(width - 20);
		countLabel.setY(height - 10);
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
	public boolean isHero(){
		return hero;
	}
}

