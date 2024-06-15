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
	private final String name;
	private String address;
	private Text countLabel;
	private int count;

	public CardViewPregame(String name) {
		this.name = name;
		fillInfoByName();
		addBackground();
		addSymbol();
		addCountLabel();
	}

	private void fillInfoByName() {
		for (CardInfo card : CardInfo.allCards) {
			if (card.name.equals(name)) {
				this.address = card.pathAddress;
				count = card.count;
				break;
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

	@Override
	public String toString() {
		return name;
	}
}
