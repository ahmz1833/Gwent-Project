package org.apgrp10.gwent.model.card;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apgrp10.gwent.R;

import java.util.*;

public class CardInfo {
	public static final ArrayList<CardInfo> allCards = new ArrayList<>();

	static {
		new CardLoader();
	}

	public final String name, pathAddress;
	public final int id, count, strength;
	public final boolean isHero;
	public final Row row;
	public final Faction faction;
	public final Ability ability;

	CardInfo(String name, String pathAddress, int id, int count, int strength, Row row, Faction faction,
			 Ability ability, boolean isHero) {
		this.name = name;
		this.pathAddress = pathAddress;
		this.isHero = isHero;
		this.id = id;
		this.count = count;
		this.strength = strength;
		this.row = row;
		this.faction = faction;
		this.ability = ability;
		allCards.add(this);
	}
}


class CardLoader {
	private String name, deck, row, strength, ability, filename, count, id;
	private boolean hero;
	static HashSet<String> abilities = new HashSet<>();

	static {
		Scanner scanner = new Scanner(R.getAsStream("cards.json")).useDelimiter("\\A");
		String result = scanner.hasNext() ? scanner.next() : "";
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		CardLoader[] allCards = gson.fromJson(result, CardLoader[].class);
		for (CardLoader card : allCards) {
			int id = convertToInt(card.id);
			int count = convertToInt(card.count);
			int strength = convertToInt(card.strength);
			new CardInfo(card.name, card.deck + "_" + card.filename, id, count, strength, Row.getEnum(card.row), Faction.getEnum(card.deck), Ability.getEnum(card.ability), card.hero);
		}
	}

	private static int convertToInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception ignored) {
			return 0;
		}
	}
}
