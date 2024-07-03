package org.apgrp10.gwent.client.view;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apgrp10.gwent.client.model.CardView;
import org.apgrp10.gwent.model.card.Card;
import java.util.ArrayList;
import java.util.List;

public class FivePlaceGame extends Pane {
    private final GameMenu gameMenu;
    private final StackPane[] images = new StackPane[5];
    private final List<Card> pickList;
    private int currentIndex;
    private int pickIdx;

    protected FivePlaceGame(GameMenu gameMenu, boolean nullPossible, List<Card> pickList, Pane gamePane, int picIdx) {
        this.pickIdx = picIdx;
        if(pickIdx >= pickList.size())
            pickIdx = pickList.size() - 1;
        if(picIdx < 0)
            pickIdx = 0;
        currentIndex = picIdx;
        this.gameMenu = gameMenu;
        this.pickList = new ArrayList<>(pickList);

        if(pickList.size() == 0)
            return;
        if (nullPossible) {
            setOnMouseClicked(k -> {
                gameMenu.pickList = null;
                gameMenu.pickFn.call(null);
                gameMenu.redraw();
                gamePane.getChildren().remove(this);
            });
        } else {
            setOnMouseClicked(Event::consume);
        }
        setPrefWidth(PreGameMenu.screenWidth);
        setPrefHeight(PreGameMenu.screenHeight);
        addPlaces();
        gamePane.getChildren().add(this);
    }

    private StackPane getStackPane(double width, double height, Pos pos) {
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(pos);
        stackPane.setMinWidth(width);
        stackPane.setMaxWidth(width);
        stackPane.setMinHeight(height);
        stackPane.setMaxHeight(height);
        return stackPane;
    }

    private void addPlaces() {
        for (int i = 0; i < 5; i++) {
            StackPane stackPane = new StackPane();
            final double height = PreGameMenu.screenHeight / 2.5 - 60 * (Math.abs(i - 2)) + 4;
            final double width = PreGameMenu.screenWidth / 6.0 - 20 * (Math.abs(i - 2)) + 4;
            switch (i) {
                case 0 -> stackPane = getStackPane(width, height, Pos.TOP_RIGHT);
                case 1, 3 -> stackPane = getStackPane(width, height, Pos.CENTER);
                case 2 -> stackPane = getStackPane(width, height, Pos.BOTTOM_CENTER);
                case 4 -> stackPane = getStackPane(width, height, Pos.TOP_LEFT);
            }
            images[i] = stackPane;
            addImage(pickIdx + i - 2, i);
            stackPane.setLayoutX(50 + 240 * i);
            stackPane.setLayoutY(100 - 20 * Math.abs(2 - i));
            this.getChildren().add(stackPane);
        }
    }

    private void addImage(int cardIndex, int placeIndex) {
        try {
            images[placeIndex].getChildren().clear();
            Card card = pickList.get(cardIndex);
            CardView view = CardView.newInfo(card.pathAddress, GameMenu.Position.info.w(), GameMenu.Position.info.h());
            view.setPrefWidth(PreGameMenu.screenWidth / 6.0 - 20 * (Math.abs(placeIndex - 2)));
            view.setPrefHeight(PreGameMenu.screenHeight / 2.5 - 60 * (Math.abs(placeIndex - 2)));
            view.setStyle("-fx-background-radius: 50px");
            images[placeIndex].setStyle("-fx-background-radius: 50px");
            DropShadow dropShadow = new DropShadow();
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(0);
            if (placeIndex == 2) {
                dropShadow.setRadius(80);
                dropShadow.setColor(Color.rgb(8, 247, 63));
            } else {
                dropShadow.setRadius(60);
                dropShadow.setColor(Color.rgb(247, 211, 10));
            }
            view.setEffect(dropShadow);
            images[placeIndex].getChildren().add(view);
            view.setOnMouseClicked(k -> {
                if (placeIndex == 2) {
                    if (k.getClickCount() > 1) clickedOn(placeIndex, card, cardIndex);
                } else clickedOn(placeIndex, card, cardIndex);
                k.consume();
            });

        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    private void clickedOn(int index, Card card, int indexInList) {
        currentIndex += index - 2;
        setCurrentImage(currentIndex);
        if (index == 2) {
            gameMenu.pickList = null;
            gameMenu.pickFn.call(card);
            gameMenu.changePicIdx(indexInList);
            gameMenu.redraw();
        }
    }

    private void setCurrentImage(int index) {
        for (int i = -2; i <= 2; i++) {
            addImage(index + i, 2 + i);
        }
    }
}
