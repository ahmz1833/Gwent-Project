package org.apgrp10.gwent;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class R
{
	static {
		loadFont("Comfortaa", "Regular", "SemiBold", "Light", "Bold", "Medium", "Variable");
		loadFont("OpenSans", "Regular", "Bold", "BoldItalic", "ExtraBold", "Italic", "Light", "LightItalic", "SemiBold", "SemiBoldItalic");
		loadFont("Roboto", "Regular", "Thin", "Black", "Bold", "Bold", "Light", "Medium");
		loadFont("VarelaRound", "Regular");
		loadFont("VisbyRoundCF", "Regular", "SemiBold", "Light", "Bold", "ExtraBold", "ExtraLight", "Medium", "Variable");
	}
	
	private R() {}
	
	public static <T> T getFXML(String name)
	{
		try {
			return FXMLLoader.load(get("fxml/" + name));
		} catch (IOException e) {
			throw new RuntimeException("Failed in fetching Resource with name " + name, e);
		}
	}
	
	public static Image getImage(String name)
	{
		return new Image(getAsStream("image/" + name));
	}
	
	public static AudioClip getAudio(String name)
	{
		return new AudioClip(get("sound/" + name).toExternalForm());
	}
	
	public static Media getMedia(String name)
	{
		return new Media(get("sound/" + name).toExternalForm());
	}
	
	public static URL get(String path)
	{
		return R.class.getResource(path);
	}
	
	public static InputStream getAsStream(String path)
	{
		return R.class.getResourceAsStream(path);
	}
	
	public static Font loadFont(String family, String attr)
	{
		return Font.loadFont(getAsStream("fonts/" + family + "/" + family + "-" + attr + ".ttf"), 16);
	}
	
	public static void loadFont(String family, String... attrs)
	{
		for (String attr : attrs)
			loadFont(family, attr);
	}
	
	public static class scene
	{
		public static final Scene login = getFXML("login.fxml");
	}
	
	public static class image
	{
	
	}
	
	public static class icon
	{
//		public static final Image app_icon = getImage("ic_app.png");
	}
	
	public static class sound
	{
//		public static AudioClip nuclear_explosion = R.getAudio("nuclear_explosion.wav");
	}
}
