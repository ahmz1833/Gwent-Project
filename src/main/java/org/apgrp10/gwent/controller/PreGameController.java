package org.apgrp10.gwent.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.scene.layout.Pane;
import org.apgrp10.gwent.model.card.CardImage;
import org.apgrp10.gwent.view.PreGameMenu;

import java.util.ArrayList;

public class PreGameController {
	public MFXButton updateButton;

	public void update() {
		ArrayList<CardImage> arrayList = new ArrayList<>();
		double number = Math.random() * 100;
		for(int i = 0; i < number; i++){
			arrayList.add(new CardImage("neutral_ciri"));
		}
		PreGameMenu.currentMenu.updateLists(arrayList);
	}
}
