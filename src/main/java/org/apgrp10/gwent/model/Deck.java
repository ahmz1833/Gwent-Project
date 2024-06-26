package org.apgrp10.gwent.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.layout.GridPane;
import org.apgrp10.gwent.model.card.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Deck {
	private final User user;
	private final ArrayList<Card> deck = new ArrayList<>();
	private final Faction faction;
	private final Card leader;

	public Deck(int factionId, String leaderName, User user) {
		this.user = user;
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

	public void createDeckFromPane(GridPane gridPane) {
		for (int i = 0; i < gridPane.getChildren().size(); i++) {
			try {
				CardView cardView = (CardView) gridPane.getChildren().get(i);
				for (CardInfo cardInfo : CardInfo.allCards)
					if (cardInfo.pathAddress.equals(cardView.getAddress())) {
						for (int j = 0; j < cardView.getCount(); j++)
							addCard(convertCortInfoToCard(cardInfo));
					}
			} catch (Exception ignored) {
			}
		}
	}

	private Card convertCortInfoToCard(CardInfo cardInfo) {
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
	public User getUser(){
		return user;
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

	public Card getLeader() {
		return leader;
	}

	public ArrayList<Card> getDeck() {
		return deck;
	}

	public Faction getFaction() {
		return faction;
	}

	public static Deck loadDeck(String fileAddress) {
		try {
			File file = new File(fileAddress);
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder text = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line).append("\n");
			}
			br.close();
			return DeckToSave.loadFromJson(text.toString());
		} catch (Exception e) {
			return null;
		}
	}

	public static String saveDeck(Deck deck) {
		DeckToSave deckToSave = new DeckToSave();
		deckToSave.changeFaction(deck.faction);
		deckToSave.changeLeader(deck.leader);
		for (Card card : deck.deck) {
			deckToSave.addCard(card.pathAddress);
		}
		return deckToSave.getJson();
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
}


class DeckToSave {
	private final HashMap<String, Integer> deck = new HashMap<>();
	private String faction = "";
	private String leader = "";

	public void addCard(String path) {
		deck.putIfAbsent(path, 0);
		deck.put(path, deck.get(path) + 1);
	}

	public void changeLeader(Card leader) {
		this.leader = leader.pathAddress;
	}

	public void changeFaction(Faction faction) {
		this.faction = faction.name();
	}

	public String getJson() {
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	private static CardInfo convertPathToCardInfo(String path) {
		for (CardInfo cardInfo : CardInfo.allCards) {
			if (cardInfo.pathAddress.equals(path)) {
				return cardInfo;
			}
		}
		return null;
	}

	public static Deck loadFromJson(String json) {
		try {
			Gson gson = new Gson();
			DeckToSave deckToSave = gson.fromJson(json, DeckToSave.class);
			int factionIndex = -1;
			switch (deckToSave.faction) {
				case "REALMS" -> factionIndex = 0;
				case "NILFGAARD" -> factionIndex = 1;
				case "MONSTERS" -> factionIndex = 2;
				case "SCOIATAEL" -> factionIndex = 3;
				case "SKELLIGE" -> factionIndex = 4;
			}
			Deck outputDeck = new Deck(factionIndex, deckToSave.leader, null);
			for (String path : deckToSave.deck.keySet()) {
				CardInfo cardInfo = convertPathToCardInfo(path);
				if (cardInfo == null) return null;
				if (deckToSave.deck.get(path) < 0) return null;
				for (int i = 0; i < deckToSave.deck.get(path); i++) {
					outputDeck.addCard(cardInfo);
				}
			}
			if (!Deck.isCorrectDeck(outputDeck)) return null;
			return outputDeck;
		} catch (Exception ignored) {
			return null;
		}
	}
}
