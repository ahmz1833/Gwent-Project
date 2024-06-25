package org.apgrp10.gwent.model.Massage;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.ChatMenu;

public class MessageView extends HBox {
	private final Message message;
	private final User user;
	private VBox messageBox;

	public MessageView(Message message, User user) {
		this.message = message;
		this.user = user;
		this.setPrefWidth(ChatMenu.width - 30);
		addImage();
		addMessage();
		addUserName();
		addText();
		messageBox.setMinHeight(50);
	}
	private Node getImage(){
		//TODO set avatar image of message.getOwner in here instead of sample image
		ImageView imageView = new ImageView(R.getImage("icons/card_ability_frost.png"));
		imageView.setFitWidth(30);
		imageView.setFitHeight(30);
		return imageView;
	}
	private void addImage(){
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.BOTTOM_CENTER);
		stackPane.getChildren().add(getImage());
		this.getChildren().add(stackPane);
		this.setSpacing(5);
	}
	private void addMessage(){
		Pane pane = new Pane();
		messageBox = new VBox();
		Rectangle background = new Rectangle();
		if(!user.equals(message.getOwner()))
			background.setFill(Color.rgb(238,180, 114));
		else
			background.setFill(Color.rgb(141,227, 118));
		background.setArcWidth(20);
		background.setArcHeight(20);
		background.setWidth(160);
		StackPane stackPane = new StackPane();
		stackPane.getChildren().add(background);
		stackPane.getChildren().add(messageBox);
		background.heightProperty().bind(stackPane.heightProperty());
		pane.getChildren().add(stackPane);
		messageBox.setStyle("-fx-padding: 5 5 5 5;");
		this.getChildren().add(pane);
	}
	private void addUserName(){
		Text username;
		if(user.equals(message.getOwner())) {
			username = new Text("YOU:");
			username.setFill(Color.RED);

		}
		else {
			username = new Text(message.getOwner().getUsername() + ":");
			username.setFill(Color.GREEN);
		}
		username.setWrappingWidth(150);
		username.setStyle("-fx-font-size: 14px");
		messageBox.getChildren().add(username);
	}
	private void addText(){
		Text text = new Text(message.getText() + "\n");
		text.setWrappingWidth(150);
		text.setFill(Color.BLACK);
		text.setStyle("-fx-font-size: 14px");
		messageBox.getChildren().add(text);
	}
}
