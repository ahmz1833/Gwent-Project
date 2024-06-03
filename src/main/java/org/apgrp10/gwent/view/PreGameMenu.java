package org.apgrp10.gwent.view;

import javafx.application.Application;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apgrp10.gwent.*;

public class PreGameMenu extends Application {
	private Stage stage = null;
	public static void main(String[] args){
		launch(args);
	}
	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		setTitle();
		show();
	}
	private Pane getPane(){
		Pane pane = R.getFXML("preGame.fxml");
		pane.setId("preGameMenu");
		setCursor(pane);
		return pane;
	}
	private void setTitle(){
		stage.setTitle("pre game menu");
		stage.getIcons().add(R.getImage("icons/profile.png"));
	}
	private void show(){
		stage.setScene(new Scene(getPane()));
		stage.setResizable(false);
		stage.show();
	}
	private void setCursor(Pane pane){
		Image cursor = R.getImage("icons/cursor.png");
		pane.setCursor(new ImageCursor(cursor));
	}
}
