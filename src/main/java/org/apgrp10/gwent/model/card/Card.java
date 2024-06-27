package org.apgrp10.gwent.model.card;

public class Card {
	public final String name, pathAddress;
	public final int strength;
	public final boolean isHero;
	public final Row row;
	public final Faction faction;
	public final Ability ability;
	private int score;
	private int gameId; // used in commands do they can be easily serialized
	public Card(String name, String pathAddress, int strength, Row row, Faction faction, Ability ability, boolean isHero) {
		this.name = name;
		this.pathAddress = pathAddress;
		this.isHero = isHero;
		this.strength = strength;
		this.row = row;
		this.faction = faction;
		this.ability = ability;

		this.score = strength;
	}
	public int getScore() { return score; }
	public void setScore(int score) { this.score = score; }
	public void resetScore() { this.score = strength; }
	public int getGameId() { return gameId; }
	public void setGameId(int id) { gameId = id; }

	public Card decoyVersion(int gameId) {
		Card ans = new Card(name, "decoy", strength, row, faction, ability, isHero);
		ans.score = this.score;
		ans.gameId = gameId;
		return ans;
	}
}
