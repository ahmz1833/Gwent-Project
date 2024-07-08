package org.apgrp10.gwent.client.view;

import java.util.ArrayList;
import java.util.List;

import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.MouseInputController;
import org.apgrp10.gwent.client.controller.ReplayInputController;
import org.apgrp10.gwent.client.controller.ServerInputController;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import javafx.stage.WindowEvent;

public class GameStage extends AbstractStage {
	private static GameStage INSTANCE;

	private GameStage() {
		super("Gwent Game", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of GameStage");
	}

	public static GameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new GameStage();
		return INSTANCE;
	}

	private enum GameMode { NONE, LOCAL, ONLINE, CONTINUE, LIVE, REPLAY }
	private static GameMode mode = GameMode.NONE;
	private static User.PublicInfo user1, user2;
	private static Deck deck1, deck2;
	private static long seed;
	private static int player;
	private static final List<Command> cmds = new ArrayList<>();

	public static void setCommonData(User.PublicInfo u1, User.PublicInfo u2, Deck d1, Deck d2, long seed) {
		user1 = u1;
		user2 = u2;
		deck1 = d1;
		deck2 = d2;
		GameStage.seed = seed;
	}

	public static void setLocal() {
		mode = GameMode.LOCAL;
	}

	public static void setOnline(int localPlayer) {
		player = localPlayer;
		mode = GameMode.ONLINE;
	}

	public static void setContinue(int localPlayer, List<Command> pastCommands) {
		player = localPlayer;
		cmds.addAll(pastCommands);
		mode = GameMode.CONTINUE;
	}

	public static void setLive(int pov, List<Command> pastCommands) {
		player = pov;
		cmds.addAll(pastCommands);
		mode = GameMode.LIVE;
	}

	public static void setReplay(int pov, List<Command> commands) {
		player = pov;
		cmds.addAll(commands);
		mode = GameMode.REPLAY;
	}

	private void setupServer(GameController gc) {
		InputController c1 = gc.getPlayer(0).controller;
		InputController c2 = gc.getPlayer(0).controller;
		boolean isOnline1 = c1 instanceof ServerInputController;
		boolean isOnline2 = c2 instanceof ServerInputController;
		Server.setListener("command", req -> {
			Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
			int player = req.getBody().get("player").getAsInt();
			if (player == 0 && isOnline1) ((ServerInputController) c1).sendCommand(cmd);
			if (player == 1 && isOnline2) ((ServerInputController) c2).sendCommand(cmd);
			return req.response(Response.OK_NO_CONTENT);
		});
		gc.addCommandListener(cmd -> {
			if (cmd.player() == 0 && !isOnline1 || cmd.player() == 1 && !isOnline2)
				Server.send(new Request("command", MGson.makeJsonObject("cmd", cmd.toBase64(), "player", cmd.player())));
		});
	}

	private void stopServer() {
		Server.setListener("command", null);
	}

	private void createLocal() {
		InputController c1 = new MouseInputController();
		InputController c2 = new MouseInputController();
		GameMenu gm = new GameMenu(this, true);
		// TODO: better way to save recording
		new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> {
			Utils.choosePlaceAndDownload("Choose place to save recording", "recording.gwent", this,
					MGson.get(true, true).toJson(gr));
			this.close();
		}, 0, false);
	}

	private void createOnline() {
		InputController c1 = player == 0? new MouseInputController(): new ServerInputController();
		InputController c2 = player == 1? new MouseInputController(): new ServerInputController();
		GameMenu gm = new GameMenu(this, true);
		GameController gc = new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> this.close(), player, false);
		setupServer(gc);
	}

	private void createContinue() {
		InputController c1 = player == 0? new MouseInputController(): new ServerInputController();
		InputController c2 = player == 1? new MouseInputController(): new ServerInputController();
		GameMenu gm = new GameMenu(this, true);
		GameController gc = new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> this.close(), player, false);
		gc.fastForward(cmds);
		setupServer(gc);
	}

	private void createLive() {
		InputController c1 = new ServerInputController();
		InputController c2 = new ServerInputController();
		GameMenu gm = new GameMenu(this, true);
		GameController gc = new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> this.close(), player, true);
		gc.fastForward(cmds);
		setupServer(gc);
	}

	private void createReplay() {
		InputController c1 = new ReplayInputController(cmds);
		InputController c2 = new ReplayInputController(cmds);
		GameMenu gm = new GameMenu(this, false);
		new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> this.close(), player, true);
	}

	@Override
	protected boolean onCreate() {
		switch (GameStage.mode) {
			case NONE -> {
				ANSI.logError(System.err, "You must set a game mode first");
				return false;
			}
			case LOCAL -> createLocal();
			case ONLINE -> createOnline();
			case CONTINUE -> createContinue();
			case LIVE -> createLive();
			case REPLAY -> createReplay();
		}
		return true;
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
		event.consume();
		if(showExitDialog()) stopServer();
	}
}
