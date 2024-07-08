package org.apgrp10.gwent.model;

public record Experience(long userId,
                         GameRecord bestPlaying,
                         int wins,
                         int losses,
                         int draws){}