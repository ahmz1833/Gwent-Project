package org.apgrp10.gwent.model;

import org.apgrp10.gwent.model.card.Card;

import java.io.Serializable;
import java.util.ArrayList;

public class Deck implements Serializable
{
	private final ArrayList<Card> deck = new ArrayList<>();
	private final Faction faction;
	private Leader leader;
	
	public Deck(Faction faction)
	{
		this.faction = faction;
	}
	
	public static Deck loadDeck(String fileAddress)
	{
		return null;
	}
	
	public Leader getLeader()
	{
		return leader;
	}
	
	public void setLeader(Leader leader)
	{
		this.leader = leader;
	}

	
	public void saveDeck(String fileAddress)
	{

	}
}
