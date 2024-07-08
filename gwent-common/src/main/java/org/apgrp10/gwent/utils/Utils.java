package org.apgrp10.gwent.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apgrp10.gwent.model.User;

public class Utils {
	public static String chooseFileToUpload(String title, Window owner) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gwent Files", "*.gwent"));
		File downloadFolder = new File(System.getProperty("user.home"), "Downloads");
		fileChooser.setInitialDirectory(downloadFolder);
		File selectedFile = fileChooser.showOpenDialog(owner);
		return selectedFile != null ? selectedFile.getAbsolutePath() : null;
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

	public static int levenshteinDistance(String s1, String s2) {
		int len1 = s1.length();
		int len2 = s2.length();

		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			for (int j = 0; j <= len2; j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
				}
			}
		}

		return dp[len1][len2];
	}

	public static double calculateSimilarityPercentage(String s1, String s2) {
		int distance = levenshteinDistance(s1, s2);
		int maxLen = Math.max(s1.length(), s2.length());
		return maxLen == 0 ? 100 : (1 - (double) distance / maxLen) * 100;
	}

	// Use LevenshteinDistance to find the closest objects to the query
	public static <T> List<T> search(List<T> list, Function<T, String> stringFunction, String query, int limit) {
		PriorityQueue<ObjSimilarity<T>> top = new PriorityQueue<>(limit, Comparator.comparingDouble(us -> -us.similarity));
		list.parallelStream().forEach(element -> {
			double similarity = Utils.calculateSimilarityPercentage(query, stringFunction.apply(element));
			if (similarity >= 50.0) {  // Only consider users with at least 50% similarity
				if (top.size() < limit) {
					top.add(new ObjSimilarity<>(element, similarity));
				} else if (top.peek().similarity < similarity) {
					top.poll();
					top.add(new ObjSimilarity<>(element, similarity));
				}
			}
		});
		return top.stream()
				.sorted(Comparator.comparingDouble(us -> -us.similarity))
				.map(us -> us.obj)
				.collect(Collectors.toList());
	}

	private record ObjSimilarity<T>(T obj, double similarity) {}
}
