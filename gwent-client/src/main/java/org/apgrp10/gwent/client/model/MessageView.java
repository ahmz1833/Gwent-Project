package org.apgrp10.gwent.client.model;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.model.Message;
import org.apgrp10.gwent.model.User;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MessageView extends HBox {
	private final Message message;
	private final User user;
	private VBox messageBox;
	private final HBox[] reactions = new HBox[4];
	private final HBox allReactions = new HBox();
	private Message replyOn;
	private final Text messageText = new Text();
	private final StackPane mainPain = new StackPane(), backPane = new StackPane();
	private final Text time = new Text();
	boolean edited = false;

	public MessageView(Message message, User user, Message replyOn) {
		this.replyOn = replyOn;
		this.message = message;
		this.user = user;
		this.setPrefWidth(ChatMenu.width - 30);
		addImage();
		addMessage();
		addUserName();
		addReply();
		addText();
		fillReactions();
		updateReactions();
		messageBox.getChildren().add(allReactions);
		setupTime();
	}

	private void setupTime() {
		try {
			StackPane pane = new StackPane();
			pane.setPrefWidth(140);
			pane.setPrefHeight(20);
			pane.setAlignment(Pos.BOTTOM_RIGHT);
			time.setFill(Color.GRAY);
			time.setStyle("-fx-font-size: 8px");
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withLocale(Locale.UK).withZone(ZoneId.systemDefault());
			String formattedInstant = formatter.format(message.getCreationTime());
			time.setText(formattedInstant);
			pane.getChildren().add(time);
			messageBox.getChildren().add(pane);
		} catch (Exception ignored) {

		}
	}

	private void fillReactions() {
		for (int i = 0; i < 4; i++) {
			reactions[i] = new HBox();
			ImageView image = new ImageView(R.getImage("chat/emoji" + i + ".png"));
			image.setFitHeight(14);
			image.setFitWidth(14);
			reactions[i].getChildren().add(image);
			Text text = new Text(String.valueOf(0));
			text.setFill(Color.DARKMAGENTA);
			text.setStyle("-fx-font-size: 12px");
			reactions[i].getChildren().add(text);
			reactions[i].setSpacing(3);
			reactions[i].setMaxWidth(40);
		}
	}

	private void addReply() {
		if (replyOn != null) {
			messageBox.getChildren().add(ChatMenu.getMessageReplyView(replyOn, user, true));
		}
	}

	public void deleteReply(int id) {
		if (replyOn != null) {
			if (replyOn.getId() == id) {
				messageBox.getChildren().remove(1);
				replyOn = null;
			}
		}
	}

	public void editReply(int id, String text) {
		if (replyOn != null) {
			try {
				if (replyOn.getId() == id) {
					Text reply = ((Text) (((StackPane) messageBox.getChildren().get(1)).getChildren().get(1)));
					String string = "reply on you: " + text;
					if (string.length() > 30) string = string.substring(0, 30) + "...";
					reply.setText(string);
				}
			} catch (Exception ignored) {
			}
		}
	}

	private void updateReactions() {
		allReactions.getChildren().clear();
		allReactions.setSpacing(7);
		List<HBox> sorted = new java.util.ArrayList<>(Arrays.stream(reactions).sorted((o1, o2) -> {
			try {
				int count1 = Integer.parseInt(((Text) (o1.getChildren().get(1))).getText());
				int count2 = Integer.parseInt(((Text) (o2.getChildren().get(1))).getText());
				return Integer.compare(count1, count2);
			} catch (Exception ignored) {
				return 0;
			}
		}).toList());
		Collections.reverse(sorted);
		for (HBox reaction : sorted) {
			if (Integer.parseInt(((Text) (reaction.getChildren().get(1))).getText()) > 0)
				allReactions.getChildren().add(reaction);
		}
	}

	public Message getMessage() {
		return message;
	}

	private Node getImage() {
		//TODO set avatar image of message.getOwner in here instead of sample image
		ImageView imageView = new ImageView(R.getImage("icons/card_ability_frost.png"));
		imageView.setFitWidth(30);
		imageView.setFitHeight(30);
		return imageView;
	}

	private void addImage() {
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.BOTTOM_CENTER);
		stackPane.getChildren().add(getImage());
		this.getChildren().add(stackPane);
		this.setSpacing(5);
	}

	private void addMessage() {
		messageBox = new VBox();
		Rectangle background = new Rectangle();
		if (!user.equals(message.getOwner())) background.setFill(Color.rgb(238, 180, 114));
		else background.setFill(Color.rgb(141, 227, 118));
		background.setArcWidth(20);
		background.setArcHeight(20);
		background.setWidth(160);
		backPane.getChildren().add(background);
		backPane.getChildren().add(messageBox);
		background.heightProperty().bind(backPane.heightProperty());
		messageBox.setStyle("-fx-padding: 5 5 5 5;");
		mainPain.setAlignment(Pos.BOTTOM_CENTER);
		mainPain.getChildren().add(backPane);
		this.getChildren().add(mainPain);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setColor(Color.BLUE);
		dropShadow.setRadius(10);
		dropShadow.setOffsetX(5);
		dropShadow.setOffsetY(5);
		background.setEffect(dropShadow);
	}

	private void addUserName() {
		Text username;
		if (user.equals(message.getOwner())) {
			username = new Text("you:");
			username.setFill(Color.RED);

		} else {
			username = new Text(message.getOwner().getNickname() + ":");
			username.setFill(Color.GREEN);
		}
		username.setWrappingWidth(150);
		username.setStyle("-fx-font-size: 14px");
		messageBox.getChildren().add(username);
	}

	private void addText() {
		messageText.setText(message.getText().trim() + "\n");
		messageText.setWrappingWidth(150);
		messageText.setFill(Color.BLACK);
		messageText.setStyle("-fx-font-size: 12px");
		messageBox.getChildren().add(messageText);
	}

	public void increaseReaction(int index) {
		int count = Integer.parseInt(((Text) (reactions[index].getChildren().get(1))).getText());
		count++;
		((Text) (reactions[index].getChildren().get(1))).setText(String.valueOf(count));
		updateReactions();
	}

	public void decreaseReaction(int index) {
		int count = Integer.parseInt(((Text) (reactions[index].getChildren().get(1))).getText());
		count--;
		if (count >= 0) ((Text) (reactions[index].getChildren().get(1))).setText(String.valueOf(count));
		updateReactions();
		messageBox.requestLayout();
		mainPain.requestLayout();
		backPane.requestLayout();
	}

	public void changeText(String text) {
		messageText.setText(text.trim() + "\n");
		message.setText(text.trim());
		messageBox.requestLayout();
		backPane.requestLayout();
		mainPain.requestLayout();
		if (!edited) {
			time.setText("\"EDITED\" " + time.getText());
			edited = true;
		}
	}
}
