package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apgrp10.gwent.client.Gwent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractStage extends Stage {
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

	protected static MouseEvent emptyMouseEvent() {
		return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null);
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
			if (e.getCode().getName().equals("Enter") && handler != null) handler.handle(e);
		});
	}

	public void connectionLost() {
		Platform.runLater(() -> {
			if (!isShowing()) return;
			var showingDialogs = Dialogs.getShowingDialogs(this);
			if (!showingDialogs.isEmpty() &&
			    showingDialogs.getLast().getTitle().equals("Connection Lost")) return;
			disable();
			Dialogs.showDialogAndWait(this, Dialogs.ERROR(),
					"Connection Lost",
					"Connection to server lost, Trying to reconnect ...",
					Map.entry("Exit", e -> Gwent.exit()));
		});
	}

	public void connectionEstablished() {
		Platform.runLater(() -> {
			if (!isShowing()) return;
			enable();
			var showingDialogs = Dialogs.getShowingDialogs(this);
			if (showingDialogs.isEmpty() ||
			    !showingDialogs.getLast().getTitle().equals("Connection Lost")) return;
			showingDialogs.getLast().close();
			showingDialogs.remove(showingDialogs.getLast());

			updateInformation();
		});
	}

	protected void updateInformation() {
		// May be overridden by some stages
	}

	public boolean showExitDialog() {
		if (this instanceof PreGameStage || this instanceof GameStage) {
			AtomicBoolean returnValue = new AtomicBoolean(true);
			showDialogAndWait(Dialogs.WARN(), "Confirmation",
					"Are you sure you want to exit? ",
					Map.entry("Exit", e -> Gwent.exit()),
					Map.entry("Back to Main", e -> {
						close();
						MainStage.getInstance().start();
					}),
					Map.entry("*Cancel", e -> returnValue.set(false)));
			return returnValue.get();
		} else if ((this instanceof LoginStage || this instanceof MainStage) &&
		           showConfirmDialog(Dialogs.WARN(), "Exit Confirmation",
				           "Are you sure you want to exit?", "Yes", "No"))
			Gwent.exit();
		else return false;
		return true;
	}

	public boolean showConfirmDialog(MFXGenericDialogBuilder base, String title, String contentMsg, String posBtn, String negBtn) {
		return Dialogs.showConfirmDialog(this, base, title, contentMsg, posBtn, negBtn);
	}

	@SafeVarargs
	public final void showDialogAndWait(MFXGenericDialogBuilder base, String title, String content, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
		Dialogs.showDialogAndWait(this, base, title, content, actions);
	}

	public void showAlert(MFXGenericDialogBuilder base, String title, String message) {
		Dialogs.showAlert(this, base, title, message);
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
