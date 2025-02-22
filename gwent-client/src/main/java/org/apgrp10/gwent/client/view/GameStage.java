package org.apgrp10.gwent.client.view;

import java.util.*;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.apgrp10.gwent.client.Gwent;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.Server;
import org.apgrp10.gwent.client.controller.MouseInputController;
import org.apgrp10.gwent.client.controller.ReplayInputController;
import org.apgrp10.gwent.client.controller.UserController;
import org.apgrp10.gwent.controller.DummyInputController;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.Deck;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.model.net.Request;
import org.apgrp10.gwent.model.net.Response;
import org.apgrp10.gwent.utils.ANSI;
import org.apgrp10.gwent.utils.MGson;
import org.apgrp10.gwent.utils.Utils;

import javafx.stage.WindowEvent;
import org.apgrp10.gwent.utils.WaitExec;

public class GameStage extends AbstractStage {
	private static List<Command> cmds = new ArrayList<>();
	private static GameStage INSTANCE;
	private static GameMode mode = GameMode.NONE;
	private static User.PublicInfo user1, user2;
	private static Deck deck1, deck2;
	private static long seed;
	private static int player;

	private GameStage() {
		super("Gwent Game", R.icon.app_icon);
		if (INSTANCE != null) throw new RuntimeException("Duplicate Instance of GameStage");
	}

	public static GameStage getInstance() {
		if (INSTANCE == null) INSTANCE = new GameStage();
		return INSTANCE;
	}

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
		cmds = null;
		mode = GameMode.ONLINE;
	}

	public static void setContinue(int localPlayer, List<Command> pastCommands) {
		player = localPlayer;
		cmds = new ArrayList<>(pastCommands);
		mode = GameMode.CONTINUE;
	}

	public static void setLive(int pov, List<Command> pastCommands) {
		player = pov;
		cmds = new ArrayList<>(pastCommands);
		mode = GameMode.LIVE;
	}

	public static void setReplay(int pov, List<Command> commands) {
		player = pov;
		cmds = new ArrayList<>(commands);
		mode = GameMode.REPLAY;
	}

	private void setupServer(GameController gc) {
		InputController c1 = gc.getPlayer(0).controller;
		InputController c2 = gc.getPlayer(1).controller;
		boolean isOnline1 = c1 instanceof DummyInputController;
		boolean isOnline2 = c2 instanceof DummyInputController;
		Server.setListener("command", req -> {
			Command cmd = Command.fromBase64(req.getBody().get("cmd").getAsString());
			int player = req.getBody().get("player").getAsInt();
			if (player == 0 && isOnline1) gc.sendCommand(cmd);
			if (player == 1 && isOnline2) gc.sendCommand(cmd);
			return req.response(Response.OK_NO_CONTENT);
		});
		gc.addCommandListener(cmd -> {
			if (cmd.player() == 0 && !isOnline1 || cmd.player() == 1 && !isOnline2)
				Server.send(new Request("command", MGson.makeJsonObject("cmd", cmd.toBase64(), "player", cmd.player())));
		});
	}

	private void exit() {
		Server.setListener("command", null);
		if(mode == GameMode.LIVE || mode == GameMode.REPLAY || mode == GameMode.LOCAL){
			Platform.runLater(()->{
				Gwent.forEachStage(Stage::close);
				MainStage.getInstance().start();
			});
			return;
		}
		Gwent.exit();
	}

	private void createLocal() {
		InputController c1 = new MouseInputController();
		InputController c2 = new MouseInputController();
		GameMenu gm = new GameMenu(this, false);
		new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr -> {
			showWinnerDialog(gr, true);
			this.exit();
		}, 0, false, null, false);
	}

	private void createOnline() {
		InputController c1 = player == 0 ? new MouseInputController() : new DummyInputController();
		InputController c2 = player == 1 ? new MouseInputController() : new DummyInputController();
		GameMenu gm = new GameMenu(this, true);
		GameController gc = new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr ->
				new WaitExec(false).run(3000, () -> {
					showWinnerDialog(gr, false);
					exit();
				}), player, false, cmds, false);
		setupServer(gc);
	}

	private void createLive() {
		InputController c1 = new DummyInputController();
		InputController c2 = new DummyInputController();
		GameMenu gm = new GameMenu(this, true);
		GameController gc = new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr ->
				new WaitExec(false).run(3000, this::exit), player, true, cmds, false);
		setupServer(gc);
	}

	private void createReplay() {
		InputController c1 = new ReplayInputController(cmds);
		InputController c2 = new ReplayInputController(cmds);
		GameMenu gm = new GameMenu(this, false);
		new GameController(c1, c2, user1, user2, deck1, deck2, seed, gm, gr ->
				new WaitExec(false).run(3000, this::exit), player, true, null, false);
	}

	@Override
	protected boolean onCreate() {
		switch (GameStage.mode) {
			case NONE -> {
				ANSI.logError(System.err, "You must set a game mode first");
				return false;
			}
			case LOCAL -> createLocal();
			case ONLINE, CONTINUE -> createOnline();
			case LIVE -> createLive();
			case REPLAY -> createReplay();
		}
		Gwent.forEachStage(Stage::close);
		return true;
	}

	@Override
	public void connectionLost() {
		if(isShowing()) {
			showAlert(Dialogs.ERROR(), "Connection Lost", "Your Connection to server lost. Exiting the Game...");
			new WaitExec(false).run(1000, this::exit);
		}
	}

	@Override
	protected void onCloseRequest(WindowEvent event) {
		event.consume();
		if (showExitDialog()) exit();
	}

	private void showWinnerDialog(GameRecord gr, boolean hasSave){
		String title;
		if(gr.gameWinner() == -1)
			title = "Draw!";
		else if((gr.gameWinner() == 0 && UserController.getCurrentUser().id() == gr.player1ID()) ||
		        (gr.gameWinner() == 1 && UserController.getCurrentUser().id() == gr.player2ID()))
			title = "You won!";
		else
			title = "You lose!";
		StringBuilder content = new StringBuilder();
		for(int i = 0; i < gr.p1Sc().size(); i++)
			content.append("Round ").append(i).append(" : ").append(gr.p1Sc().get(i)).append(" - ").append(gr.p2Sc().get(i)).append("\n");
		if(!hasSave)
			showDialogAndWait(Dialogs.INFO(), title, content.toString(), Map.entry("#OK", k -> {}));
		else
			showDialogAndWait(Dialogs.INFO(), title, content.toString(), Map.entry("#OK", k -> {}), Map.entry("save game", k ->
					Utils.choosePlaceAndDownload("Choose place to save recording", "recording.gwent", this,
					MGson.get(true, true).toJson(gr))));
	}

	private enum GameMode {NONE, LOCAL, ONLINE, CONTINUE, LIVE, REPLAY}
}
