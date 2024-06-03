package org.apgrp10.gwent.model.card;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.apgrp10.gwent.view.PreGameMenu;
import org.apgrp10.gwent.*;

public class CardImage extends Rectangle {
	public CardImage(String name) {
		super(PreGameMenu.screenWidth / (double) PreGameMenu.cardWidth,
				PreGameMenu.screenHeight / (double) PreGameMenu.cardHeight);
		setFill(new ImagePattern(R.getImage("lg/" + name + ".jpg")));
	}
}
