package org.apgrp10.gwent;

import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UtilsTest {
	@Test
	public void showDialog() {
		try {
			Utils.chooseFileToUpload("choose", null);
			assert false;
		} catch (Exception ignored) {
			assert true;
		}
		try {
			Utils.choosePlaceAndDownload("choose", "test", null, "test");
			assert false;
		} catch (Exception ignored) {
			assert true;
		}
	}

	@Test
	public void loadFile() {
		String test = Utils.loadFile("....."); //should write error
		String cards = Utils.loadFile(R.get("cards.json").getPath());
		assert Objects.requireNonNull(cards).startsWith("[\n" + "  {\n" + "    " +
		                                                "\"name\": \"Mysterious Elf\",\n" + "    \"id\": \"142\",\n" + "    " +
		                                                "\"deck\": \"neutral\",\n" + "    \"row\": \"close\",\n" + "    \"strength\":" +
		                                                " \"0\",\n" + "    \"ability\": \"hero spy\",\n" + "    \"filename\": " +
		                                                "\"mysterious_elf\",\n" + "    \"count\": \"1\",\n" + "    \"hero\": \"true\"\n"
		                                                + "  },\n" + "  {\n" + "    \"name\": \"Decoy\",\n" + "    \"id\": \"1\",\n" + "" +
		                                                "    \"deck\": \"special\",\n" + "    \"row\": \"\",\n" + "    \"strength\": \"\"," +
		                                                "\n" + "    \"ability\": \"decoy\",\n" + "    \"filename\": \"decoy\",\n" + "    \"" +
		                                                "count\": \"3\",\n" + "    \"hero\": \"false\"\n" + "  },");
	}

	@Test
	public void calculateDistance(){
		int d = Utils.levenshteinDistance("abcdefg", "bcdefj");
		assert d == 2;
		double dis = (Utils.calculateSimilarityPercentage("abcdefg", "bcdefj"));
		assert  dis > 71 && dis < 72;
	}
	@Test
	public void search(){
		List<User> users = new ArrayList<>();
		users.add(new User(new User.RegisterInfo(new User.PublicInfo(10, "abcd", "", Avatar.random()),
				"f", "test", "f")));
		users.add(new User(new User.RegisterInfo(new User.PublicInfo(10, "abcq", "", Avatar.random()),
				"f", "test", "f")));
		users.add(new User(new User.RegisterInfo(new User.PublicInfo(10, "juyt", "", Avatar.random()),
				"f", "test", "f")));
		users.add(new User(new User.RegisterInfo(new User.PublicInfo(10, "fdre", "", Avatar.random()),
				"f", "test", "f")));
		List<User> searched = Utils.search(users, User::username, "abcd", 20);
		assert  searched.size() == 2;

	}
}
