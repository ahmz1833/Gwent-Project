package org.apgrp10.gwent.model.card;

public enum Ability {
	AVENGER,
	AVENGER_KAMBI,
	BERSERKER,
	BOND,
	CLEAR,
	CRACH_AN_CRAITE,
	DECOY,
	EMHYR_EMPEROR,
	EMHYR_IMPERIAL,
	EMHYR_INVADER,
	EMHYR_RELENTLESS,
	EMHYR_WHITEFLAME,
	EREDIN_BRINGER_OF_DEATH,
	EREDIN_COMMANDER,
	EREDIN_DESTROYER,
	EREDIN_KING,
	EREDIN_TREACHEROUS,
	FOG,
	FOLTEST_KING,
	FOLTEST_LORD,
	FOLTEST_SIEGEMASTER,
	FOLTEST_SON,
	FOLTEST_STEELFORGED,
	FRANCESCA_BEAUTIFUL,
	FRANCESCA_DAISY,
	FRANCESCA_HOPE,
	FRANCESCA_PUREBLOOD,
	FRANCESCA_QUEEN,
	FROST,
	HORN,
	KING_BRAN,
	MARDROEME,
	MEDIC,
	MORALE,
	MUSTER,
	RAIN,
	RAIN_FOG,
	SCORCH,
	SCORCH_C,
	SCORCH_R,
	SCORCH_S,
	SPY,
	NON;

	public static Ability getEnum(String name) {
		if (name.equals("emhyr_imperial")) return EMHYR_IMPERIAL;
		if (name.equals("foltest_lord")) return FOLTEST_LORD;
		if (name.equals("eredin_king")) return EREDIN_KING;
		if (name.equals("francesca_pureblood")) return FRANCESCA_PUREBLOOD;
		if (name.equals("francesca_hope")) return FRANCESCA_HOPE;
		if (name.equals("hero spy")) return SPY;
		if (name.equals("emhyr_whiteflame")) return EMHYR_WHITEFLAME;
		if (name.equals("frost")) return FROST;
		if (name.equals("foltest_siegemaster")) return FOLTEST_SIEGEMASTER;
		if (name.equals("emhyr_emperor")) return EMHYR_EMPEROR;
		if (name.equals("francesca_beautiful")) return FRANCESCA_BEAUTIFUL;
		if (name.equals("rain")) return RAIN;
		if (name.equals("eredin_destroyer")) return EREDIN_DESTROYER;
		if (name.equals("foltest_son")) return FOLTEST_SON;
		if (name.equals("king_bran")) return KING_BRAN;
		if (name.equals("avenger")) return AVENGER;
		if (name.equals("hero muster")) return MUSTER;
		if (name.equals("avenger_kambi")) return AVENGER_KAMBI;
		if (name.equals("horn")) return HORN;
		if (name.equals("scorch_c")) return SCORCH_C;
		if (name.equals("scorch")) return SCORCH;
		if (name.equals("emhyr_relentless")) return EMHYR_RELENTLESS;
		if (name.equals("spy")) return SPY;
		if (name.equals("fog")) return FOG;
		if (name.equals("foltest_steelforged")) return FOLTEST_STEELFORGED;
		if (name.equals("mardroeme")) return MARDROEME;
		if (name.equals("eredin_treacherous")) return EREDIN_TREACHEROUS;
		if (name.equals("scorch_s")) return SCORCH_S;
		if (name.equals("scorch_r")) return SCORCH_R;
		if (name.equals("medic")) return MEDIC;
		if (name.equals("eredin_bringer_of_death")) return EREDIN_BRINGER_OF_DEATH;
		if (name.equals("foltest_king")) return FOLTEST_KING;
		if (name.equals("muster")) return MUSTER;
		if (name.equals("eredin_commander")) return EREDIN_COMMANDER;
		if (name.equals("berserker")) return BERSERKER;
		if (name.equals("emhyr_invader")) return EMHYR_INVADER;
		if (name.equals("morale")) return MORALE;
		if (name.equals("decoy")) return DECOY;
		if (name.equals("francesca_daisy")) return FRANCESCA_DAISY;
		if (name.equals("hero morale")) return MORALE;
		if (name.equals("clear")) return CLEAR;
		if (name.equals("bond")) return BOND;
		if (name.equals("hero mardroeme")) return MARDROEME;
		if (name.equals("francesca_queen")) return FRANCESCA_QUEEN;
		if (name.equals("crach_an_craite")) return CRACH_AN_CRAITE;
		if (name.equals("hero medic")) return MEDIC;
		if (name.equals("rain fog")) return RAIN_FOG;
		return NON;
	}
}
