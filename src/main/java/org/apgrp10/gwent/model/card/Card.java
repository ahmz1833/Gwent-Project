package org.apgrp10.gwent.model.card;

public abstract class Card
{
	private final CardInfo info;
	
	protected Card(CardInfo info)
	{
		this.info = info;
	}
	
	protected CardInfo info()
	{
		return this.info;
	}
}
