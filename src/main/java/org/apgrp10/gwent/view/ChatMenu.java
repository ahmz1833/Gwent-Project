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

public class ChatMenu extends Pane {
	private final Text replyBox = new Text();
	private final TextArea textInput = new TextArea();
	private final VBox massagesBox = new VBox();
	private ScrollPane massagesScroll;
	public final static int width = 250, height = 720;

	public ChatMenu(double screenWidth) {
		setSize(screenWidth);
		addTextInput();
		addMassagesBox();
	}

	private void addMassagesBox() {
		massagesBox.setLayoutX(0);
		massagesBox.setLayoutY(0);
		massagesBox.setPrefWidth(width - 10);
		massagesBox.setPrefHeight(height - 140);
		massagesScroll = new ScrollPane(massagesBox);
		massagesScroll.setOnMouseClicked(k -> {
			massagesScroll.requestFocus();
			k.consume();
		});
		massagesScroll.getStyleClass().add("massagesBox");
		massagesScroll.setFitToWidth(true);
		massagesScroll.setLayoutY(5);
		massagesScroll.setLayoutX(5);
		getChildren().add(massagesScroll);
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
		textInput.setPromptText("send massage");
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
		stackPane.setPrefWidth(width);
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
					sendMassage();
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
		image.setOnMouseClicked(k -> {
			sendMassage();
			massagesScroll.requestFocus();
		});
		container.getChildren().add(image);
		return container;
	}

	private void sendMassage() {
		if (textInput.getText().trim().equals(""))
			return;
		textInput.setText("");
		//TODO
		massagesBox.getChildren().add(new Rectangle(200, 50, Color.BLUE));
		scrollToEnd();
	}

	private void scrollToEnd() {
		massagesScroll.layout();
		massagesScroll.setVvalue(1.0);
	}

	private void addMassage() {
		//TODO
		if (massagesScroll.getVvalue() > 0.9)
			scrollToEnd();
	}
}
