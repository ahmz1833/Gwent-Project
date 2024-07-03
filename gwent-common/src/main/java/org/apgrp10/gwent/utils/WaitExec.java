package org.apgrp10.gwent.utils;

import javafx.animation.Transition;
import javafx.util.Duration;

public class WaitExec {
	private boolean dummy;

	public void run(long duration, Runnable fn) {
		if (dummy) {
			fn.run();
			return;
		}

		new Transition() {
			{
				setCycleDuration(Duration.millis(duration));
				setCycleCount(1);
				setOnFinished(e -> fn.run());
				play();
			}
			protected void interpolate(double frac) {}
		};
	}

	public WaitExec(boolean dummy) {
		this.dummy = dummy;
	}
}
