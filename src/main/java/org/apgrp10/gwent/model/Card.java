package org.apgrp10.gwent.model;

import org.apgrp10.gwent.controller.CardInfo;

public abstract class Card
{
	private final CardInfo info;
	
	Card(CardInfo info)
	{
		this.info = info;
	}
	
	protected CardInfo info()
	{
		return this.info;
	}
}
