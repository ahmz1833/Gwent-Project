package org.apgrp10.gwent.client.model;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

public class PersonListFactory extends MFXListCell<Long> {
	public PersonListFactory(MFXListView<Long> listView, Long data) {
		super(listView, data);
	}

}
