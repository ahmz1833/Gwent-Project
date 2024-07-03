package org.apgrp10.gwent;

import javafx.scene.image.Image;
import org.apgrp10.gwent.model.Avatar;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class R {
	public static List<Avatar> ALL_AVATARS = new ArrayList<>();
	static {
		ALL_AVATARS.add(Avatar.fromImage(new Image(getAsStream("/avatars/1.png"))));
		ALL_AVATARS.add(Avatar.fromImage(new Image(getAsStream("/avatars/2.png"))));
		ALL_AVATARS.add(Avatar.fromImage(new Image(getAsStream("/avatars/3.png"))));
		ALL_AVATARS.add(Avatar.fromImage(new Image(getAsStream("/avatars/4.png"))));
		ALL_AVATARS.add(Avatar.fromImage(new Image(getAsStream("/avatars/5.png"))));
	}

	public static URL get(String path) {
		return R.class.getResource(path);
	}

	public static InputStream getAsStream(String path) {
		return R.class.getResourceAsStream(path);
	}
}
