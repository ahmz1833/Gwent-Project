package org.apgrp10.gwent.client.view;

import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.utils.WaitExec;

import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class MessageGame extends Pane {
	private final Pane gamePane;
	private final Pane self = this;
	private WaitExec waitExec = new WaitExec(false);

	MessageGame(Pane gamePane, Image image, String txt) {
		this.gamePane = gamePane;
		setPrefWidth(PreGameMenu.screenWidth);
		setPrefHeight(PreGameMenu.screenHeight);
		this.setLayoutX(0);
		this.setLayoutY(0);
		setOnMouseClicked(Event::consume);
		addImageView(R.getImage("icons/black.png"), PreGameMenu.screenWidth, 100, 0, 300);
		addImageView(image, 200, 200, 300, 230);
		addText(txt);
	}

	public void show(int firstTime) {
		waitExec.run(firstTime, () -> gamePane.getChildren().add(self));
		waitExec.run(firstTime + 1000, () -> gamePane.getChildren().remove(self));
	}

	private void addImageView(Image image, int width, int height, int x, int y) {
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(width);
		imageView.setFitHeight(height);
		imageView.setX(x);
		imageView.setY(y);
		getChildren().add(imageView);
	}

	private void addText(String comment) {
		Text text = new Text(comment);
		text.setStyle("-fx-font-family: 'Yrsa SemiBold'; -fx-font-size: 50px");
		text.setFill(Color.GOLD);
		text.setY(365);
		text.setX(500);
		getChildren().add(text);
	}
}
