package org.apgrp10.gwent.model;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.card.UnitCard;

public interface Action
{
	void perform(GameController gameC, UnitCard thisCard);
}
