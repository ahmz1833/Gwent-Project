package org.apgrp10.gwent.client.model;

import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apgrp10.gwent.client.R;

public class ReactionChat extends Pane {
	private final long id;
	private final ChatPane chatPane;
	private final boolean isForOwner;
	private final int reaction;
	private final VBox menu = new VBox();

	public ReactionChat(int X, int Y, long id, ChatPane chatPane, boolean isForOwner, int reaction) {
		this.isForOwner = isForOwner;
		this.reaction = reaction;
		this.chatPane = chatPane;
		this.id = id;
		setSize();
		setupMenu(X, Y);
		setReactions();
		setReplyButton();
		if (isForOwner) {
			setEditButton();
			setDeleteButton();
		}
	}

	private void setDeleteButton() {
		ImageView imageView = new ImageView(R.getImage("chat/delete.png"));
		imageView.setFitWidth(80);
		imageView.setFitHeight(20);
		imageView.setOnMouseClicked(k -> chatPane.deleteMessage(id));
		menu.getChildren().add(imageView);
	}

	private void setEditButton() {
		ImageView imageView = new ImageView(R.getImage("chat/edit.png"));
		imageView.setFitWidth(80);
		imageView.setFitHeight(20);
		imageView.setOnMouseClicked(k -> chatPane.changeEditNumber(id));
		menu.getChildren().add(imageView);
	}

	private void setReplyButton() {
		ImageView imageView = new ImageView(R.getImage("chat/reply.png"));
		imageView.setFitWidth(80);
		imageView.setFitHeight(20);
		imageView.setOnMouseClicked(k -> chatPane.changeReplyNumber(id));
		menu.getChildren().add(imageView);
	}

	private void setSize() {
		this.setMinWidth(ChatPane.width);
		this.setMinHeight(ChatPane.height);
		this.setLayoutY(0);
		this.setLayoutX(0);
		chatPane.getChildren().add(this);
		this.setOnMouseClicked(k -> endShow());
	}

	private void setupMenu(int X, int Y) {
		menu.setPrefHeight(isForOwner ? 86 : 42);
		menu.setPrefWidth(80);
		menu.setSpacing(2);
		menu.setStyle("-fx-background-color: #727c7b");
		menu.setLayoutX(X + menu.getPrefWidth() > ChatPane.width ? ChatPane.width - menu.getPrefWidth() - 5 : X);
		menu.setLayoutY(Y + menu.getPrefHeight() > ChatPane.height ? ChatPane.height - menu.getPrefHeight() - 5 : Y);
		this.getChildren().add(menu);
	}

	private void setReactions() {
		HBox hBox = new HBox();
		hBox.setPrefHeight(20);
		hBox.setPrefWidth(50);
		for (int i = 0; i < 4; i++) {
			ImageView image = new ImageView(R.getImage("chat/emoji" + i + ".png"));
			image.setFitHeight(20);
			image.setFitWidth(20);
			StackPane stackPane = new StackPane(image);
			stackPane.setMaxWidth(20);
			stackPane.setMaxHeight(20);
			stackPane.setBackground(Background.fill(i == reaction ? Color.LIGHTBLUE : Color.WHITE));
			int finalI = i;
			image.setOnMouseClicked(k -> {
				removeReaction(reaction);
				if (finalI != reaction)
					createReaction(finalI);
				endShow();
			});
			hBox.getChildren().add(stackPane);
		}
		menu.getChildren().add(hBox);
	}


	private void endShow() {
		chatPane.getChildren().remove(this);
	}

	private void createReaction(int index) {
		chatPane.sendNewReaction(id, index);
	}

	private void removeReaction(int index) {
		if (index != -1) {
			chatPane.sendDeleteReaction(id, index);
		}
	}
}
