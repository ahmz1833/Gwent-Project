package org.apgrp10.gwent.model;

import java.util.ArrayList;

public class CombatZone
{
	private final ArrayList<UnitCard> normal = new ArrayList<>();
	
	;
	private SpellCard specialZone;
	
	public CombatZone(Row row)
	{
	
	}
	
	public int getTotalPower()
	{
		return 0;
	}
	
	enum Row
	{CLOSED, RANGED, SIEGE}
}
