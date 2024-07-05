package org.apgrp10.gwent.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class Utils {
	public static String chooseFileToUpload(String title, Window owner) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gwent Files", "*.gwent"));
		File downloadFolder = new File(System.getProperty("user.home"), "Downloads");
		fileChooser.setInitialDirectory(downloadFolder);
		File selectedFile = fileChooser.showOpenDialog(owner);
		return selectedFile != null? selectedFile.getAbsolutePath(): null;
	}

	public static void choosePlaceAndDownload(String title, String defaultFileName, Window owner, String content) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialFileName(defaultFileName);
		String home = System.getProperty("user.home");
		File downloadFolder = new File(home, "Downloads");
		fileChooser.setInitialDirectory(downloadFolder);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gwent Files", "*.gwent"));
		File selectedDir = fileChooser.showSaveDialog(owner);
		if (selectedDir != null) {
			try {
				File myFile = new File(selectedDir.getAbsolutePath());
				FileWriter myWriter = new FileWriter(myFile);
				myWriter.write(content);
				myWriter.close();
			} catch (Exception ignored) {
			}
		}
	}

	public static String loadFile(String fileAddress) {
		try {
			File file = new File(fileAddress);
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder text = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line).append("\n");
			}
			br.close();
			return text.toString();
		} catch (Exception e) {
			ANSI.logError(System.err, "Error in loading " + fileAddress, e);
			return null;
		}
	}
}
