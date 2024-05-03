package org.apgrp10.gwent.model;

import java.util.ArrayList;

public class CombatZone
{
	public static enum Type { SWORD, BOW, CANON, SIZE };
	ArrayList<UnitCard> normal;
	Card special;
}
