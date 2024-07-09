package org.apgrp10.gwent.view;

import java.util.List;
import java.util.function.Consumer;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.card.Card;

public interface GameMenuInterface {
	void redraw();
	void pickCard(List<Card> list, Consumer<Card> cb, boolean nullPossible);
	void cancelPossiblePick();

	Object addCardListener(Consumer<Object> cb);
	Object addButtonListener(Consumer<Object> cb);
	Object addRowListener(Consumer<Object> cb);
	Object addBgListener(Consumer<Object> cb);

	void removeListener(Object obj);
	void animationToRow(Card card, int actualRow);
	void animationToSpecial(Card card, int actualRow);
	void animationToHand(Card card);
	void animationToWeather(Card card);
	void animationToDeck(Card card, int player);
	void animationToUsed(Card card, int player);
	void animationSwap(Card c1, Card c2);
	void reactTo(Card card, int reactId);

	boolean isAnimationPlaying();

	void setScorchCards(List<Card> list);

	void setController(GameController controller);
	void start();

	void beginRound();
	void userPassed(int player);
	void showWinner(int player);
	void showDraw();
	void showMainWinner(int player);
	void showMainDraw();
	void userTurn(int player);
	void showConnection(int player, boolean connection);

	void endGame();
}
