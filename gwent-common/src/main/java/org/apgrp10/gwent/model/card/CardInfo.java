package org.apgrp10.gwent.model.card;

import org.apgrp10.gwent.R;
import org.apgrp10.gwent.utils.MGson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

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

	public static CardInfo byPathAddress(String path) {
		for (CardInfo info : allCards)
			if (info.pathAddress.equals(path))
				return info;
		return null;
	}

	static class CardLoader {
		static HashSet<String> abilities = new HashSet<>();

		static {
			Scanner scanner = new Scanner(R.getAsStream("cards.json")).useDelimiter("\\A");
			String result = scanner.hasNext() ? scanner.next() : "";
			CardLoader[] allCards = MGson.fromJson(result, CardLoader[].class);
			for (CardLoader card : allCards) {
				int id = convertToInt(card.id);
				int count = convertToInt(card.count);
				int strength = convertToInt(card.strength);
				new CardInfo(card.name, card.deck + "_" + card.filename, id, count, strength, Row.getEnum(card.row), Faction.getEnum(card.deck), Ability.getEnum(card.ability), card.hero);
			}
		}

		private String name, deck, row, strength, ability, filename, count, id;
		private boolean hero;

		private static int convertToInt(String text) {
			try {
				return Integer.parseInt(text);
			} catch (Exception ignored) {
				return 0;
			}
		}
	}
}


