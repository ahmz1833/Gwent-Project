package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXDialogs;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.R;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dialogs {

	private static final HashMap<Stage, ArrayList<MFXStageDialog>> showingDialogs = new HashMap<>();

	public static MFXGenericDialogBuilder ERROR() {return MFXDialogs.error();}

	public static MFXGenericDialogBuilder WARN() {return MFXDialogs.warn();}

	public static MFXGenericDialogBuilder INFO() {return MFXDialogs.info();}

	private static MFXStageDialog makeDialog(Stage owner, MFXGenericDialogBuilder base) {
		base.addStylesheets(R.get("css/styles.css").toExternalForm());

		MFXStageDialog dialog = MFXGenericDialogBuilder.build(base.get())
				.toStageDialogBuilder()
				.initOwner(owner)
				.initModality(Modality.WINDOW_MODAL)
				.setOwnerNode((Pane) owner.getScene().getRoot())
				.setScrimOwner(true)
				.setScrimPriority(ScrimPriority.WINDOW)
				.setDraggable(true)
				.setCenterInOwnerNode(true)
				.setOverlayClose(false)
				.get();

		dialog.setIconified(false);
		return dialog;
	}

	protected static MouseEvent emptyMouseEvent() {
		return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null);
	}

	@SafeVarargs
	public static void showDialogAndWait(Stage owner, MFXGenericDialogBuilder base, String title, Node content,
	                                     Orientation actionsOrientation, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
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
		base.setHeaderText(title).setContent(content);
		Pane actionsPane;
		if (actionsOrientation == Orientation.HORIZONTAL) {
			actionsPane = new HBox(10.0);
			((HBox) actionsPane).setAlignment(Pos.CENTER_RIGHT);

		} else {
			actionsPane = new VBox(10.0);
			((VBox) actionsPane).setAlignment(Pos.TOP_RIGHT);
		}
		actionsPane.getChildren().addAll(actionsBtns);
		base.addActions(actionsPane);

		var dialog = makeDialog(owner, base);

		actionsBtns.forEach(c -> c.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> dialog.close()));

		if (cancelBtn != null) {
			Button finalCancelBtn1 = cancelBtn;
			dialog.setOnCloseRequest(e -> finalCancelBtn1.fireEvent(emptyMouseEvent()));
		}
		dialog.setTitle(title);
		if (showingDialogs.containsKey(owner)) {
			showingDialogs.get(owner).add(dialog);
		} else {
			showingDialogs.put(owner, new ArrayList<>(List.of(dialog)));
		}
		dialog.showAndWait();
		showingDialogs.get(owner).remove(dialog);
	}

	@SafeVarargs
	public static void showDialogAndWait(Stage owner, MFXGenericDialogBuilder base, String title, String content, Orientation actionsOrientation, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
		Label contentLabel = new Label(content);
		contentLabel.setId("dialog_label");
		showDialogAndWait(owner, base, title, contentLabel, actionsOrientation, actions);
	}

	@SafeVarargs
	public static void showDialogAndWait(Stage owner, MFXGenericDialogBuilder base, String title, String content, Map.Entry<String, EventHandler<? super MouseEvent>>... actions) {
		showDialogAndWait(owner, base, title, content, Orientation.HORIZONTAL, actions);
	}

	public static boolean showConfirmDialog(Stage owner, MFXGenericDialogBuilder base, String title, Node content, String posBtn, String negBtn) {
		AtomicBoolean result = new AtomicBoolean(false);
		showDialogAndWait(owner, base, title, content, Orientation.HORIZONTAL,
				Map.entry("#" + posBtn, e -> result.set(true)),
				Map.entry("*" + negBtn, e1 -> {}));
		return result.get();
	}

	public static boolean showConfirmDialog(Stage owner, MFXGenericDialogBuilder base, String title, String contentMsg, String posBtn, String negBtn) {
		AtomicBoolean result = new AtomicBoolean(false);
		showDialogAndWait(owner, base, title, contentMsg,
				Map.entry("#" + posBtn, e -> result.set(true)),
				Map.entry("*" + negBtn, e1 -> {}));
		return result.get();
	}

	public static void showAlert(Stage owner, MFXGenericDialogBuilder base, String title, String message) {
		showDialogAndWait(owner, base, title, message, Map.entry("#*OK", e -> {}));
	}

	public static ArrayList<MFXStageDialog> getShowingDialogs(Stage owner) {
		return showingDialogs.get(owner);
	}
}
