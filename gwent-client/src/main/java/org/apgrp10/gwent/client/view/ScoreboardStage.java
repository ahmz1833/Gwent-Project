package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import org.apgrp10.gwent.client.R;


import java.util.Comparator;
import java.util.function.Function;

public class ScoreboardStage extends AbstractStage{
	private static ScoreboardStage INSTANCE;
	private MFXTableView<Long> table;

	private ScoreboardStage() {
		super("Scoreboard", R.icon.scores);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of ScoreboardStage");
		initOwner(MainStage.getInstance());
		initModality(Modality.WINDOW_MODAL);
	}

	public static ScoreboardStage getInstance() {
		if (INSTANCE == null) INSTANCE = new ScoreboardStage();
		return INSTANCE;

	}

	@Override
	protected boolean onCreate()
	{
		setScene(R.scene.scores);
		table = lookup("#scoreTable");
		table.getTableColumns().clear();
		table.getFilters().clear();
		table.update();



		return true;
	}

	private MFXTableColumn<Long> addColumn(String name, int prefWidth, Comparator<Long> comparator, Function<Long, String> cellFactory)
	{
		MFXTableColumn<Long> column = new MFXTableColumn<>(name, false)
		{{
			setRowCellFactory(person -> new MFXTableRowCell<>(cellFactory));
			setAlignment(Pos.CENTER);
			setPrefWidth(prefWidth);
			if (comparator != null) setComparator(comparator);
		}};
		table.getTableColumns().add(column);
		return column;
	}
}
