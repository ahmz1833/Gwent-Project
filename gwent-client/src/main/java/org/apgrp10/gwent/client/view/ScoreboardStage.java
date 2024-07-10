package org.apgrp10.gwent.client.view;

import org.apgrp10.gwent.client.R;

public class ScoreboardStage extends AbstractStage{
	private static ScoreboardStage INSTANCE;

	private ScoreboardStage() {
		//TODO: setIcon
		super("Scoreboard", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of ScoreboardStage");
	}

	public static ScoreboardStage getInstance() {
		if (INSTANCE == null) INSTANCE = new ScoreboardStage();
		return INSTANCE;
	}

	@Override
	protected boolean onCreate() {
		return true;
	}
}
