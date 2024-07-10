package org.apgrp10.gwent.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class R {
	// getImage is used for loading cards each time a new card is made which is sloooow!
	// so we cache the results
	private static Map<String, Image> imageCache = new HashMap<>();

	static {
		loadFont("Comfortaa", "Regular", "Light", "Medium", "SemiBold", "Bold");
		loadFont("OpenSans", "Regular", "Italic", "Light", "LightItalic", "SemiBold", "SemiBoldItalic", "Bold", "BoldItalic", "ExtraBold", "ExtraBoldItalic");
		loadFont("Roboto", "Regular", "Italic", "Thin", "ThinItalic", "Light", "LightItalic", "Medium", "MediumItalic", "Bold", "BoldItalic", "Black", "BlackItalic");
		loadFont("Vazirmatn", "Regular", "Thin", "ExtraLight", "Light", "Medium", "SemiBold", "Bold", "ExtraBold", "Black");
		loadFont("VisbyRoundCF", "Regular", "ExtraLight", "Light", "Medium", "DemiBold", "Bold", "ExtraBold", "Heavy");
		loadFont("Yrsa", "Regular", "Italic", "Light", "Medium", "SemiBold", "Bold");
	}

	private R() {}

	public static <T> T getFXML(String name) {
		try {
			return FXMLLoader.load(get("fxml/" + name));
		} catch (IOException e) {
			throw new RuntimeException("Failed in fetching Resource with name " + name, e);
		}
	}

	public static Image getImage(String name) {
		Image ans = imageCache.get(name);
		if (ans == null) {
			ans = new Image(getAsStream("image/" + name));
			imageCache.put(name, ans);
		}
		return ans;
	}

	public static AudioClip getAudio(String name) {
		return new AudioClip(get("sound/" + name).toExternalForm());
	}

	public static Media getMedia(String name) {
		return new Media(get("sound/" + name).toExternalForm());
	}

	public static URL get(String path) {
		return R.class.getResource(path);
	}

	public static String getAbsPath(String resource) {
		return URLDecoder.decode(get(resource).getFile(), StandardCharsets.UTF_8);
	}

	public static InputStream getAsStream(String path) {
		return R.class.getResourceAsStream(path);
	}

	public static void loadFont(String family, String attr) {
		Font.loadFonts(getAsStream("fonts/" + family + "/" + family + "-" + attr + ".ttf"), 16);
	}

	public static void loadFont(String family, String... attrs) {
		for (String attr : attrs)
			loadFont(family, attr);
	}

	public static class scene {
		public static final Scene login = getFXML("login.fxml");
		public static final Scene main = getFXML("main.fxml");
	}

	public static class image {
		public static final Image board[] = {getImage("board.jpg"), getImage("board_rev.jpg")};
		public static final Image frost = getImage("icons/overlay_frost.png");
		public static final Image fog = getImage("icons/overlay_fog.png");
		public static final Image rain = getImage("icons/overlay_rain.png");
		public static final Image scorch = getImage("icons/anim_scorch.png");
		public static final Image gem_on = getImage("icons/icon_gem_on.png");

		public static final Image reactions[] = {
			getImage("reacts/1.png"),
			getImage("reacts/2.png"),
			getImage("reacts/3.png"),
			getImage("reacts/4.png"),
			getImage("reacts/5.png"),
			getImage("reacts/6.png"),
			getImage("reacts/7.png"),
			getImage("reacts/8.png"),

		};
	}

	public static class icon {
		public static final Image app_icon = getImage("ic_gwent.png");
		public static final Image profile = getImage("ic_profile.png");
		public static final Image login = getImage("ic_login.png");
		public static final Image chat = getImage("ic_chat.png");
	}

	public static class sound {
//		public static AudioClip nuclear_explosion = R.getAudio("nuclear_explosion.wav");
	}
}
