package org.apgrp10.gwent.model;

import java.io.Serializable;

import org.apgrp10.gwent.model.card.Card;

public interface Command {
	public static record PlayCard(int player, int cardId, int row) implements Command, Serializable {}
	public static record MoveToHand(int player, int cardId) implements Command, Serializable {}
	public static record Pass(int player) implements Command, Serializable {}
	public static record SetActiveCard(int player, int cardId) implements Command, Serializable {}
}
