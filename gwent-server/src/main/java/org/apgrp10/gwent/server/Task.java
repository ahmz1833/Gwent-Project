package org.apgrp10.gwent.server;

import java.util.ArrayList;
import java.util.List;

public abstract class Task {
	private List<Runnable> commandQueue = new ArrayList<>();

	protected final void addCommand(Runnable cmd) {
		synchronized (commandQueue) {
			commandQueue.add(cmd);
		}
	}

	private void flushCommands()  {
		List<Runnable> copy;
		synchronized (commandQueue) {
			copy = new ArrayList<>(commandQueue);
			commandQueue.clear();
		}
		for (Runnable fn : copy)
			fn.run();
	}

	public final void run() {
		flushCommands();
		iterate();
	}

	protected abstract void iterate();
	public abstract boolean isDone();
}
