package org.apgrp10.gwent.model;

public record UserExperience(long userId,
                             int maxScore,
                             int wins,
                             int losses,
                             int draws,
                             int rankByWins,
                             int rankByMaxScore){
	public UserExperience withRankByWins(int i) {
		return new UserExperience(userId, maxScore, wins, losses, draws, i, rankByMaxScore);
	}

	public UserExperience withRankByMaxScore(int i) {
		return new UserExperience(userId, maxScore, wins, losses, draws, rankByWins, i);
	}
}