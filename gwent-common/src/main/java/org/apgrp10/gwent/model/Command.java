package org.apgrp10.gwent.model;

import java.io.Serializable;

public interface Command {
	public static record VetoCard(int player, int cardId) implements Command, Serializable {}
	public static record PlayCard(int player, int cardId, int row) implements Command, Serializable {}
	public static record SwapCard(int player, int cardId1, int cardId2) implements Command, Serializable {}
	public static record PlayLeader(int player) implements Command, Serializable {}
	public static record MoveToHand(int player, int cardId) implements Command, Serializable {}
	public static record Pass(int player) implements Command, Serializable {}
	public static record SetActiveCard(int player, int cardId) implements Command, Serializable {}
	public static record PickResponse(int player, int cardId, String what) implements Command, Serializable {}
	public static record Sync() implements Command, Serializable {}
}
