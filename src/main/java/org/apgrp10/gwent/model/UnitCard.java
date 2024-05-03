package org.apgrp10.gwent.model;

public class UnitCard extends Card
{
	private final UnitCardInfo info;
	private int score;
	
	public UnitCard(UnitCardInfo info)
	{
		this.info = info;
	}
}
