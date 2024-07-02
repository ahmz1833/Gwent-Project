package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
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
		if (onCreate()) {
			setOpacity(1.0);
			focusedProperty().addListener(l -> {
				if (((ReadOnlyBooleanProperty) l).get()) onGetFocus();
				else onLostFocus();
			});
			show();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T lookup(String id) {
		return (T) getScene().lookup(id);
	}
	
	public void setOnPressListener(String buttonID, EventHandler<Event> handler) {
		MFXButton button = lookup(buttonID);
		button.setOnMouseClicked(handler);
		button.setOnKeyReleased(e -> {
			if (e.getCode().getName().equals("Enter")) handler.handle(e);
		});
	}
	
	private MFXStageDialog makeDialog(MFXGenericDialogBuilder base) {
		base.addStylesheets(getScene().getStylesheets().toArray(new String[0]));
		
		MFXStageDialog dialog = MFXGenericDialogBuilder.build(base.get())
				.toStageDialogBuilder()
				.initOwner(this)
				.initModality(Modality.WINDOW_MODAL)
				.setOwnerNode((Pane) getScene().getRoot())
				.setScrimOwner(true)
				.setScrimPriority(ScrimPriority.WINDOW)
				.setDraggable(true)
				.setCenterInOwnerNode(true)
				.setOverlayClose(false)
				.get();
		
		Pane actionPane = (Pane) base.get().getBottom();
		actionPane.getChildren().forEach(c -> {
			if (c.equals(actionPane)) return;
			c.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> dialog.close());
		});
		
		dialog.setIconified(false);
		return dialog;
	}
	
	@SafeVarargs
	public final void showDialogAndWait(MFXGenericDialogBuilder base, String title, Node content, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
		ArrayList<Node> actionsBtns = new ArrayList<>();
		Button defaultBtn = null, cancelBtn = null;
		for (Map.Entry<String, EventHandler<? super MouseEvent>> action : actions) {
			String caption = action.getKey();
			MFXButton button = new MFXButton();
			if (caption.startsWith("#")) {
				caption = caption.substring(1);
				button.setDefaultButton(true);
				defaultBtn = button;
			}
			if (caption.startsWith("*")) {
				caption = caption.substring(1);
				button.setCancelButton(true);
				cancelBtn = button;
			}
			button.setText(caption);
			button.setOnMouseClicked(action.getValue());
			actionsBtns.add(button);
		}
		
		final Button finalDefaultBtn = defaultBtn;
		final Button finalCancelBtn = cancelBtn;
		for (Node button : actionsBtns) {
			button.setOnKeyReleased(event -> {
				if (event.getCode().getName().equals("Enter") && finalDefaultBtn != null)
					finalDefaultBtn.fireEvent(emptyMouseEvent());
				else if (event.getCode().getName().equals("Esc") && finalCancelBtn != null)
					finalCancelBtn.fireEvent(emptyMouseEvent());
			});
		}
		var dialog = makeDialog(base.setHeaderText(title).setContent(content)
				.addActions(actionsBtns.toArray(new Node[0])));
		if (cancelBtn != null) {
			Button finalCancelBtn1 = cancelBtn;
			dialog.setOnCloseRequest(e -> finalCancelBtn1.fireEvent(emptyMouseEvent()));
		}
		dialog.showAndWait();
	}
	
	@SafeVarargs
	public final void showDialogAndWait(MFXGenericDialogBuilder base, String title, String content, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
		Label contentLabel = new Label(content);
		contentLabel.setId("dialog_label");
		showDialogAndWait(base, title, contentLabel, actions);
	}
	
	public boolean showConfirmDialog(MFXGenericDialogBuilder base, String title, Node content, String posBtn, String negBtn) {
		AtomicBoolean result = new AtomicBoolean(false);
		showDialogAndWait(base, title, content,
				Map.entry("#" + posBtn, e -> result.set(true)),
				Map.entry("*" + negBtn, e1 -> {}));
		return result.get();
	}
	
	public boolean showConfirmDialog(MFXGenericDialogBuilder base, String title, String contentMsg, String posBtn, String negBtn) {
		AtomicBoolean result = new AtomicBoolean(false);
		showDialogAndWait(base, title, contentMsg,
				Map.entry("#" + posBtn, e -> result.set(true)),
				Map.entry("*" + negBtn, e1 -> {}));
		return result.get();
	}
	
	public void showAlert(MFXGenericDialogBuilder base, String title, String message) {
		showDialogAndWait(base, title, message, Map.entry("#*OK", e -> {}));
	}

//	public void showExitDialog() {
//		if (UserController.getCurrentUser() != null) {
//			User user = UserController.getCurrentUser();
//			showDialogAndWait(MFXDialogs.warn(), "Confirmation",
//					"Are you sure you want to exit? '" + user.getUsername() + "' will be logged out.",
//					Map.entry("Exit", e -> Platform.exit()),
//					Map.entry("Logout Only", e -> UserController.logout()),
//					Map.entry("*Cancel", e -> {}));
//		} else if (showConfirmDialog(MFXDialogs.warn(), "Exit Confirmation",
//				"Are you sure you want to exit?", "Yes", "No"))
//			Platform.exit();
//	}
	
	protected abstract boolean onCreate();
	
	protected abstract void onCloseRequest(WindowEvent event);
	
	protected abstract void onGetFocus();
	
	protected abstract void onLostFocus();
}
