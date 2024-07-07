package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractStage extends Stage {
	private final ArrayList<MFXStageDialog> showingDialogs = new ArrayList<>();

	protected AbstractStage(String title, Image icon) {
		super();
		setOpacity(0.0);
		setTitle(title);
		setResizable(false);
		if (icon != null) getIcons().add(icon);
		this.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> Platform.runLater(() -> {
			toFront();
			centerOnScreen();
		}));
		this.setOnCloseRequest(AbstractStage.this::onCloseRequest);
	}



	public void start() {
		Platform.runLater(() -> {
			if (onCreate()) {
				setOpacity(1.0);
				focusedProperty().addListener(l -> {
					if (((ReadOnlyBooleanProperty) l).get()) onGetFocus();
					else onLostFocus();
				});
				show();
			}
		});
	}

	@SuppressWarnings("unchecked")
	protected <T> T lookup(String id) {
		return (T) getScene().lookup(id);
	}

	public void setOnPressListener(Button button, EventHandler<Event> handler) {
		button.setOnMouseClicked(handler);
		button.setOnKeyReleased(e -> {
			if (e.getCode().getName().equals("Enter")) handler.handle(e);
		});
	}



	public void connectionLost() {
		Platform.runLater(() -> {
			if (!showingDialogs.isEmpty() &&
			    showingDialogs.getLast().getTitle().equals("Connection Lost")) return;
			disable();
			Dialogs.showAlert(this, Dialogs.ERROR,
					"Connection Lost",
					"Connection to server lost, Trying to reconnect ...");
		});
	}

	public void connectionEstablished() {
		Platform.runLater(() -> {
			enable();
			if (showingDialogs.isEmpty() ||
			    !showingDialogs.getLast().getTitle().equals("Connection Lost")) return;
			showingDialogs.getLast().close();
			showingDialogs.remove(showingDialogs.getLast());
		});
	}


	public boolean showExitDialog() {
		if (this instanceof PreGameStage || this instanceof GameStage) {
			AtomicBoolean returnValue = new AtomicBoolean(true);
			Dialogs.showDialogAndWait(this, Dialogs.WARNING, "Confirmation",
					"Are you sure you want to exit? ",
					Map.entry("Exit", e -> Platform.exit()),
					Map.entry("Back to Main", e -> {
						close();
						MainStage.getInstance().start();
					}),
					Map.entry("*Cancel", e -> returnValue.set(false)));
			return returnValue.get();
		} else if ((this instanceof LoginStage || this instanceof MainStage) &&
		           Dialogs.showConfirmDialog(this, Dialogs.WARNING, "Exit Confirmation",
				           "Are you sure you want to exit?", "Yes", "No"))
			Platform.exit();
		else return false;
		return true;
	}

	protected abstract boolean onCreate();

	protected void onCloseRequest(WindowEvent event) {
		if (this instanceof MainStage || this instanceof LoginStage || this instanceof PreGameStage || this instanceof GameStage) {
			event.consume();
			showExitDialog();
		}
	}

	protected void onGetFocus() {}

	protected void onLostFocus() {}

	public void disable() {
		getScene().getRoot().setDisable(true);
	}

	public void enable() {
		getScene().getRoot().setDisable(false);
	}
}
