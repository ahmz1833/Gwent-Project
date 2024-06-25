package org.apgrp10.gwent.view;

import javafx.scene.layout.Pane;

public class ChatMenu extends Pane {
	public final static int width = 250, height = 720;

	public ChatMenu(double screenWidth) {
		this.setLayoutX(screenWidth - 250);
		this.setLayoutY(0);
		System.out.println(this.getLayoutX());
		this.setPrefWidth(width);
		this.setPrefHeight(height);
	}
}
