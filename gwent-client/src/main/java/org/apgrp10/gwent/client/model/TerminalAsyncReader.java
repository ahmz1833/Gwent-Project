package org.apgrp10.gwent.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apgrp10.gwent.model.AbstractAsyncReader;
import org.apgrp10.gwent.utils.ANSI;

import javafx.application.Platform;

public class TerminalAsyncReader extends AbstractAsyncReader {
	private static TerminalAsyncReader instance = new TerminalAsyncReader();

	private TerminalAsyncReader() {
		super(System.in, null, null);
		setOnReceive(this::receive);
		setOnFailure(this::failure);
	}

	@Override
	public void run() {
		if (!read())
			return;

		try {
			for (;;) {
				int i = 0;
				while (i < size && buf[i] != (byte)'\n') i++;
				if (i == size) break;
				if (i > 0 && buf[i - 1] == (byte)'\r') { // Windows
					receive.accept(part(0, i - 1));
					skip(i + 1);
				} else { // Any sane OS
					receive.accept(part(0, i));
					skip(i + 1);
				}
			}
		} catch (Exception e) {
			ANSI.logError(System.err, "TerminalAsyncReader failure", e);
			failure.accept(e);
		}
	}

	private void failure(Exception e) {
		running = false;
	}

	private List<Consumer<String>> listeners = new ArrayList<>();

	public static Object addListener(Consumer<String> cb) {
		instance.listeners.add(cb);
		return cb;
	}
	public static void removeListener(Object cb) {
		instance.listeners.remove(cb);
	}

	private void receive(byte b[]) {
		String str = new String(b);
		for (Consumer<String> cb : listeners)
			cb.accept(str);
	}

	private boolean running = false;

	private void fxLoop() {
		if (!running) return;
		Platform.runLater(this::fxLoop);
		run();
	}

	public static void instanceRun() {
		if (!instance.running) {
			instance.running = true;
			instance.fxLoop();
		}
	}

	public static void stop() {
		instance.running = false;
	}
}
