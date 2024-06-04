package org.apgrp10.gwent.view;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.application.Application;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apgrp10.gwent.*;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.CardView;

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
		for (int i = 0; i < 4; i++) {
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
			switch (i) {
				case 0:
					scroll.setLayoutX(10);
					break;
				case 1:
					scroll.setLayoutX(460);
					break;
				case 2:
					scroll.setLayoutX(750);
					break;
				case 3:
					scroll.setLayoutX(1200);
			}
			scroll.setLayoutY(130);
			pane.getChildren().add(scroll);
			lists[i] = gridPane;
		}
	}

	public void updateLists(ArrayList<ArrayList<CardView>> arrayList) {
		for (int k = 0; k < 4; k++) {
			int i = 0, j = 0;
			lists[k].getChildren().clear();
			for (CardView cardImage : arrayList.get(k)) {
				lists[k].add(new CardView("monsters_nekker"), i, j);
				if (i == 2) {
					i = 0;
					j++;
				} else
					i++;
			}
		}
	}

	private void setCursor() {
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}
}
