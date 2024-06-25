package org.apgrp10.gwent.view;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.R;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardView;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;


public class GameMenu extends Application {
	private GameController controller;
	private Stage stage;
	private Pane rootPane;

	public static GameMenu currentMenu;
	private static final int WIDTH = 1280, HEIGHT = 720;

	private static class Position {
		public static record RectPos(double posX, double posY, double posX2, double posY2) {
			public double x() { return posX * WIDTH; }
			public double y() { return posY * HEIGHT; }
			public double w() { return (posX2 - posX) * WIDTH; }
			public double h() { return (posY2 - posY) * HEIGHT; }
			public void setBounds(Region region) {
				region.setLayoutX(x());
				region.setLayoutY(y());
				region.setMinSize(w(), h());
				region.setMaxSize(w(), h());
			}
		}

		public static final RectPos hand = new RectPos(0.3010, 0.7796, 0.7875, 0.8953);
		public static final RectPos card = new RectPos(0, 0, 0.0453, 0.1157);
	}


	public GameMenu(GameController gameController, Stage stage) {
		controller = gameController;
		currentMenu = this;
		start(stage);
	}

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		rootPane = new Pane();
		Scene scene = new Scene(rootPane);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		redraw();
	}

	public void redraw() {
		rootPane.getChildren().clear();

		ImageView background = new ImageView(R.image.board);
		background.setFitWidth(WIDTH);
		background.setFitHeight(HEIGHT);
		rootPane.getChildren().add(background);

		Button hello = new Button("Hello");
		hello.setOnAction(e -> notifyButton("hello"));
		rootPane.getChildren().add(hello);

		int player = controller.getActivePlayer();

		HBox hand = new HBox();
		Position.hand.setBounds(hand);
		for (Card card : controller.getPlayer(player).handCards)
			hand.getChildren().add(CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h()));
		hand.setAlignment(Pos.CENTER);
		rootPane.getChildren().add(hand);
	}

	public static interface Callback { public void call(Object object); }
	private List<Callback> cardListeners = new ArrayList<>();
	private List<Callback> buttonListeners = new ArrayList<>();

	public Object addCardListener(Callback cb) { cardListeners.add(cb); return cb; }
	public Object addButtonListener(Callback cb) { buttonListeners.add(cb); return cb; }

	public void removeListener(Object obj) {
		cardListeners.remove(obj);
		buttonListeners.remove(obj);
	}

	private void notifyButton(String msg) {
		for (Callback cb : buttonListeners)
			cb.call(msg);
	}
}
