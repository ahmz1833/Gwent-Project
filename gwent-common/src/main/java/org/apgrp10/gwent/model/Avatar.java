package org.apgrp10.gwent.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.Random;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		if (number > 0 && number < R.DEFAULT_AVATARS.size()) return R.DEFAULT_AVATARS.get(number);
		else {
			byte[] decoded = Base64.getDecoder().decode(value);
			return Avatar.fromImage(imageFromBytes(decoded));
		}
	}

	private static Image imageFromBytes(byte[] bytes) {
		// Read the byte array into a BufferedImage
		ByteArrayInputStream input = new ByteArrayInputStream(bytes);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(input);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to read image from byte array", e);
			return null;
		}

		// Convert the BufferedImage to a JavaFX Image
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		ByteBuffer byteBuffer = ByteBuffer.allocate(width * height * 4);
		return new Image(new ByteArrayInputStream(byteBuffer.array()), width, height, true, true);
	}

	private static byte[] imageToBytes(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader pixelReader = image.getPixelReader();

		// Create a BufferedImage and get its pixel data
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = pixelReader.getColor(x, y);
				int argb = ((int) (color.getOpacity() * 255) << 24) | ((int) (color.getRed() * 255) << 16)
				           | ((int) (color.getGreen() * 255) << 8) | ((int) (color.getBlue() * 255));
				bufferedImage.setRGB(x, y, argb);
			}
		}

		// Write the BufferedImage to a byte array
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "png", output);
		} catch (IOException e) {
			ANSI.logError(System.err, "Failed to write image to byte array", e);
		}
		return output.toByteArray();
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
		if (allAvatars.contains(this)) return String.valueOf(allAvatars.indexOf(this));
		return Base64.getEncoder().encodeToString(imageToBytes(image));
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
