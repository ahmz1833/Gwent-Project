package org.apgrp10.gwent.client.view;

import org.apgrp10.gwent.client.R;

public class FriendshipStage extends AbstractStage {

	private static FriendshipStage INSTANCE;

	private FriendshipStage() {
		super("Friends", R.icon.profile);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of FriendshipStage");
	}

	public static FriendshipStage getInstance() {
		if (INSTANCE == null) INSTANCE = new FriendshipStage();
		return INSTANCE;
	}

	protected boolean onCreate() {
		return true;
	}
}
