package org.apgrp10.gwent.model;

public record UserExperience(long userId,
                             int maxScore,
                             int wins,
                             int losses,
                             int draws){}