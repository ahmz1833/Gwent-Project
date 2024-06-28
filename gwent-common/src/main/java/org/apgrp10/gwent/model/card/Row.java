package org.apgrp10.gwent.model.card;

public enum Row {
	CLOSED, RANGED, SIEGE, AGILE, LEADER, NON;
	public static Row getEnum(String name){
		if(name.equals("ranged"))
			return RANGED;
		if(name.equals("close"))
			return CLOSED;
		if(name.equals("siege"))
			return SIEGE;
		if(name.equals("agile"))
			return AGILE;
		if(name.equals("leader"))
			return LEADER;
		return NON;
	}
}
