package org.apgrp10.gwent.client.view;

import io.github.palexdev.materialfx.controls.MFXListView;
import javafx.collections.FXCollections;
import javafx.stage.Modality;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.client.model.PersonListFactory;
import org.apgrp10.gwent.model.FriendshipRequest;
import org.apgrp10.gwent.utils.WaitExec;

import java.util.HashMap;
import java.util.List;

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
		friends.setCellFactory(param -> new PersonListFactory(friends, param));
		incoming.setCellFactory(param -> new PersonListFactory(incoming, param));
		outgoing.setCellFactory(param -> new PersonListFactory(outgoing, param, map));

		setOnPressListener("#addFriend", e -> {

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
