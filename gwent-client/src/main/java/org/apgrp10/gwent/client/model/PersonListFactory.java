package org.apgrp10.gwent.client.model;

import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.model.FriendshipRequest;

import java.util.HashMap;
import java.util.function.Consumer;

public class PersonListFactory extends MFXListCell<Long> {
	private final AvatarView avatarView;
	private HashMap<Long, FriendshipRequest.RequestState> stateMap;
	private ImageView statusIcon;
	private final Label label;

	public PersonListFactory(MFXListView<Long> listView, Long data) {
		super(listView, data);

		avatarView = new AvatarView();
		avatarView.setPrefWidth(45);
		label = (Label) lookup(".data-label");
		label.textProperty().unbind();
		label.setText("Loading...");
		render(data);
	}

	public PersonListFactory(MFXListView<Long> listView, Long data, HashMap<Long, FriendshipRequest.RequestState> map) {
		super(listView, data);

		avatarView = new AvatarView();
		avatarView.setPrefWidth(45);
		label = (Label) lookup(".data-label");
		label.textProperty().unbind();
		label.setText("Loading...");
		statusIcon = new ImageView(R.getImage("ic_wait.png"));
		statusIcon.setFitWidth(35);
		statusIcon.setFitHeight(35);
		stateMap = map;
		render(data);
	}

	@Override
	protected void render(Long data) {
		super.render(data);
		if (avatarView == null) return;
		setPrefHeight(55);
		setSpacing(10);
		UserController.getUserInfo(data, false, (user) -> {
			label.setText("%s (%s)".formatted(user.nickname(), user.username()));
			avatarView.setAvatar(user.avatar());
		});
		getChildren().add(0, avatarView);
		if(statusIcon != null)
		{
			FriendshipRequest.RequestState state = stateMap.getOrDefault(data, FriendshipRequest.RequestState.PENDING);
			statusIcon.setImage(switch (state) {
				case ACCEPTED -> R.getImage("ic_ok.png");
				case PENDING -> R.getImage("ic_wait.png");
				case REJECTED -> R.getImage("ic_reject.png");
			});
			getChildren().add(statusIcon);
		}
	}

	public void setOnDoubleClick(Consumer<Long> runnable) {
		setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				runnable.accept(getData());
			}
		});
	}
}
