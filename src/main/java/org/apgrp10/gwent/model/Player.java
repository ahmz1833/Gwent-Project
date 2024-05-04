package org.apgrp10.gwent.model;

import java.util.ArrayList;

public class Player
{
	private final User user;
	private CombatZone closedCombat, siegeCombat, rangedCombat;
	private final Deck deck;
	private final ArrayList<Card> hand = new ArrayList<>();
	
	
	public Player(User user, Deck deck) {
		this.user = user;
		this.deck = deck;
	}
	
	public CombatZone getCombatZoneOf(CombatZone.Row ROW)
	{
		return null;
	}
	
	public boolean isOKZone(UnitCard card, CombatZone.Row row)
	{
		return false;
	}
	
	
}
