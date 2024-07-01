package org.apgrp10.gwent.server;

import java.util.ArrayList;
import java.util.List;

public class HandlingThread extends Thread {
	private final List<Task> tasks = new ArrayList<>();

	public void removeTask(Task task) { synchronized (tasks) { tasks.remove(task); } }
	public void addTask(Task task) { synchronized (tasks) { tasks.add(task); } }
	
	@Override
	public void run() {
		while (true) {
			List<Task> copy, toBeRemoved = new ArrayList<>();
			synchronized (tasks) { copy = new ArrayList<>(tasks); }
			for (Task task : copy) {
				if (task.isDone())
					toBeRemoved.add(task);
				else
					task.run();
			}
			synchronized (tasks) { tasks.removeAll(toBeRemoved); }
			try { Thread.sleep(1); } catch (Exception e) {}
		}
	}
}
