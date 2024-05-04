package org.apgrp10.gwent.controller;

import org.apgrp10.gwent.model.PlayResult;

import java.util.ArrayList;

public class GameResultController
{
	private String fileName;
	private final ArrayList<PlayResult> allGames = new ArrayList<>();
	
	private GameResultController() {}
	
	public static GameResultController loadResults(String filename)
	{
		GameResultController rC = new GameResultController();
		rC.fileName = filename;
		// rC.allGames.add();
		return rC;
	}
	
	public void saveResults()
	{
	
	}
	
	public void putResult(PlayResult result)
	{
	
	}
	
	public ArrayList<PlayResult> getAllGameResults()
	{
		return allGames;
	}
}
