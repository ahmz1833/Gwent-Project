package org.apgrp10.gwent.view;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Application;
import javafx.beans.binding.IntegerBinding;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apgrp10.gwent.*;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardImage;

import java.util.ArrayList;

public class PreGameMenu extends Application {
	private Stage stage;
	private Pane pane;
	private Scene scene;
	private GridPane[] lists = new GridPane[4];
	public static PreGameMenu currentMenu;
	public static final int screenWidth = 1500, cardWidth = 18;
	public static final int screenHeight = 800, cardHeight = 5;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		currentMenu = this;
		this.stage = stage;
		load();
		setTitle();
		addGradePane();

		stage.show();
	}

	private void setTitle() {
		stage.setTitle("pre game menu");
		stage.getIcons().add(R.getImage("icons/profile.png"));
	}

	private void load() {
		this.scene = R.getFXML("preGame.fxml");
		stage.setScene(scene);
		pane = (Pane) scene.getRoot();
		stage.setResizable(false);
		stage.setMinWidth(screenWidth);
		stage.setMaxWidth(screenWidth);
		stage.setMinHeight(screenHeight);
		stage.setMaxHeight(screenHeight);
		setCursor();
	}

	private void addGradePane() {
		for(int i = 0; i < 4; i++) {
			GridPane gridPane = new GridPane();
			gridPane.setMinWidth(3 * screenWidth / (double) cardWidth + 0);
			gridPane.setMaxWidth(gridPane.getMinWidth());
			gridPane.setMaxHeight(3 * (double) screenHeight / cardHeight + 30);
			gridPane.setMaxHeight(gridPane.getMinHeight());
			gridPane.setVgap(5);
			gridPane.setHgap(5);
			for (int j = 0; j < 4; j++) {
				gridPane.getColumnConstraints().add(
						new ColumnConstraints(screenWidth / (double) cardWidth));
			}
			MFXScrollPane scroll = new MFXScrollPane(gridPane);
			scroll.setLayoutX(50 + 300 * i);
			scroll.setLayoutY(50);
			pane.getChildren().add(scroll);
			lists[i] = gridPane;
		}
	}

	public void updateLists(ArrayList<CardImage> arrayList) {
		int i = 0, j = 0;
		lists[0].getChildren().clear();
		for (CardImage cardImage : arrayList) {
			lists[0].add(new CardImage("monsters_nekker"), i, j);
			if (i == 2) {
				i = 0;
				j++;
			} else
				i++;
		}
	}

	private void setCursor() {
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}
}
