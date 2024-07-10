package org.apgrp10.gwent.client.model;

import io.github.palexdev.materialfx.controls.MFXListView;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

public class GameListView extends MFXListView<Long> {
	public GameListView()
	{
		super();
	}

	@Override
	public void setItems(ObservableList<Long> items) {
		super.setItems(items);
	}

	public static class GameListItem extends AnchorPane
	{
		public GameListItem(long userId)
		{

		}
	}
}
