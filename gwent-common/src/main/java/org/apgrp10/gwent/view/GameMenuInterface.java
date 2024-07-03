package org.apgrp10.gwent.view;

import java.util.List;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.utils.Callback;

public interface GameMenuInterface {
	void redraw();
	void pickCard(List<Card> list, Callback<Card> cb, boolean nullPossible);

	Object addCardListener(Callback<Object> cb);
	Object addButtonListener(Callback<Object> cb);
	Object addRowListener(Callback<Object> cb);
	Object addBgListener(Callback<Object> cb);

	void removeListener(Object obj);
	void animationToRow(Card card, int actualRow);
	void animationToSpecial(Card card, int actualRow);
	void animationToHand(Card card);
	void animationToWeather(Card card);
	void animationToDeck(Card card, int player);
	void animationToUsed(Card card, int player);
	void animationSwap(Card c1, Card c2);

	boolean isAnimationPlaying();

	void setScorchCards(List<Card> list);

	void setController(GameController controller);
	void start();
}
