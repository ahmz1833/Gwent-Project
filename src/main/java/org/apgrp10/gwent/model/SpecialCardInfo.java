package org.apgrp10.gwent.model;

import org.apgrp10.gwent.controller.CardInfo;

public class SpecialCardInfo extends CardInfo
{
	private static final SpecialCardInfo[] allCards = {
		new SpecialCardInfo("", Faction.Monsters, WeatherCard.class)
	};
	
	private SpecialCardInfo(String name, Faction faction, Class<? extends Card> type)
	{
		super(name, faction);
	}
	
	protected static CardInfo search(String cardname)
	{
		return search(allCards, cardname);
	}
	
	@Override
	public Card makeNewCard()
	{
		return null;
	}
}
