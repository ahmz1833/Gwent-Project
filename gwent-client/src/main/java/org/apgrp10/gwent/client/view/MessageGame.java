package org.apgrp10.gwent.client.view;

import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.model.WaitExec;

public class MessageGame extends Pane {
    private final Pane gamePane;
    private final Pane self = this;
    MessageGame(Pane gamePane, Image image, String Text){
        this.gamePane = gamePane;
        setPrefWidth(PreGameMenu.screenWidth);
        setPrefHeight(PreGameMenu.screenHeight);
        this.setLayoutX(0);
        this.setLayoutY(0);
        setOnMouseClicked(Event::consume);
        addImageView(R.getImage("icons/black.png"), PreGameMenu.screenWidth, 100, 0, 300);
        addImageView(image, 200, 200, 300, 230);
        new WaitExec(400, new Runnable() {
            @Override
            public void run() {
                gamePane.getChildren().add(self);
                System.out.println("added");
            }
        });
        new WaitExec(20000, new Runnable() {
            @Override
            public void run() {
                gamePane.getChildren().remove(self);
                System.out.println("deleted");
            }
        });
    }
    private void addImageView(Image image, int width, int height, int x, int y){
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setX(x);
        imageView.setY(y);
        getChildren().add(imageView);
    }

}
