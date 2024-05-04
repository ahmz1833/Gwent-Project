package org.apgrp10.gwent.model;

import org.apgrp10.gwent.controller.GameController;

public class UnitCard extends Card
{
	private int power;
	
	public UnitCard(UnitCardInfo info)
	{
		super(info);
	}
	
	private UnitCardInfo minfo()
	{
		return (UnitCardInfo) info();
	}
	
	public int getPower(GameController gameC)
	{
		//TODO: MOZAL ASLI
		return minfo().power;
	}
	
	public void setPower(int power)
	{
		this.power = power;
	}
}
