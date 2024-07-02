package org.apgrp10.gwent.model;

import java.util.List;

public record GameRecord(long id,
                         boolean isPublic,
                         long player1ID,
                         long player2ID,
                         long seed,
                         Deck deck1,
                         Deck deck2,
                         List<Command> commands,
                         int set1P1Sc, int set1P2Sc,
                         int set2P1Sc, int set2P2Sc,
                         int set3P1Sc, int set3P2Sc){}