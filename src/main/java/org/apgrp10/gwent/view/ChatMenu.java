package org.apgrp10.gwent.view;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.controller.ChatMenuController;
import org.apgrp10.gwent.model.Massage.Message;
import org.apgrp10.gwent.model.Massage.MessageView;
import org.apgrp10.gwent.model.User;

import java.util.HashMap;
import java.util.Objects;

public class ChatMenu extends Pane {
	private final Text replyBox = new Text();
	private final TextArea textInput = new TextArea();
	private final VBox messagesBox = new VBox();
	private MFXScrollPane messagesScroll;
	public final static int width = 250, height = 720;
	private final int screenWidth;
	private final ChatMenuController controller;
	private final HashMap<Integer, Integer> reactionList = new HashMap<>();
	//this is a map from each message id to reaction number
	private final User user;

	public ChatMenu(double screenWidth, ChatMenuController controller, User user) {
		this.user = user;
		this.screenWidth = (int) screenWidth;
		this.controller = controller;
		setSize(screenWidth);
		addTextInput();
		addMessagesBox();
	}

	private void addMessagesBox() {
		messagesBox.setLayoutX(0);
		messagesBox.setLayoutY(0);
		messagesBox.setSpacing(10);
		messagesBox.setPrefWidth(width - 10);
		messagesBox.setPrefHeight(height - 140);
		messagesScroll = new MFXScrollPane();
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
		textInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() > 35) {
				textInput.setText(oldValue);
			}
			if (newValue.contains("\n")) {
				textInput.setText(oldValue);
			}
		});
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

	private StackPane setupTextFiled(TextArea textField, int width, int height,
									 Pos pos, String styleClass) {
		StackPane container = getStackPane(width, pos);
		container.getChildren().add(textField);
		textField.setPrefWidth(width);
		textField.setMaxHeight(height);
		textField.setWrapText(true);
		textField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
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
		container.setOnMouseClicked(k -> {
			messagesScroll.requestFocus();
			k.consume();
		});
		image.setOnMouseClicked(k -> sendMessage());
		container.getChildren().add(image);
		return container;
	}

	private void sendMessage() {
		if (textInput.getText().trim().equals(""))
			return;
		//TODO
		User user1 = Math.random() > 0.5 ? user : new User("user", "b", "c", "d");
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
			messageView.setOnMouseClicked(k -> {
				if (k.getButton() == MouseButton.SECONDARY)
					openNewWindow(k.getSceneX(), k.getSceneY(), messageView.getMessage().getId());
			});
			reactionList.put(message.getId(), -1);
			messagesBox.getChildren().add(messageView);
			if (messagesScroll.getVvalue() > 0.9)
				scrollToEnd();
		}
		else if (message.getType() == (byte) 1) {
			try {
				MessageView target = getMessageById(message.getId());
				Objects.requireNonNull(target).increaseReaction(message.getNumberOfReaction());
			} catch (NullPointerException ignored) {
			}
		}
		else if (message.getType() == (byte) 3) {
			try {
				MessageView target = getMessageById(message.getId());
				Objects.requireNonNull(target).decreaseReaction(message.getNumberOfReaction());
			} catch (NullPointerException ignored) {
			}
		}

	}

	private User getUserById(int id) {
		try {
			return Objects.requireNonNull(getMessageById(id)).getMessage().getOwner();
		} catch (NullPointerException e) {
			return null;
		}
	}

	private MessageView getMessageById(int id) {
		for (Node node : messagesBox.getChildren()) {
			if (id == ((MessageView) (node)).getMessage().getId()) {
				return ((MessageView) node);
			}
		}
		return null;
	}

	private void openNewWindow(double X, double Y, int id) {
		new ReactionChat((int) (X - screenWidth + width), (int) Y, id, this,
				user.equals(getUserById(id)), reactionList.get(id));
	}

	public void sendDeleteReaction(int id, int index) {
		reactionList.put(id, -1);
		controller.sendMessage(Message.deleteReactionMessage(id, index, user));
	}

	public void sendNewReaction(int id, int index) {
		reactionList.put(id, index);
		controller.sendMessage(Message.newReactionMessage(id, index, user));
	}
}
