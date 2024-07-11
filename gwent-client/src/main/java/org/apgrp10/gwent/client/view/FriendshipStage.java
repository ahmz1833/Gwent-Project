package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.PersonListFactory;
import org.apgrp10.gwent.model.FriendshipRequest;
import org.apgrp10.gwent.utils.WaitExec;

import java.util.HashMap;
import java.util.Map;

public class FriendshipStage extends AbstractStage {
	private static FriendshipStage INSTANCE;
	private final WaitExec updater = new WaitExec(false);
	private final HashMap<Long, FriendshipRequest.RequestState> map = new HashMap<>();
	private MFXListView<Long> friends, incoming, outgoing;

	private FriendshipStage() {
		super("Friends", R.icon.profile);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of FriendshipStage");
		initOwner(MainStage.getInstance());
		initModality(Modality.WINDOW_MODAL);
	}

	public static FriendshipStage getInstance() {
		if (INSTANCE == null) INSTANCE = new FriendshipStage();
		return INSTANCE;
	}

	protected boolean onCreate() {
		setScene(R.scene.friends);

		friends = lookup("#friends");
		incoming = lookup("#incoming");
		outgoing = lookup("#outgoing");
		friends.getStyleClass().add("list");
		incoming.getStyleClass().add("list");
		outgoing.getStyleClass().add("list");
		friends.setCellFactory(param -> {
			var cell = new PersonListFactory(friends, param);
			cell.setOnDoubleClick(friendId -> {
				boolean remove = showConfirmDialog(Dialogs.WARN(), "Remove Friend",
						"Are you sure you want to remove this friend?", "Yes", "No");
				if (remove) {
					UserController.removeFriendship(friendId, res -> {
						if (res.isOk())
							showAlert(Dialogs.INFO(), "Friend Removed", "Friend removed successfully.");
						else
							showAlert(Dialogs.ERROR(), "Error", "Failed to remove friend.");
					});
				}
			});
			return cell;
		});
		incoming.setCellFactory(param -> {
			var cell = new PersonListFactory(incoming, param);
			cell.setOnDoubleClick(request -> {
				showDialogAndWait(Dialogs.WARN(), "Accept Friend Request",
						"Do you want to accept this friend request?",
						Map.entry("Accept", e->{
							UserController.acceptFriendshipRequest(request, res -> {
								if (res.isOk())
									showAlert(Dialogs.INFO(), "Friend Request Accepted", "Friend request accepted successfully.");
								else
									showAlert(Dialogs.ERROR(), "Error", "Failed to accept friend request.");
							});
						}),
						Map.entry("Reject", e->{
							UserController.rejectFriendshipRequest(request, res -> {
								if (res.isOk())
									showAlert(Dialogs.INFO(), "Friend Request Rejected", "Friend request rejected successfully.");
								else
									showAlert(Dialogs.ERROR(), "Error", "Failed to reject friend request.");
							});
						}),
						Map.entry("Cancel", e->{}));
			});
			return cell;
		});
		outgoing.setCellFactory(param -> new PersonListFactory(outgoing, param, map));

		setOnPressListener("#addFriend", e -> {
			showSearchForUserDialog();
		});

		updateInformation();
		updater.run(5000, new Runnable() {
			@Override
			public void run() {
				if (!FriendshipStage.this.isShowing()) return;
				updater.run(5000, this);
				updateInformation();
			}
		});
		return true;
	}

	private void showSearchForUserDialog() {
		var content = new VBox();
		content.setSpacing(5);
		content.setPadding(new Insets(10, 10, 10, 10));

		var query = new MFXTextField();
		query.setFloatMode(FloatMode.BORDER);
		query.setFloatingText("Query");
		query.setPrefWidth(400);
		content.getChildren().add(query);

		var list = new MFXListView<Long>();
		list.getStyleClass().add("list");
		list.setPrefWidth(400);
		list.setCellFactory(param -> {
			var cell = new PersonListFactory(list, param);
			cell.setOnDoubleClick(userId -> {
				boolean request = showConfirmDialog(Dialogs.INFO(), "Send Friend Request",
						"Do you want to send a friend request to " + UserController.getCachedInfo(userId).nickname() + "?",
						"Yes", "No");
				if(!request) return;
				UserController.addFriendshipRequest(userId, res -> {
					if (res.isOk())
						showAlert(Dialogs.INFO(), "Friend Request Sent", "Friend request sent successfully.");
					else
						showAlert(Dialogs.ERROR(), "Error", "Failed to send friend request.");
				});
			});
			return cell;
		});
		content.getChildren().add(list);

		query.setOnKeyReleased(e1 -> {
			if (query.getText().isBlank()) return;
			UserController.searchUsername(query.getText(), 10, users -> {
				list.setItems(FXCollections.observableArrayList(users));
			});
		});

		Dialogs.showDialogAndWait(this, Dialogs.INFO(), "Search for user", content, Orientation.HORIZONTAL,
				Map.entry("*Close", e1 -> {}));
	}

	@Override
	protected void updateInformation() {
		UserController.getFriendList((friends) -> this.friends.setItems(FXCollections.observableArrayList(friends)));
		UserController.getIncomingFriendshipRequests((incoming) ->
				this.incoming.setItems(FXCollections.observableArrayList(incoming.stream().map(FriendshipRequest::from).toList())));
		UserController.getOutgoingFriendshipRequests((outgoing) -> {
			outgoing.forEach(request -> map.put(request.to(), request.state()));
			this.outgoing.setItems(FXCollections.observableArrayList(outgoing.stream().map(FriendshipRequest::to).toList()));
		});
	}
}
