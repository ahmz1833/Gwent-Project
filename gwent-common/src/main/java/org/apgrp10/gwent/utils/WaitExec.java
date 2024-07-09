package org.apgrp10.gwent.utils;

import javafx.application.Platform;

public class WaitExec {
	private boolean dummy;

	public void run(long duration, Runnable fn) {
		if (dummy) {
			fn.run();
			return;
		}

		new Waiter(duration, fn);
	}

	private static class Waiter {
		private Runnable fn;
		private long start, wait;

		public Waiter(long wait, Runnable fn) {
			this.wait = wait;
			this.fn = fn;
			this.start = System.currentTimeMillis();
			tryRun();
		}

		private void tryRun() {
			if (System.currentTimeMillis() - start < wait) {
				Platform.runLater(this::tryRun);
				return;
			}
			fn.run();
		}
	}

	public WaitExec(boolean dummy) {
		this.dummy = dummy;
	}

	public void setDummy(boolean b) {
		dummy = b;
	}
}
