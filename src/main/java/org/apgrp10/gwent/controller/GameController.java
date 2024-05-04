package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.PlayResult;
import org.apgrp10.gwent.model.Player;

public class GameController
{
	private Player player1, player2;
	private int currentTurn; // currentTurn%2 == 0 -> Player1 / currentTurn%2 == 1 -> Player2
	private int currentRound;
	public PlayResult[] roundsResult  = new PlayResult[3];
	
	private GameController(Player player1, Player player2)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.currentTurn = 0;
	}
	
	public static GameController makeNewGame(Player player1, Player player2)
	{
		return new GameController(player1, player2);
	}
	
	
	public PlayResult endGame()
	{
		return null;
	}
	
	public void endTurn()
	{
	
	}
	
	public void endRound()
	{
	
	}
}
