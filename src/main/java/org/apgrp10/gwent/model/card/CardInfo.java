package org.apgrp10.gwent.model.card;

import org.apgrp10.gwent.model.Faction;

public abstract class CardInfo
{
	private final String name;
	private final Faction faction;
	
	protected CardInfo(String name, Faction faction)
	{
		this.name = name;
		this.faction = faction;
	}
	
	protected static CardInfo search(CardInfo[] list, String cardname)
	{
		// TODO: SEARCH MARBOOTE
		return null;
	}
	
	public static CardInfo of(String cardname)
	{
		CardInfo unit = UnitCardInfo.search(cardname);
		CardInfo spec = SpecialCardInfo.of(cardname);
		return (unit != null) ? unit : spec;
	}
	
	public abstract Card makeNewCard();
	
	public String getName()
	{
		return this.name;
	}
}
