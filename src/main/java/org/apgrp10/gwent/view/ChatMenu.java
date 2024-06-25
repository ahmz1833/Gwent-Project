package org.apgrp10.gwent.view;

import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.controller.ChatMenuController;
import org.apgrp10.gwent.model.Massage.Message;
import org.apgrp10.gwent.model.Massage.MessageView;
import org.apgrp10.gwent.model.User;

public class ChatMenu extends Pane {
	private final Text replyBox = new Text();
	private final TextArea textInput = new TextArea();
	private final VBox messagesBox = new VBox();
	private ScrollPane messagesScroll;
	public final static int width = 250, height = 720;
	private final ChatMenuController controller;
	private final User user;

	public ChatMenu(double screenWidth, ChatMenuController controller, User user) {
		this.user = user;
		this.controller = controller;
		setSize(screenWidth);
		addTextInput();
		addMessagesBox();
	}

	private void addMessagesBox() {
		messagesBox.setLayoutX(0);
		messagesBox.setLayoutY(0);
		messagesBox.setSpacing(20);
		messagesBox.setPrefWidth(width - 10);
		messagesBox.setPrefHeight(height - 140);
		messagesScroll = new ScrollPane();
		messagesScroll.setContent(messagesBox);
		messagesScroll.setOnMouseClicked(k -> {
			messagesScroll.requestFocus();
			k.consume();
		});
		messagesScroll.getStyleClass().add("messagesBox");
		messagesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		messagesScroll.setFitToWidth(true);
		messagesScroll.setFitToHeight(true);
		messagesScroll.setLayoutY(5);
		messagesScroll.setLayoutX(5);
		getChildren().add(messagesScroll);
	}

	private void setSize(double screenWidth) {
		this.setLayoutX(screenWidth - 250);
		this.setLayoutY(0);
		this.setPrefWidth(width);
		this.setPrefHeight(height);
		this.getStyleClass().add("chatPane");
		this.setOnMouseClicked(k -> this.requestFocus());
	}

	private void addTextInput() {
		textInput.setPromptText("send message");
		VBox container = new VBox();
		container.getChildren().add(setupText(replyBox, 240, Pos.CENTER, "replyText"));
		HBox hBox = new HBox();
		hBox.getChildren().add(setupTextFiled(textInput, 200, 60, Pos.CENTER, "input"));
		hBox.getChildren().add(getImageToSend());
		container.getChildren().add(hBox);
		container.setLayoutX(5);
		container.setLayoutY(height - 60 - 32);
		this.getChildren().add(container);

	}

	private StackPane getStackPane(int width, int height, Pos pos) {
		StackPane stackPane = getStackPane(width, pos);
		stackPane.setPrefHeight(height);
		return stackPane;
	}

	private StackPane getStackPane(int width, Pos pos) {
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(pos);
		stackPane.setMinWidth(width);
		stackPane.setMaxWidth(width);
		return stackPane;
	}

	private StackPane setupText(Text text, int width, Pos pos, String styleClass) {
		StackPane container = getStackPane(width, pos);
		container.getChildren().add(text);
		text.setWrappingWidth(width);
		text.getStyleClass().add(styleClass);
		return container;
	}

	private StackPane setupTextFiled(TextArea textField, int width, int height, Pos pos, String styleClass) {
		StackPane container = getStackPane(width, pos);
		container.getChildren().add(textField);
		textField.setPrefWidth(width);
		textField.setMaxHeight(height);
		textField.setWrapText(true);
		textField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				event.consume();
				if (event.isShiftDown())
					sendMessage();
			}
		});
		textField.getStyleClass().add(styleClass);
		return container;
	}

	private StackPane getImageToSend() {
		StackPane container = getStackPane(40, 60, Pos.BOTTOM_CENTER);
		ImageView image = new ImageView(R.getImage("chat/send.png"));
		image.setFitHeight(40);
		image.setFitWidth(40);
		container.setOnMouseClicked(k -> {messagesScroll.requestFocus(); k.consume();});
		image.setOnMouseClicked(k -> sendMessage());
		container.getChildren().add(image);
		return container;
	}

	private void sendMessage() {
		if (textInput.getText().trim().equals(""))
			return;
		//TODO
		User user1 = Math.random() > 0.5? user : new User("user", "b", "c", "d");
		controller.sendMessage(Message.newTextMessage(controller.getId(), textInput.getText(), user1));
		textInput.setText("");
		scrollToEnd();
	}

	private void scrollToEnd() {
		messagesScroll.layout();
		messagesScroll.setVvalue(1.0);
	}

	public void addMessage(Message message) {
		if (message.getType() == (byte) 0) {
			MessageView messageView = new MessageView(message, user);
			StackPane stackPane = new StackPane(messageView);
			messagesBox.getChildren().add(stackPane);
			if (messagesScroll.getVvalue() > 0.9)
				scrollToEnd();
		}

	}
}
