package org.apgrp10.gwent.model.card;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apgrp10.gwent.R;

import java.util.*;

public class CardInfo {
	public static final ArrayList<CardInfo> allCards = new ArrayList<>();

	static {
		new CardLoader();
		String[] sortOrder = {"special", "weather", "natural"};
		allCards.sort((o1, o2) ->
		{
			int index1 = 4, index2 = 4;
			for(int i = 0; i < sortOrder.length; i++){
				if(Faction.getEnum(sortOrder[i]).equals(o1.faction))
					index1 = i;
				if(Faction.getEnum(sortOrder[i]).equals(o2.faction))
					index2 = i;
			}
			if(index1 < index2)
				return -1;
			if(index1 > index2)
				return 1;
			if(o1.strength == o2.strength)
				return o1.name.compareTo(o2.name);
			return (o1.strength > o2.strength)? -1: +1;
		});
	}

	public final String name, pathAddress;
	public final int id, count, strength;
	public final Row row;
	public final Faction faction;
	public final Ability ability;

	CardInfo(String name, String pathAddress, int id, int count, int strength, Row row, Faction faction, Ability ability) {
		this.name = name;
		this.pathAddress = pathAddress;
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
			new CardInfo(card.name, card.deck + "_" + card.filename, id, count, strength, Row.getEnum(card.row), Faction.getEnum(card.deck), Ability.getEnum(card.ability));
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
