package org.apgrp10.gwent.model.card;

public enum Ability {
	EMHYR_IMPERIAL,
	FOLTEST_LORD,
	EREDIN_KING,
	FRANCESCA_PUREBLOOD,
	FRANCESCA_HOPE,
	HERO_SPY,
	EMHYR_WHITEFLAME,
	FROST,
	FOLTEST_SIEGEMASTER,
	EMHYR_EMPEROR,
	FRANCESCA_BEAUTIFUL,
	RAIN,
	EREDIN_DESTROYER,
	FOLTEST_SON,
	KING_BRAN,
	AVENGER,
	HERO_MUSTER,
	AVENGER_KAMBI,
	HORN,
	SCORCH_C,
	SCORCH,
	EMHYR_RELENTLESS,
	SPY,
	FOG,
	FOLTEST_STEELFORGED,
	MARDROEME,
	EREDIN_TREACHEROUS,
	SCORCH_S,
	SCORCH_R,
	MEDIC,
	EREDIN_BRINGER_OF_DEATH,
	HERO,
	FOLTEST_KING,
	MUSTER,
	EREDIN_COMMANDER,
	BERSERKER,
	EMHYR_INVADER,
	MORALE,
	DECOY,
	FRANCESCA_DAISY,
	HERO_MORALE,
	CLEAR,
	BOND,
	HERO_MARDROEME,
	FRANCESCA_QUEEN,
	CRACH_AN_CRAITE,
	HERO_MEDIC,
	RAIN_FOG,
	NON;

	public static Ability getEnum(String name) {
		if (name.equals("emhyr_imperial")) return EMHYR_IMPERIAL;
		if (name.equals("foltest_lord")) return FOLTEST_LORD;
		if (name.equals("eredin_king")) return EREDIN_KING;
		if (name.equals("francesca_pureblood")) return FRANCESCA_PUREBLOOD;
		if (name.equals("francesca_hope")) return FRANCESCA_HOPE;
		if (name.equals("hero spy")) return HERO_SPY;
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
		if (name.equals("hero muster")) return HERO_MUSTER;
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
		if (name.equals("hero")) return HERO;
		if (name.equals("foltest_king")) return FOLTEST_KING;
		if (name.equals("muster")) return MUSTER;
		if (name.equals("eredin_commander")) return EREDIN_COMMANDER;
		if (name.equals("berserker")) return BERSERKER;
		if (name.equals("emhyr_invader")) return EMHYR_INVADER;
		if (name.equals("morale")) return MORALE;
		if (name.equals("decoy")) return DECOY;
		if (name.equals("francesca_daisy")) return FRANCESCA_DAISY;
		if (name.equals("hero morale")) return HERO_MORALE;
		if (name.equals("clear")) return CLEAR;
		if (name.equals("bond")) return BOND;
		if (name.equals("hero mardroeme")) return HERO_MARDROEME;
		if (name.equals("francesca_queen")) return FRANCESCA_QUEEN;
		if (name.equals("crach_an_craite")) return CRACH_AN_CRAITE;
		if (name.equals("hero medic")) return HERO_MEDIC;
		if (name.equals("rain fog")) return RAIN_FOG;
		return NON;
	}
}
