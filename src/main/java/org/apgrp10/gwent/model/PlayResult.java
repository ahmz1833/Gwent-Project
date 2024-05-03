package org.apgrp10.gwent.model;

import java.util.Date;

public record PlayResult(User player1, User player2, int player1Score, int player2Score, Date date)
{
	public boolean isParticipant(User u)
	{
		return player1.equals(u) || player2.equals(u);
	}
	
	public boolean isWinner(User u)
	{
		return false;
	}
	
	public boolean isLoser(User u)
	{
		return false;
	}
	
	public boolean isDraw()
	{
		return false;
	}
	
	public int scoreOf(User u)
	{
		return 0;
	}
}
