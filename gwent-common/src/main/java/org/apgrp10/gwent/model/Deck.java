package org.apgrp10.gwent.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardInfo;
import org.apgrp10.gwent.model.card.Faction;
import org.apgrp10.gwent.model.card.Row;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import java.util.*;

public class Deck {
	private final ArrayList<Card> deck = new ArrayList<>();
	private Faction faction;
	private Card leader;

	public Deck(int factionId, String leaderName) {
		switch (factionId) {
			case 0 -> faction = Faction.REALMS;
			case 1 -> faction = Faction.NILFGAARD;
			case 2 -> faction = Faction.MONSTERS;
			case 3 -> faction = Faction.SCOIATAEL;
			case 4 -> faction = Faction.SKELLIGE;
			default -> faction = null;
		}
		Card leaderCopy = null;
		for (CardInfo cardInfo : CardInfo.allCards) {
			if ((cardInfo.pathAddress.equals(leaderName) || cardInfo.name.equals(leaderName)) && cardInfo.row.equals(Row.LEADER)) {
				leaderCopy = convertCortInfoToCard(cardInfo);
				break;
			}
		}
		leader = leaderCopy;
	}

	public void setLeaderAndFaction(Card card) {
		faction = card.faction;
		leader = card;
	}

	public static boolean isCorrectDeck(Deck deck) {
		if (deck == null) return false;
		if (deck.leader == null || deck.faction == null) {
			return false;
		}
		//check that leader is for correct faction
		boolean found = false;
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.pathAddress.equals(deck.leader.pathAddress) && cardInfo.row.equals(Row.LEADER)) {
				found = true;
				if (!cardInfo.faction.equals(deck.faction)) {
					return false;
				}
			}
		}
		if (!found) {
			return false;
		}
		//check that cards are in correct faction
		for (Card card : deck.deck) {
			if ((!card.faction.equals(deck.faction) && !card.faction.equals(Faction.NATURAL) && !card.faction.equals(Faction.WEATHER) && !card.faction.equals(Faction.SPECIAL)) || card.row.equals(Row.LEADER))
				return false;
		}
		//check count of cards
		for (Card card : deck.deck)
			for (CardInfo cardInfo : CardInfo.allCards)
				if (cardInfo.pathAddress.equals(card.pathAddress))
					if (getSimilarCountInDeck(deck, card.pathAddress) > cardInfo.count) return false;
		return true;
	}

	private static int getSimilarCountInDeck(Deck deck, String path) {
		int count = 0;
		for (Card card : deck.deck) {
			if (card.pathAddress.equals(path)) count++;
		}
		return count;
	}

	public static Deck loadDeckFromFile(String fileAddress) {
		try {
			String text = Utils.loadFile(fileAddress);
			Deck deck = fromJsonString(text);
			assert deck != null;
			return deck;
		} catch (Exception e) {
			ANSI.logError(System.err, "Error in loading deck from " + fileAddress, e);
			return null;
		}
	}

	public static Deck fromJson(JsonElement deck) {
		return DeckToSave.loadFromJson(deck);
	}

	public JsonElement toJson() {
		DeckToSave deckToSave = new DeckToSave();
		deckToSave.changeFaction(this.faction);
		deckToSave.changeLeader(this.leader);
		for (Card card : this.deck) {
			deckToSave.addCard(card.pathAddress);
		}
		return MGson.toJsonElement(deckToSave);
	}

	public static Deck fromJsonString(String string) {
		return fromJson(MGson.fromJson(string, JsonObject.class));
	}

	public String toJsonString() {
		return toJson().toString();
	}

	public static Deck fromBase64(String base64) {
		return fromJsonString(new String(Base64.getDecoder().decode(base64)));
	}

	public String toBase64() {
		return Base64.getEncoder().encodeToString(toJsonString().getBytes());
	}

	public static Card convertCortInfoToCard(CardInfo cardInfo) {
		return new Card(cardInfo.name, cardInfo.pathAddress, cardInfo.strength, cardInfo.row, cardInfo.faction, cardInfo.ability, cardInfo.isHero);
	}

	public void addCard(Card card) {
		deck.add(card);
	}

	public void removeCard(Card card) {
		deck.remove(card);
	}

	public void addCard(CardInfo cardInfo) {
		addCard(convertCortInfoToCard(cardInfo));
	}

	public Card getLeader() {
		return leader;
	}

	public ArrayList<Card> getDeck() {
		return deck;
	}

	public Faction getFaction() {
		return faction;
	}

	public int assignGameIds(int startingId) {
		int id = startingId;
		for (Card card : deck)
			card.setGameId(id++);
		return id;
	}

	public void shuffle(Random random) {
		Collections.shuffle(deck, random);
	}

	public Deck deepCopy() {
		return fromJson(toJson());
	}

	static class DeckToSave {
		private final List<String> deck = new ArrayList<>();
		private String faction = "";
		private String leader = "";

		private static CardInfo convertPathToCardInfo(String path) {
			for (CardInfo cardInfo : CardInfo.allCards) {
				if (cardInfo.pathAddress.equals(path)) {
					return cardInfo;
				}
			}
			return null;
		}

		public static Deck loadFromJson(JsonElement json) {
			try {
				DeckToSave deckToSave = MGson.fromJson(json, DeckToSave.class);
				int factionIndex = -1;
				switch (deckToSave.faction) {
					case "REALMS" -> factionIndex = 0;
					case "NILFGAARD" -> factionIndex = 1;
					case "MONSTERS" -> factionIndex = 2;
					case "SCOIATAEL" -> factionIndex = 3;
					case "SKELLIGE" -> factionIndex = 4;
				}
				Deck outputDeck = new Deck(factionIndex, deckToSave.leader);
				for (String path : deckToSave.deck) {
					CardInfo cardInfo = convertPathToCardInfo(path);
					if (cardInfo == null) return null;
					outputDeck.addCard(cardInfo);
				}
				if (!isCorrectDeck(outputDeck)) return null;
				return outputDeck;
			} catch (Exception ignored) {
				return null;
			}
		}

		public void addCard(String path) {
			deck.add(path);
		}

		public void changeLeader(Card leader) {
			this.leader = leader.pathAddress;
		}

		public void changeFaction(Faction faction) {
			this.faction = faction.name();
		}

		public String getJson() {
			return MGson.get(true, true).toJson(this);
		}
	}
}


