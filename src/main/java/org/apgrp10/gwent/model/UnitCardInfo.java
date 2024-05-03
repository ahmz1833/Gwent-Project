package org.apgrp10.gwent.model;

public class UnitCardInfo
{
	
	//TODO:
	private static final UnitCardInfo[] allCards = {
	
	};
	public final String name;
	public final int power;
	public final Type type;
	public final Ability ability;
	public final boolean isHero;
	public final Action cardAction;
	public final Faction faction;
	
	public UnitCardInfo(Faction faction, String name, int power, Type type, Ability ability, boolean isHero, Action cardAction)
	{
		this.faction = faction;
		this.name = name;
		this.power = power;
		this.type = type;
		this.ability = ability;
		this.isHero = isHero;
		this.cardAction = cardAction;
	}
	
	public enum Ability
	{
		FOO(null),
		BAR(null);
		
		Ability(Action abilityAction)
		{
		
		}
	}
	
	public enum Type
	{
		CLOSE(),
		RANGED(),
		SIEGE(),
		AGILE(),
		SPECIAL();
		
		Type()
		{
		
		}
		
	}
}
