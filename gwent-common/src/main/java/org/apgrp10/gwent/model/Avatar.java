package org.apgrp10.gwent.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;

@JsonAdapter(Avatar.AvatarAdapter.class)
public class Avatar {
	public static final ArrayList<Avatar> allAvatars = new ArrayList<>();
	private final Image image;

	private Avatar(Image image) {
		this.image = image;
		allAvatars.add(this);
	}

	public static Avatar fromImage(Image image) {
		if (image == null) return null;
		for (Avatar avatar : allAvatars) {
			if (avatar.image.equals(image)) return avatar;
		}
		return new Avatar(image);
	}

	public static Avatar random() {
		return R.DEFAULT_AVATARS.get(Random.nextInt(0, R.DEFAULT_AVATARS.size()));
	}

	private static Avatar fromString(String string) throws IllegalArgumentException {
		return fromBase64(string);
	}

	public static Avatar fromBase64(String value) {
		int number = -1;
		try {
			number = Integer.parseInt(value);
		} catch (Exception ignored) {}
		if (number >= 0 && number < R.DEFAULT_AVATARS.size()) return R.DEFAULT_AVATARS.get(number);
		else {
			byte[] decoded = Base64.getDecoder().decode(value);
			return Avatar.fromImage(imageFromBytes(decoded));
		}
	}

	private static Image imageFromBytes(byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			SerializableImage si = (SerializableImage) ois.readObject();
			return si.getImage();
		} catch (Exception e) {
			return null;
		}
	}

	private static byte[] imageToBytes(Image image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ous = new ObjectOutputStream(baos);
			SerializableImage si = new SerializableImage();
			si.setImage(image);
			ous.writeObject(si);
			return baos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	public Image getViewableImage() {
		//crop image to center square
		PixelReader reader = image.getPixelReader();
		int size = Math.min((int) image.getWidth(), (int) image.getHeight());
		int x = (int) (image.getWidth() / 2) - (size / 2);
		int y = (int) (image.getHeight() / 2) - (size / 2);
		return new WritableImage(reader, x, y, size, size);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Avatar) return ((Avatar) obj).image.equals(this.image);
		else return false;
	}

	@Override
	public String toString() {
		return toBase64();
	}

	public String toBase64() {
		if (R.DEFAULT_AVATARS.contains(this)) return String.valueOf(R.DEFAULT_AVATARS.indexOf(this));
		return Base64.getEncoder().encodeToString(imageToBytes(image));
	}


	private static class SerializableImage implements Serializable {
		private int width, height;
		private int[][] data;

		public SerializableImage() {
		}

		public void setImage(Image image) {
			width = ((int) image.getWidth());
			height = ((int) image.getHeight());
			data = new int[width][height];

			PixelReader r = image.getPixelReader();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					data[i][j] = r.getArgb(i, j);
				}
			}
		}

		public Image getImage() {
			WritableImage img = new WritableImage(width, height);

			PixelWriter w = img.getPixelWriter();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					w.setArgb(i, j, data[i][j]);
				}
			}

			return img;
		}

	}

	static class AvatarAdapter extends TypeAdapter<Avatar> {
		@Override
		public void write(JsonWriter out, Avatar value) throws IOException {
			out.value(value.toString());
		}

		@Override
		public Avatar read(JsonReader in) throws IOException {
			try {
				return Avatar.fromString(in.nextString());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}
}
