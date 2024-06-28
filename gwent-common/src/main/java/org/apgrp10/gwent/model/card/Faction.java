package org.apgrp10.gwent.model.card;

public enum Faction {
	SKELLIGE, SCOIATAEL, REALMS, NILFGAARD, MONSTERS, SPECIAL, WEATHER, NATURAL;

	public static Faction getEnum(String name) {
		if (name.equals("neutral")) return NATURAL;
		if (name.equals("special")) return SPECIAL;
		if (name.equals("weather")) return WEATHER;
		if (name.equals("realms")) return REALMS;
		if (name.equals("nilfgaard")) return NILFGAARD;
		if (name.equals("monsters")) return MONSTERS;
		if (name.equals("scoiatael")) return SCOIATAEL;
		return SKELLIGE;
	}
}
