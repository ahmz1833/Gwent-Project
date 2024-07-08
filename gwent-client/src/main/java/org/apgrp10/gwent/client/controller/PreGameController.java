package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.view.GameMenu;
import org.apgrp10.gwent.client.view.PreGameMenu;
import org.apgrp10.gwent.client.view.PreGameStage;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

public class PreGameController {

	private PreGameController(){}

//	public static void startGame(User opponent, Deck deck) {
//		PreGameMenu.getInstance().close();
//		GameController.startGame(opponent, deck);
//	}
}
