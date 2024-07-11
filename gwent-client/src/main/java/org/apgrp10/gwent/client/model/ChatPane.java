package org.apgrp10.gwent.client.model;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.ChatMenuController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.Message;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.ANSI;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ChatPane extends Pane {
	public final static int width = 250, height = 700;
	private final Text replyBox = new Text();
	private final TextArea textInput = new TextArea();
	private final VBox messagesBox = new VBox();
	private final int screenWidth;
	private final ChatMenuController controller;
	private final HashMap<Long, Integer> reactionList = new HashMap<>();
	private User.PublicInfo user;
	private final ImageView deleteReply = new ImageView(R.getImage("chat/clear.png"));
	private ScrollPane messagesScroll;
	//this is a map from each message id to reaction number
	private long replyId = 0;
	private long editID = 0;
	private StackPane massageReplyViw = new StackPane();

	public ChatPane(ChatMenuController controller) {
		updateUser();
		this.screenWidth = 250;
		this.controller = controller;
		setSize();
		setupDeleteReply();
		addTextInput();
		addMessagesBox();
	}

	private void updateUser(){
		User tmp = UserController.getCurrentUser();
		user = tmp != null? tmp.publicInfo(): new User.PublicInfo(666, "lucifer", "The Fallen Angel", Avatar.random());
	}

	public static StackPane getMessageReplyView(Message replyOn, User.PublicInfo user, boolean isReply, User.PublicInfo messageOwner) {
		String reply;
		if (isReply)
				reply = ("reply on " + (replyOn.getUserId() == user.id() ? "you" : messageOwner.nickname()) + ": " + replyOn.getText());
		else  reply = ("edit: " + replyOn.getText());
		if (reply.length() > 30) reply = (reply.substring(0, 30) + "...");
		Text text = new Text(reply);
		text.setWrappingWidth(140);
		text.setStyle("-fx-font-size: 10px");
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(Color.BLACK);
		Rectangle background = new Rectangle(140, 35, Color.LIGHTBLUE);
		background.setArcWidth(10);
		background.setArcHeight(10);
		StackPane container = new StackPane();
		container.setAlignment(Pos.CENTER);
		container.getChildren().add(background);
		container.getChildren().add(text);
		container.setMaxWidth(140);
		container.setMaxHeight(38);
		return container;
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

	private void setSize() {
		this.setLayoutX(0);
		this.setLayoutY(0);
		this.setPrefWidth(width);
		this.setPrefHeight(height);
		ImageView img = new ImageView(R.getImage("chat_bkg.png"));
		img.setFitHeight(height); img.setFitWidth(width);
		img.setX(0);
		img.setY(0);
		getChildren().add(img);
		this.setStyle("-fx-border-width: 2px;" +
		              "-fx-border-color: #0d17d7;");
		this.setOnMouseClicked(k -> this.requestFocus());
	}

	private void setupDeleteReply() {
		deleteReply.setFitWidth(20);
		deleteReply.setFitHeight(20);
		deleteReply.setLayoutX(150);
		deleteReply.setLayoutY(height - 60 - 32 - 15);
	}

	private void addTextInput() {
		textInput.setPromptText("send message");
		VBox container = new VBox();
		container.getChildren().add(setupText(replyBox));
		HBox hBox = new HBox();
		hBox.getChildren().add(setupTextFiled(textInput));
		hBox.getChildren().add(getImageToSend());
		container.getChildren().add(hBox);
		container.setLayoutX(5);
		container.setLayoutY(height - 60 - 32);
		this.getChildren().add(container);

	}

	private StackPane getStackPane() {
		StackPane stackPane = getStackPane(40, Pos.BOTTOM_CENTER);
		stackPane.setPrefHeight(60);
		return stackPane;
	}

	private StackPane getStackPane(int width, Pos pos) {
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(pos);
		stackPane.setMinWidth(width);
		stackPane.setMaxWidth(width);
		return stackPane;
	}

	private StackPane setupText(Text text) {
		StackPane container = getStackPane(240, Pos.CENTER);
		container.getChildren().add(text);
		text.setWrappingWidth(240);
		text.getStyleClass().add("replyText");
		return container;
	}

	private StackPane setupTextFiled(TextArea textField) {
		StackPane container = getStackPane(200, Pos.CENTER);
		container.getChildren().add(textField);
		textField.setPrefWidth(200);
		textField.setMaxHeight(60);
		textField.setWrapText(true);
		textField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				if (event.isShiftDown()) sendMessage();
			}
		});
		textField.getStyleClass().add("input");
		return container;
	}

	private StackPane getImageToSend() {
		StackPane container = getStackPane();
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
		if (textInput.getText().trim().equals("")) return;
		if (editID == 0) {
			controller.sendMessage(Message.newTextMessage(controller.getId(), textInput.getText(), user.id(), replyId));
		} else {
			controller.sendMessage(Message.editMessage(editID, textInput.getText(), user.id()));
		}
		changeReplyNumber(0);
		textInput.setText("");
		scrollToEnd();
	}

	private void scrollToEnd() {
		messagesScroll.layout();
		messagesScroll.setVvalue(1.0);
	}

	public void addMessage(String  messageS) {
		Message message = Message.fromString(messageS);
		try {
			if (message.getType() == (byte) 0) {
				MessageView messageView;
				try {
					messageView = new MessageView(message, user, Objects.requireNonNull(getMessageById(message.getReplyOn())).getMessage());
				} catch (NullPointerException ignored) {
					messageView = new MessageView(message, user, null);
				}
				MessageView finalMessageView = messageView;
				messageView.setOnMouseClicked(k -> {
					if (k.getButton() == MouseButton.SECONDARY)
						openNewWindow(k.getSceneX(), k.getSceneY(), finalMessageView.getMessage().getId());
				});
				reactionList.put(message.getId(), -1);
				messagesBox.getChildren().add(messageView);
				if (messagesScroll.getVvalue() > 0.9) scrollToEnd();
			} else if (message.getType() == (byte) 1) {
				MessageView target = getMessageById(message.getId());
				Objects.requireNonNull(target).increaseReaction(message.getNumberOfReaction());
			} else if (message.getType() == (byte) 2) {
				MessageView messageView = getMessageById(message.getId());
				messagesBox.getChildren().remove(messageView);
				if (replyId == message.getId()) {
					changeReplyNumber(0);
				}
				if (editID == message.getId()) {
					changeEditNumber(0);
				}
				for (Node node : messagesBox.getChildren()) {
					((MessageView) node).deleteReply(message.getId());
				}
			} else if (message.getType() == (byte) 3) {
				MessageView target = getMessageById(message.getId());
				Objects.requireNonNull(target).decreaseReaction(message.getNumberOfReaction());
			} else if (message.getType() == (byte) 4) {
				MessageView target = getMessageById(message.getId());
				Objects.requireNonNull(target).changeText(message.getText());
				if (replyId == message.getId()) changeReplyNumber(replyId);
				if (editID == message.getId()) changeEditNumber(editID);
				for (Node node : messagesBox.getChildren()) {
					((MessageView) node).editReply(message.getId(), message.getText());
				}
			}
		} catch (Exception e) {
			ANSI.logError(System.err, null, e);
		}
	}


	private MessageView getMessageById(long id) {
		for (Node node : messagesBox.getChildren()) {
			if (id == ((MessageView) (node)).getMessage().getId()) {
				return ((MessageView) node);
			}
		}
		return null;
	}

	private void openNewWindow(double X, double Y, long id) {
		new ReactionChat((int) (X - screenWidth + width), (int) Y, id, this, user.id() == (id), reactionList.get(id));
	}

	public void sendDeleteReaction(long id, int index) {
		reactionList.put(id, -1);
		controller.sendMessage(Message.deleteReactionMessage(id, index, user.id()));
	}

	public void sendNewReaction(long id, int index) {
		reactionList.put(id, index);
		controller.sendMessage(Message.newReactionMessage(id, index, user.id()));
	}

	public void changeReplyNumber(long id) {
		this.replyId = id;
		this.editID = 0;
		addInfoTopInput(true, id);
	}

	public void changeEditNumber(long id) {
		this.editID = id;
		this.replyId = 0;
		addInfoTopInput(false, id);
	}

	private void addInfoTopInput(boolean isReply, long id) {
		try {
			this.getChildren().remove(massageReplyViw);
			this.getChildren().remove(deleteReply);
			if (id == 0) return;
			textInput.requestFocus();
			Message editOn = Objects.requireNonNull(getMessageById(id)).getMessage();
			massageReplyViw = getMessageReplyView(editOn, user, isReply);
			massageReplyViw.setLayoutX(5);
			massageReplyViw.setLayoutY(height - 60 - 32 - 25);
			this.getChildren().add(massageReplyViw);
			deleteReply.setOnMouseClicked(isReply ? k -> changeReplyNumber(0) : k -> changeEditNumber(0));
			this.getChildren().add(deleteReply);
			if (!isReply) {
				textInput.setText(Objects.requireNonNull(getMessageById(id)).getMessage().getText());
			}
		} catch (Exception e) {
			ANSI.logError(System.err, "", e);
		}
	}

	public void deleteMessage(long id) {
		controller.sendMessage(Message.deleteTextMessage(id, user.id()));
	}
}
