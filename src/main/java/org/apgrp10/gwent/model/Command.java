package org.apgrp10.gwent.model;

import org.apgrp10.gwent.model.card.Card;

public interface Command {
	public static record PlayCard(int player, Card card, int row) implements Command {}
	public static record MoveToHand(int player, Card card) implements Command {}
	public static record Pass(int player) implements Command {}
}
