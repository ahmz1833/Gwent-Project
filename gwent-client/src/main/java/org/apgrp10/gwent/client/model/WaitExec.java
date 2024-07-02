package org.apgrp10.gwent.client.model;

import javafx.animation.Transition;
import javafx.util.Duration;

public class WaitExec extends Transition {
	public WaitExec(long duration, Runnable fn) {
		setCycleDuration(Duration.millis(duration));
		setCycleCount(1);
		setOnFinished(e -> fn.run());
		play();
	}
	
	@Override
	protected void interpolate(double frac) {}
}
