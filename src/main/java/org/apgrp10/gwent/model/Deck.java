package org.apgrp10.gwent.model;

import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.Faction;

import java.io.Serializable;
import java.util.ArrayList;

public class Deck implements Serializable
{
	private final ArrayList<Card> deck = new ArrayList<>();
	private final Faction faction;

	public Deck(Faction faction)
	{
		this.faction = faction;
	}
	
	public static Deck loadDeck(String fileAddress)
	{
		return null;
	}

	public void saveDeck(String fileAddress)
	{

	}
}
