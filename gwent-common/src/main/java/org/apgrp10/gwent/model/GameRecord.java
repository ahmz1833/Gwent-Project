package org.apgrp10.gwent.model;

import java.util.List;

public record GameRecord(long player1ID,
                         long player2ID,
                         long seed,
                         String deck1,
                         String deck2,
                         List<Command> commands,
						 int gameWinner,
						 List<Integer> roundWinner,
						 List<Integer> p1Sc,
						 List<Integer> p2Sc) {}
