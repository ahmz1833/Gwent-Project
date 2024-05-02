package org.apgrp10.gwent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainWindow extends Application
{
	public static void main(String[] args)
	{
		System.out.println("Hello world!");
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception
	{
		stage.setScene(new Scene(FXMLLoader.load(MainWindow.class.getResource("/FXML/main.fxml"))));
		stage.show();
	}
}
