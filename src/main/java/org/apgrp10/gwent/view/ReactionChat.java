package org.apgrp10.gwent.view;

import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apgrp10.gwent.R;

public class ReactionChat extends Pane {
	private final int id;
	private final ChatMenu chatMenu;
	private final boolean isForOwner;
	private final int reaction;
	private final VBox menu = new VBox();

	public ReactionChat(int X, int Y, int id, ChatMenu chatMenu, boolean isForOwner, int reaction) {
		this.isForOwner = isForOwner;
		this.reaction = reaction;
		this.chatMenu = chatMenu;
		this.id = id;
		setSize();
		setupMenu(X, Y);
		setReactions();
	}

	private void setSize() {
		this.setMinWidth(ChatMenu.width);
		this.setMinHeight(ChatMenu.height);
		this.setLayoutY(0);
		this.setLayoutX(0);
		chatMenu.getChildren().add(this);
		this.setOnMouseClicked(k -> endShow());
	}

	private void setupMenu(int X, int Y) {
		menu.setPrefHeight(isForOwner ? 160 : 80);
		menu.setPrefWidth(160);
		menu.setStyle("-fx-background-color: #727c7b");
		menu.setLayoutX(X + menu.getPrefWidth() > ChatMenu.width ? ChatMenu.width - menu.getPrefWidth() - 5 : X);
		menu.setLayoutY(Y + menu.getPrefHeight() > ChatMenu.height ? ChatMenu.height - menu.getPrefHeight() - 5 : Y);
		this.getChildren().add(menu);
	}

	private void setReactions() {
		HBox hBox = new HBox();
		hBox.setPrefHeight(40);
		hBox.setPrefWidth(100);
		for (int i = 0; i < 4; i++) {
			ImageView image = new ImageView(R.getImage("chat/emoji" + i + ".png"));
			image.setFitHeight(40);
			image.setFitWidth(40);
			StackPane stackPane = new StackPane(image);
			stackPane.setMaxWidth(40);
			stackPane.setMaxHeight(40);
			stackPane.setBackground(Background.fill(i == reaction ? Color.LIGHTBLUE : Color.WHITE));
			int finalI = i;
			image.setOnMouseClicked(k -> {
				removeReaction(reaction);
				if(finalI != reaction)
					createReaction(finalI);
				endShow();
			});
			hBox.getChildren().add(stackPane);
		}
		menu.getChildren().add(hBox);
	}


	private void endShow() {
		chatMenu.getChildren().remove(this);
	}

	private void createReaction(int index) {
		chatMenu.sendNewReaction(id, index);
	}

	private void removeReaction(int index) {
		if (index != -1) {
			chatMenu.sendDeleteReaction(id, index);
		}
	}
}
