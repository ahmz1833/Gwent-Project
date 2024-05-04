package org.apgrp10.gwent.model;

import org.apgrp10.gwent.controller.CardInfo;

public class UnitCardInfo extends CardInfo
{
	//TODO:
	private static final UnitCardInfo[] allCards = {
//		new UnitCardInfo(),
//			new UnitCardInfo(),
//			new UnitCardInfo(),
//
	};
	public final String name;
	public final int power;
	public final Type type;
	public final Ability ability;
	public final boolean isHero;
	public final Action cardAction;
	public final Faction faction;
	
	private UnitCardInfo(Faction faction, String name, int power, Type type, Ability ability, boolean isHero, Action cardAction)
	{
		super(name, faction);
		this.faction = faction;
		this.name = name;
		this.power = power;
		this.type = type;
		this.ability = ability;
		this.isHero = isHero;
		this.cardAction = cardAction;
	}
	
	public static CardInfo search(String cardname)
	{
		return search(allCards, cardname);
	}
	
	@Override
	public UnitCard makeNewCard()
	{
		return null;
	}
	
	public enum Type
	{
		CLOSE(),
		RANGED(),
		SIEGE(),
		AGILE();
		
		Type()
		{
		
		}
		
	}
}
