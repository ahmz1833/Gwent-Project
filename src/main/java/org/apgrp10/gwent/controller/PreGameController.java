package org.apgrp10.gwent.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import org.apgrp10.gwent.model.card.CardView;
import org.apgrp10.gwent.view.PreGameMenu;

import java.util.ArrayList;

public class PreGameController {
	public MFXButton updateButton;

	public void update() {
		ArrayList<ArrayList<CardView>> list = new ArrayList<>(4);
		for (int k = 0; k < 4; k++) {
			list.add(new ArrayList<>());
			double number = Math.random() * 30;
			for (int i = 0; i < number; i++) {
				list.get(k).add(new CardView("neutral_ciri"));
			}
		}
		PreGameMenu.currentMenu.updateLists(list);
	}
}
