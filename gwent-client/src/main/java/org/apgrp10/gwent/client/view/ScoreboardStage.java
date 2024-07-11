package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.PreGameController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.UserExperience;
import org.apgrp10.gwent.utils.WaitExec;

import java.util.Comparator;
import java.util.function.Function;

public class ScoreboardStage extends AbstractStage {
	private static ScoreboardStage INSTANCE;
	private final WaitExec updater = new WaitExec(false);
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
	protected boolean onCreate() {
		setScene(R.scene.scores);
		table = lookup("#scoreTable");
		table.getTableColumns().clear();
		table.getFilters().clear();
		updateInformation();
		updater.run(5000, new Runnable() {
			@Override
			public void run() {
				if (!ScoreboardStage.this.isShowing()) return;
				updater.run(5000, this);
				updateInformation();
			}
		});

		return true;
	}

	private void addColumn(String name, int prefWidth, Comparator<Long> comparator, Function<Long, String> cellFactory) {
		MFXTableColumn<Long> column = new MFXTableColumn<>(name, false) {{
			setRowCellFactory(person -> {
				var cell = new MFXTableRowCell<>(cellFactory);
				cell.setOnMouseClicked(event -> {
					if (event.getClickCount() == 2) {
						boolean replay = showConfirmDialog(Dialogs.INFO(), "Last game play",
								"Do you want to replay the last game played by this user?", "Yes", "No");
						if (!replay) return;
						PreGameController.getLastGame(person, game -> {
							if (game == null)
								showAlert(Dialogs.ERROR(), "Error", "No game found for this user");
							else
								PreGameController.startGame(game.createReplayRequest(UserController::getCachedInfo));
						});
					}
				});
				return cell;
			});
			setAlignment(Pos.CENTER);
			setPrefWidth(prefWidth);
			if (comparator != null) setComparator(comparator);
		}};
		table.getTableColumns().add(column);
	}

	@Override
	protected void updateInformation() {
		UserController.getTopUsers(10, false, userExperiences -> {
			UserController.cacheUserInfo(() -> {
				table.getTableColumns().clear();
				addColumn("#", 50, null, id -> {
					int rank = 1;
					for (UserExperience userExperience : userExperiences) {
						if (userExperience.userId() == id) return String.valueOf(rank);
						rank++;
					}
					return "";
				});
				addColumn("Username", 150, null, id -> UserController.getCachedInfo(id).username());
				addColumn("Nickname", 150, null, id -> UserController.getCachedInfo(id).nickname());
				addColumn("Best\nScore", 100, null, id ->
						String.valueOf(userExperiences.stream().filter(userExperience -> userExperience.userId() == id)
								.findFirst().orElseThrow().maxScore()));
				addColumn("W", 80, null, id ->
						String.valueOf(userExperiences.stream().filter(userExperience -> userExperience.userId() == id)
								.findFirst().orElseThrow().wins()));
				addColumn("L", 80, null, id ->
						String.valueOf(userExperiences.stream().filter(userExperience -> userExperience.userId() == id)
								.findFirst().orElseThrow().losses()));
				addColumn("D", 80, null, id ->
						String.valueOf(userExperiences.stream().filter(userExperience -> userExperience.userId() == id)
								.findFirst().orElseThrow().draws()));
				addColumn("T", 80, null, id -> {
					UserExperience userExperience = userExperiences.stream().filter(ue -> ue.userId() == id).findFirst().orElseThrow();
					return String.valueOf(userExperience.wins() + userExperience.losses() + userExperience.draws());
				});
				addColumn("Online", 100, null, id -> UserController.getCachedOnlineState(id) ? "✔" : "");
				table.setItems(FXCollections.observableList(userExperiences.stream().map(UserExperience::userId).toList()));
				table.update();
				table.setMaxWidth(750);
				table.autosizeColumns();
			}, true, userExperiences.stream().map(UserExperience::userId).toArray(Long[]::new));
		});
	}
}
