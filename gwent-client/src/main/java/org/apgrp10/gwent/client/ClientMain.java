package org.apgrp10.gwent.client;

import org.apgrp10.gwent.utils.ANSI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ClientMain {
	public static final boolean USE_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	public static final String APP_DATA = System.getProperty("user.home") + "/.gwentdata/";
	
	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		Path path = Paths.get(APP_DATA);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
				System.out.println(ANSI.LYELLOW.bd() + "app directory created at: " + APP_DATA + ANSI.RST + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Gwent.main(args);
	}
}
