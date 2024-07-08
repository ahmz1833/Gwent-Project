package org.apgrp10.gwent.server;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;

public class TaskManager {
	private static HandlingThread threads[];

	private TaskManager() {}

	public static synchronized void init(int numThreads) {
		assert threads == null;
		threads = new HandlingThread[numThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new HandlingThread();
			threads[i].setName("TaskManager-" + i); // [1]
			threads[i].start();
		}
	}

	public static synchronized void submit(Task task) {
		int index = Random.nextInt(0, threads.length);
		threads[index].addTask(task);
	}

	private static class HandlingThread extends Thread {
		private final List<Task> tasks = new ArrayList<>();

		public void removeTask(Task task) {synchronized (tasks) {tasks.remove(task);}}

		public void addTask(Task task) {synchronized (tasks) {tasks.add(task);}}

		@Override
		public void run() {
			while (true) {
				List<Task> copy, toBeRemoved = new ArrayList<>();
				synchronized (tasks) {copy = new ArrayList<>(tasks);}
				for (Task task : copy) {
					if (task.isDone())
						toBeRemoved.add(task);
					else
						try {
							task.run();
						} catch (Exception e)
						{
							ANSI.logError(System.err, "In Thread : " + getName(), e);
						}
				}
				synchronized (tasks) {tasks.removeAll(toBeRemoved);}
				try {Thread.sleep(1);} catch (Exception e) {}
			}
		}
	}
}

