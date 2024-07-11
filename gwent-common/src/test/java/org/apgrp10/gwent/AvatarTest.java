package org.apgrp10.gwent;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.utils.MGson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;

public class AvatarTest {
	private void testSingle(Avatar avatar) {
		Avatar avatar2 = MGson.fromJson(MGson.toJson(avatar), Avatar.class);
		assert avatar.toString().equals(avatar2.toString());
		assert avatar2.getViewableImage() != null;
	}

	@Test
	public void testDefault() throws Exception {
		testSingle(Avatar.random());
		testSingle(Avatar.random());
		testSingle(Avatar.random());
	}

	@Test
	public void testImage() throws Exception {
		testSingle(Avatar.fromImage(new Image(R.getAsStream("avatars/test.png"))));
	}
}
