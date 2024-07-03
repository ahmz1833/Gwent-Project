package org.apgrp10.gwent.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.utils.Random;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

@JsonAdapter(Avatar.AvatarAdapter.class)
public class Avatar
{
	public static final ArrayList<Avatar> allAvatars = new ArrayList<>();
	private final Image image;
	
	private Avatar(Image image)
	{
		this.image = image;
		allAvatars.add(this);
	}

	public static Avatar fromImage(Image image)
	{
		for (Avatar avatar : allAvatars) {
			if (avatar.image.equals(image)) return avatar;
		}
		return new Avatar(image);
	}
	
	public static Avatar random()
	{
		return R.ALL_AVATARS.get(Random.nextInt(0, R.ALL_AVATARS.size()));
	}

	private static Avatar fromString(String string) throws IllegalArgumentException
	{
		int number = -1;
		try {
			number = Integer.parseInt(string);
		} catch (Exception ignored) {}
		if (number > 0 && number < R.ALL_AVATARS.size()) return R.ALL_AVATARS.get(number);
		else return Avatar.fromImage(new Image(string));
	}

	public static Avatar fromBase64(String value) {
		int number = -1;
		try {
			number = Integer.parseInt(value);
		} catch (Exception ignored) {}
		if (number > 0 && number < R.ALL_AVATARS.size()) return R.ALL_AVATARS.get(number);
		else {
			byte[] decoded = Base64.getDecoder().decode(value);
			return Avatar.fromImage(new Image(new ByteArrayInputStream(decoded)));
		}
	}

	public Image getViewableImage()
	{
		//crop image to center square
		PixelReader reader = image.getPixelReader();
		int size = Math.min((int) image.getWidth(), (int) image.getHeight());
		int x = (int) (image.getWidth() / 2) - (size / 2);
		int y = (int) (image.getHeight() / 2) - (size / 2);
		WritableImage newImage = new WritableImage(reader, x, y, size, size);
		
		return newImage;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Avatar) return ((Avatar) obj).image.equals(this.image);
		else return false;
	}
	
	@Override
	public String toString()
	{
		if (allAvatars.contains(this)) return String.valueOf(allAvatars.indexOf(this));
		return toBase64();
	}

	public String toBase64() {
		byte[] bytes = new byte[0];
		try {

		} catch (IOException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encode(bytes);
	}

	static class AvatarAdapter extends TypeAdapter<Avatar>
	{
		@Override
		public void write(JsonWriter out, Avatar value) throws IOException
		{
			out.value(value.toString());
		}
		
		@Override
		public Avatar read(JsonReader in) throws IOException
		{
			try {
				return Avatar.fromString(in.nextString());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}
}
