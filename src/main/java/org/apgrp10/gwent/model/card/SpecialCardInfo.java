package org.apgrp10.gwent.model.card;

import org.apgrp10.gwent.model.Faction;

public class SpecialCardInfo extends CardInfo
{
	private Class<? extends Card> type;
	private static final SpecialCardInfo[] allCards = {
			new SpecialCardInfo("", Faction.Monsters, WeatherCard.class)
	};
	
	private SpecialCardInfo(String name, Faction faction, Class<? extends Card> type)
	{
		super(name, faction);
		this.type = type;
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
