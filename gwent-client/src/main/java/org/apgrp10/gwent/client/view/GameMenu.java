package org.apgrp10.gwent.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.Event;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.HitInfo;
import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.model.AvatarView;
import org.apgrp10.gwent.client.model.CardView;
import org.apgrp10.gwent.client.model.TerminalAsyncReader;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.GameController.PlayerData;
import org.apgrp10.gwent.model.Avatar;
import org.apgrp10.gwent.model.GameRecord;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.utils.WaitExec;
import org.apgrp10.gwent.view.GameMenuInterface;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameMenu implements GameMenuInterface {
	private GameController controller;
	private Pane realRoot = new Pane();
	private Pane rootPane = new Pane();
	private Pane buttonPane = new Pane();
	private Pane messagePane = new Pane();
	private Pane overlayRedrawingPane = new Pane();
	private Pane overlayNonredrawingPane = new Pane();
	private Stage stage;
	private boolean hasChat;

	public static GameMenu currentMenu;
	private static final int WIDTH = 1280, HEIGHT = 720;
	private WaitExec waitExec = new WaitExec(false);

	protected static class Position {
		public record RectPos(double posX, double posY, double posX2, double posY2) {
			public double x() { return posX * WIDTH; }
			public double y() { return posY * HEIGHT; }
			public double w() { return (posX2 - posX) * WIDTH; }
			public double h() { return (posY2 - posY) * HEIGHT; }
			public double centerX() { return x() + w() / 2; }
			public double centerY() { return y() + h() / 2; }

			public void setBounds(Region region) {
				region.setLayoutX(x());
				region.setLayoutY(y());
				region.setMinSize(w(), h());
				region.setMaxSize(w(), h());
			}
			public void setBounds(ImageView region) {
				region.setLayoutX(x());
				region.setLayoutY(y());
				region.setFitWidth(w());
				region.setFitHeight(h());
			}

			public static RectPos bySize(double x, double y, double w, double h) {
				return new RectPos(x, y, x + w, y + h);
			}
		}

		public static final RectPos hand = new RectPos(0.3010, 0.7796, 0.7875, 0.8953);
		public static final RectPos card = new RectPos(0, 0, 0.0398, 0.1018);
		public static final RectPos info = new RectPos(0.7955, 0.2072, 0.9608, 0.7627);
		public static final RectPos row[] = {
				new RectPos(0.3697, 0.0212, 0.7906, 0.1250),
				new RectPos(0.3697, 0.1388, 0.7906, 0.2444),
				new RectPos(0.3697, 0.2666, 0.7906, 0.3722),
				new RectPos(0.3697, 0.4074, 0.7906, 0.5111),
				new RectPos(0.3697, 0.5296, 0.7906, 0.6342),
				new RectPos(0.3697, 0.6555, 0.7906, 0.7611),
		};
		public static final RectPos profBack[] = {
				new RectPos(0, 0.225, 0.235, 0.3708),
				new RectPos(0.0, 0.586, 0.233, 0.754166)
		};
		public static final RectPos profName[] = {
				new RectPos(0.0796875, 0.251388, 0.1625, 0.3375),
				new RectPos(0.0796875, 0.622, 0.1625, 0.70875)
		};
		public static final RectPos profAvatar[] = {
				new RectPos(0.016406, 0.25138, 0.064062, 0.3527),
				new RectPos(0.016406, 0.62138, 0.064062, 0.7227)
		};
		public static final RectPos profAvatarBorder[] = {
				new RectPos(0.016406, 0.24938, 0.065062, 0.3457),
				new RectPos(0.016406, 0.6178, 0.065062, 0.7151),
		};
		public static final RectPos special[] = {
				new RectPos(0.2963, 0.0212, 0.3640, 0.1250),
				new RectPos(0.2963, 0.1388, 0.3640, 0.2444),
				new RectPos(0.2963, 0.2666, 0.3640, 0.3722),
				new RectPos(0.2963, 0.4074, 0.3640, 0.5111),
				new RectPos(0.2963, 0.5296, 0.3640, 0.6342),
				new RectPos(0.2963, 0.6555, 0.3640, 0.7611),
		};
		public static final RectPos weather = new RectPos(0.0729, 0.4157, 0.2182, 0.5398);
		public static final RectPos rowScore[] = {
				RectPos.bySize(0.2651, 0.0425, 0.0270, 0.0500),
				RectPos.bySize(0.2651, 0.1638, 0.0270, 0.0500),
				RectPos.bySize(0.2651, 0.2925, 0.0270, 0.0500),
				RectPos.bySize(0.2651, 0.4296, 0.0270, 0.0500),
				RectPos.bySize(0.2651, 0.5527, 0.0270, 0.0500),
				RectPos.bySize(0.2651, 0.6805, 0.0270, 0.0500),
		};
		public static final RectPos totalScore[] = {
				RectPos.bySize(0.2218, 0.2824, 0.0270, 0.0500),
				RectPos.bySize(0.2218, 0.6546, 0.0270, 0.0500),
		};
		public static final RectPos pass = new RectPos(0.1625, 0.8203, 0.2244, 0.8490);
		public static final RectPos screen = new RectPos(0, 0, 1, 1);
		public static final RectPos deck[] = {
				new RectPos(0.8984, 0.0648, 0.9552, 0.1990),
				new RectPos(0.8984, 0.7657, 0.9552, 0.9009),
		};
		public static final RectPos used[] = {
				new RectPos(0.8041, 0.0648, 0.8619, 0.1990),
				new RectPos(0.8041, 0.7657, 0.8619, 0.9009),
		};
		public static final RectPos hp[] = {
				new RectPos(0.125, 0.300277, 0.2140, 0.34611),
				new RectPos(0.127, 0.68472, 0.2156, 0.74445)
		};
		public static final RectPos leader[] = {
				new RectPos(0.0713, 0.0740, 0.1244, 0.1990),
				new RectPos(0.0713, 0.7712, 0.1244, 0.8962),
		};
		public static final RectPos switchBtn = new RectPos(0.9734, 0.0166, 0.9968, 0.1583);
		public static final RectPos react[] = {
				RectPos.bySize(0.0117, 0.0930, 0.0480, 0.0882),
				RectPos.bySize(0.0117, 0.7916, 0.0480, 0.0882),
		};
		public static final RectPos deckCards[] = {
				RectPos.bySize(0.9062, 0.0833, 0.0578, 0.1291),
				RectPos.bySize(0.9062, 0.7819, 0.0578, 0.1291),
		};
		public static final RectPos deckCardsText =
				RectPos.bySize(0.0078, 0.0736, 0.0234, 0.0277)
		;
		public static final RectPos deaths[] = {
				RectPos.bySize(0.8125, 0.0833, 0.0578, 0.1291),
				RectPos.bySize(0.8125, 0.7819, 0.0578, 0.1291),
		};
		public static final RectPos winnerSymbol[] = {
				RectPos.bySize(0.2125, 0.27916, 0.04687, 0.06944),
				RectPos.bySize(0.2125, 0.6527, 0.04687, 0.06944),
		};
	}

	public GameMenu(Stage stage, boolean hasChat) {
		this.stage = stage;
		this.hasChat = hasChat;
	}

	public void setController(GameController controller) {
		this.controller = controller;
	}

	public void start() {
		start(stage);
	}

	private void start(Stage stage) {
		MessageStage.deleteInstance();
		this.stage = stage;
		realRoot.getChildren().add(rootPane);
		realRoot.getChildren().add(buttonPane);
		realRoot.getChildren().add(messagePane);
		realRoot.getChildren().add(overlayRedrawingPane);
		realRoot.getChildren().add(overlayNonredrawingPane);
		buttonPane.setPickOnBounds(false);
		messagePane.setPickOnBounds(false);
		overlayRedrawingPane.setPickOnBounds(false);
		overlayNonredrawingPane.setPickOnBounds(false);
		Scene scene = new Scene(realRoot);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		addTerminalListener();
		addNonredrawingButtons();
		redraw();
	}

	public void endGame() {
		removeTerminalListener();
	}

	private Object terminalListener;
	private void addTerminalListener() {
		terminalListener = TerminalAsyncReader.addListener(str -> {
			if (str.equals("¯\\_(ツ)_/¯") && !showCheats) {
				showCheats = true;
				redraw();
			}
			if (str.equals("hide") && showCheats) {
				showCheats = false;
				redraw();
			}
		});
	}
	private void removeTerminalListener() {
		TerminalAsyncReader.removeListener(terminalListener);
	}

	private final Map<Card, CardView> cardMap = new HashMap<>();

	private void addText(String str, Position.RectPos pos) {
		// TODO: better way to adjust text position and size
		Text text = new Text(str);
		text.setFont(Font.font(24));
		text.setLayoutX(pos.x() + pos.w() * (0.5 - 0.2 * str.length()));
		text.setLayoutY(pos.y() + pos.h() * 0.75);
		rootPane.getChildren().add(text);
	}

	private void addTraditionalButton(Pane parent, String str, String cmd, Position.RectPos pos) {
		Button btn = new Button(str);
		btn.setOnMouseClicked(e -> notifyListeners(buttonListeners, cmd));
		if (pos != null)
			pos.setBounds(btn);
		parent.getChildren().add(btn);
	}

	private void addButton(Pane parent, String str, String cmd, Position.RectPos pos) {
		MFXButton btn = new MFXButton(str);
		btn.setOnMouseClicked(e -> notifyListeners(buttonListeners, cmd));
		if (pos != null)
			pos.setBounds(btn);
		btn.setStyle("-fx-font-family: 'Comfortaa SemiBold'; -fx-background-color: rgba(245,222,196,0.54)");
		parent.getChildren().add(btn);
	}

	private void addImage(Image image, Position.RectPos pos) {
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(pos.w());
		imageView.setFitHeight(pos.h());
		imageView.setX(pos.x());
		imageView.setY(pos.y());
		rootPane.getChildren().add(imageView);
	}
	private void addAvatar(Avatar avatar, Position.RectPos pos){
		AvatarView view = new AvatarView(avatar);
		view.setLayoutX(pos.x());
		view.setLayoutY(pos.y());
		view.setPrefWidth(pos.w());
		rootPane.getChildren().add(view);
	}

	private void addSelectionRect(Position.RectPos pos, int code) {
		Rectangle rect = new Rectangle(pos.x(), pos.y(), pos.w(), pos.h());
		rect.setOnMouseClicked(e -> notifyListeners(rowListeners, code));
		rect.setFill(Color.color(0.5, 0.5, 0, 0.3));
		rootPane.getChildren().add(rect);
	}

	private void setInfoOnClicked(Node node, List<Card> list, boolean send) {
		node.setOnMouseClicked(e -> pickCard(list, obj -> {
			if (send && obj != null)
				notifyListeners(cardListeners, obj);
		}, true));
	}

	private CardView activeCardView;

	private void addCardHBox(Position.RectPos pos, List<Card> cards, boolean calcStrength, boolean cardClickable) {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);
		pos.setBounds(hbox);
		for (Card card : cards) {
			CardView cardView = calcStrength
					? CardView.newInBoard(card.pathAddress, controller.calcCardScore(card), Position.card.w(), Position.card.h())
					: CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
			hbox.getChildren().add(cardView);
			if (cardClickable) {
				cardView.setOnMouseClicked(e -> {
					e.consume();
					notifyListeners(cardListeners, card);
				});
			}
			if (controller.getActiveCard() == card)
				activeCardView = cardView;
			cardMap.put(card, cardView);
		}
		rootPane.getChildren().add(hbox);
	}

	private void addBackground(Image background) {
		ImageView view = new ImageView(background);
		view.setFitWidth(WIDTH);
		view.setFitHeight(HEIGHT);
		view.setOnMouseClicked(e -> notifyListeners(bgListeners, null));
		rootPane.getChildren().add(view);
	}

	// this shit is for keeping the scroller position
	private Text pickSelectedText;

	protected void changePicIdx(int value) {
		pickIdx = value;
	}

	private void addPicker() {
		new FivePlaceGame(this, pickNullPossible, pickList, overlayRedrawingPane, pickIdx);
	}

	public void addWeatherOverlay(Position.RectPos pos, Image image) {
		Rectangle rect = new Rectangle(pos.x(), pos.y(), pos.w(), pos.h());
		rect.setMouseTransparent(true);
		rect.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
		rootPane.getChildren().add(rect);
	}

	private void addCheatButtons() {
		HBox cheats = new HBox();
		addTraditionalButton(cheats, "Cheat: take from deck", "cheat_0", null);
		addTraditionalButton(cheats, "Cheat: add hp", "cheat_1", null);
		addTraditionalButton(cheats, "Cheat: restore leader", "cheat_2", null);
		addTraditionalButton(cheats, "Cheat: clear weather", "cheat_3", null);
		addTraditionalButton(cheats, "Cheat: restore graveyard", "cheat_4", null);
		addTraditionalButton(cheats, "Cheat: show opponent's hand", "cheat_5", null);
		addTraditionalButton(cheats, "Cheat: horn", "cheat_6", null);
		addTraditionalButton(cheats, "Cheat: new card", "cheat_7", null);
		for (Node node : cheats.getChildren())
			node.setStyle("-fx-font-size:8");
		rootPane.getChildren().add(cheats);
	}

	private void addDeaths(int side) {
		StackPane deaths = new StackPane();
		Position.deaths[side].setBounds(deaths);
		deaths.setAlignment(Pos.CENTER);
		for (Card card : controller.getPlayer(side == controller.getActivePlayer()? 1: 0).usedCards) {
			CardView view = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
			deaths.getChildren().add(view);
			cardMap.put(card, view);
		}
		rootPane.getChildren().add(deaths);
	}

	private void addWinnerSign() {
		if (controller.calcPlayerScore(0) == controller.calcPlayerScore(1)) return;
		ImageView image = new ImageView(R.getImage("icons/icon_high_score.png"));
		Position.winnerSymbol[controller.calcPlayerScore(controller.getActivePlayer()) > controller.calcPlayerScore(1 - controller.getActivePlayer())? 1 : 0].setBounds(image);
		rootPane.getChildren().add(image);
	}

	private void addDeckCards(boolean up) {
		Pane deckCards = new Pane();
		Position.deckCards[up? 0 : 1].setBounds(deckCards);
		PlayerData playerData = controller.getPlayer(up ? 1 - controller.getActivePlayer() : controller.getActivePlayer());
		ImageView image = new ImageView(R.getImage("icons/" + switch (playerData
				.deck.getFaction()) {
			case REALMS -> "deck_back_realms";
			case MONSTERS -> "deck_back_monsters";
			case NILFGAARD -> "deck_back_nilfgaard";
			case SCOIATAEL -> "deck_back_scoiatael";
			case SKELLIGE -> "deck_back_skellige";
			default -> "";
		} + ".jpg"));
		image.setFitWidth(Position.card.w());
		image.setFitHeight(Position.card.h());
		deckCards.getChildren().add(image);
		StackPane textContainer = new StackPane();
		textContainer.setAlignment(Pos.CENTER);
		Text text = new Text(String.valueOf(playerData.deck.getDeck().size()));
		text.setStyle("-fx-font-family: 'Comfortaa SemiBold'");
		text.setStyle("-fx-font-size: 12px");
		text.setFill(Color.GOLD);
		textContainer.getChildren().add(text);
		textContainer.setBackground(Background.fill(Color.GRAY));
		Position.deckCardsText.setBounds(textContainer);
		deckCards.getChildren().add(textContainer);
		rootPane.getChildren().add(deckCards);
		if (up) {
			addDeckCards(false);
		}
	}

	private final ArrayList<MessageGame> messages = new ArrayList<>();
	private boolean isMessageShowing = false;
	private boolean messageAnimation = false;

	private void showAllMessages() {
		if (isMessageShowing == false)
			return;
		if (!animationNodes.isEmpty() || !scorchCards.isEmpty()) {
			Platform.runLater(this::showAllMessages);
			return;
		}

		int delay = 510;
		messageAnimation = true;
		for (MessageGame message : messages) {
			message.show(delay);
			delay += 1050;
		}
		controller.waitExec.run(delay, () -> messageAnimation = false);
		messages.clear();
		isMessageShowing = false;
	}

	private void tryShowAllMessages() {
		if (!isMessageShowing) {
			isMessageShowing = true;
			Platform.runLater(() -> showAllMessages());
		}
	}

	public void beginRound() {
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_round_start.png"), "NEW Round Started"));
		tryShowAllMessages();
	}

	public void userPassed(int player) {
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_round_passed.png"),
				controller.getPlayer(player).user.nickname() + " passed"));
		tryShowAllMessages();
	}

	public void showWinner(int player) {
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_win_round.png"),
				controller.getPlayer(player).user.nickname() + " won"));
		tryShowAllMessages();
	}
	public void showDraw() {
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_draw_round.png"), "	 Draw!"));
		tryShowAllMessages();
	}
	public void showMainWinner(int player) {
		messages.add(new MessageGame(messagePane, R.getImage("icons/end_win.png"), "	 " + controller.getPlayer(player).user.nickname() + " won the Game"));
		tryShowAllMessages();
	}
	public void showMainDraw() {
		messages.add(new MessageGame(messagePane, R.getImage("icons/end_draw.png"), "Draw!"));
		tryShowAllMessages();
	}

	public void userTurn(int player) {
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_me_turn.png"),
				controller.getPlayer(1 - player).user.nickname() + "'s turn"));
		tryShowAllMessages();
	}

	public void showConnection(int player, boolean connection) {
		// TODO: add proper icon
		messages.add(new MessageGame(messagePane, R.getImage("icons/notif_round_passed.png"),
				controller.getPlayer(player).user.nickname() + (connection? " reconnected": " disconnected")));
		tryShowAllMessages();
	}

	private void addNameProfile(boolean up) {
		int playerIdx = up ? 1 - controller.getActivePlayer() : controller.getActivePlayer();
		PlayerData player = controller.getPlayer(playerIdx);
		addImage(R.getImage("icons/halfBlack.png"), Position.profBack[up ? 0 : 1]);
		String path =
				switch (player.deck.getFaction()) {
					case REALMS -> "deck_shield_realms";
					case NILFGAARD -> "deck_shield_nilfgaard";
					case MONSTERS -> "deck_shield_monsters";
					case SCOIATAEL -> "deck_shield_scoiatael";
					case SKELLIGE -> "deck_shield_skellige";
					default -> "";
				};
		ImageView image = new ImageView(R.getImage("icons/" + path + ".png"));
		image.setFitWidth(50);
		image.setFitHeight(50);
		VBox vBox = new VBox();
		vBox.setLayoutX(Position.profName[up ? 0 : 1].x());
		vBox.setLayoutY(Position.profName[up ? 0 : 1].y());
		vBox.setSpacing(4);
		Text nickName = new Text(player.user.nickname());
		nickName.setStyle("-fx-font-family: 'Yrsa SemiBold'");
		nickName.setStyle("-fx-font-size: 16px");
		nickName.setFill(Color.GOLD);
		vBox.getChildren().add(nickName);
		Text faction = new Text(path.substring(path.lastIndexOf('_') + 1));
		faction.setStyle("-fx-font-family: 'Yrsa SemiBold'");
		faction.setStyle("-fx-font-size: 14px");
		faction.setFill(Color.GREY);
		vBox.getChildren().add(faction);
		ImageView cards = new ImageView(R.getImage("icons/icon_card_count.png"));
		cards.setFitHeight(20);
		cards.setFitWidth(20);
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.getChildren().add(cards);
		Text cardsCounts = new Text(String.valueOf(player.handCards.size()));
		cardsCounts.setStyle("-fx-font-size: 20px; -fx-font-family: 'Yrsa SemiBold'");
		cardsCounts.setFill(Color.GOLD);
		hbox.getChildren().add(cardsCounts);
		vBox.getChildren().add(hbox);
		rootPane.getChildren().add(vBox);
	}

	private void addProfile(boolean up) {
		addNameProfile(up);
		addAvatar(controller.getPlayer(up? controller.getActivePlayer() : 1 - controller.getActivePlayer()).user.avatar(),
				Position.profAvatar[up ? 0 : 1]);
		addImage(R.getImage("icons/icon_player_border.png"), Position.profAvatarBorder[up ? 0 : 1]);

		if (up) {
			addProfile(false);
		}
	}

	private boolean showCheats;

	private void addNonredrawingButtons() {
		addButton(buttonPane, "Pass", "pass", Position.pass);
		addButton(buttonPane, "React", "react_0", new Position.RectPos(0.1625, 0.0958, 0.2244, 0.1900));
		if (controller.hasSwitchableSides()) {
			MFXButton btn = new MFXButton("S\ni\nd\ne");
			btn.setStyle("-fx-font-family: 'Comfortaa SemiBold'; -fx-background-color: rgba(245,222,196,0.54)");
			btn.setOnMouseClicked(e -> {
				controller.setActivePlayer(1 - controller.getActivePlayer());
				redraw();
			});
			Position.switchBtn.setBounds(btn);
			overlayNonredrawingPane.getChildren().add(btn);
		}
		if (hasChat) {
			MFXButton b = new MFXButton("C\nh\na\nt");
			b.setStyle("-fx-font-family: 'Comfortaa SemiBold'; -fx-background-color: rgba(245,222,196,0.54)");
			b.setOnAction(k -> {
				if (!MessageStage.getInstance().isShowing())
					MessageStage.getInstance().start();
				else {
					MessageStage.getInstance().close();
					Stage primaryStage = GameStage.getInstance();
					primaryStage.setX(primaryStage.getX() + 125);
				}
			});
			overlayNonredrawingPane.getChildren().add(b);
		}
	}

	public void redraw() {
		final int player = controller.getActivePlayer();
		activeCardView = null;
		cardMap.clear();
		rootPane.getChildren().clear();
		overlayRedrawingPane.getChildren().clear();
		addBackground(R.image.board[controller.getActivePlayer()]);
		if (showCheats)
			addCheatButtons();

		addDeckCards(true);
		addProfile(true);

		addDeaths(0);
		addDeaths(1);

		addCardHBox(Position.hand, controller.getPlayer(player).handCards, false, true);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1 ? 5 - i : i;
			addCardHBox(Position.row[i], controller.getRow(actualRow), true, true);
			setInfoOnClicked(rootPane.getChildren().getLast(), controller.getRow(actualRow), false);
			addCardHBox(Position.special[i], controller.getSpecial(actualRow), false, true);
			int score = controller.calcRowScore(actualRow);
			addText(String.valueOf(score), Position.rowScore[i]);
		}
		addCardHBox(Position.weather, controller.getWeather(), false, true);
		setInfoOnClicked(rootPane.getChildren().getLast(), controller.getWeather(), false);

		for (int i = 0; i < 2; i++) {
			PlayerData data = controller.getPlayer(i == player ? 1 : 0);
			List<Card> leaderList = new ArrayList<>();
			List<Card> empty = new ArrayList<>();
			leaderList.add(data.deck.getLeader());
			addCardHBox(Position.leader[i], data.leaderUsed ? empty : leaderList, false, false);
			setInfoOnClicked(rootPane.getChildren().getLast(), leaderList, true);
		}

		addText(String.valueOf(controller.calcPlayerScore(1 - player)), Position.totalScore[0]);
		addText(String.valueOf(controller.calcPlayerScore(player)), Position.totalScore[1]);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1 ? 5 - i : i;

			if (player == controller.getTurn()
			    && controller.getPlayer(player).handCards.contains(controller.getActiveCard())) {
				if (controller.canPlace(player, actualRow, controller.getActiveCard()))
					addSelectionRect(Position.row[i], actualRow);
				if (controller.canPlaceSpecial(player, actualRow, controller.getActiveCard()))
					addSelectionRect(Position.special[i], actualRow + 6);
			}
		}
		if (player == controller.getTurn()
		    && controller.getPlayer(player).handCards.contains(controller.getActiveCard())
		    && controller.canPlaceWeather(player, controller.getActiveCard()))
			addSelectionRect(Position.weather, 12);

		if (controller.hasRain()) {
			addWeatherOverlay(Position.row[0], R.image.rain);
			addWeatherOverlay(Position.row[5], R.image.rain);
		}
		if (controller.hasFog()) {
			addWeatherOverlay(Position.row[1], R.image.fog);
			addWeatherOverlay(Position.row[4], R.image.fog);
		}
		if (controller.hasFrost()) {
			addWeatherOverlay(Position.row[2], R.image.frost);
			addWeatherOverlay(Position.row[3], R.image.frost);
		}

		for (Card card : animationCards) {
			CardView view = cardMap.get(card);
			cardMap.remove(card);
			for (Node node : rootPane.getChildren())
				if (node instanceof Pane)
					((Pane) node).getChildren().remove(view);
		}
		rootPane.getChildren().addAll(animationNodes);

		for (Card card : scorchCards)
			setCardViewScorch(cardMap.get(card));

		for (int i = 0; i < 2; i++) {
			HBox healthBar = new HBox();
			Position.hp[i].setBounds(healthBar);
			for (int j = 0; j < controller.getPlayer(i == player ? 1 : 0).hp; j++)
				healthBar.getChildren().add(new ImageView(R.image.gem_on));
			rootPane.getChildren().add(healthBar);
		}

		if (activeCardView != null) {
			ImageView imageView = new ImageView(R.getImage("icons/select.png"));//"SELECTION"
			imageView.setFitWidth(Position.card.w() + 5);
			imageView.setFitHeight(Position.card.h() + 5);
			imageView.setX(-2);
			imageView.setY(-2);
			activeCardView.getChildren().add(imageView);

			CardView info = CardView.newInfo(activeCardView.getAddress(), Position.info.w(), Position.info.h());
			Position.info.setBounds(info);
			rootPane.getChildren().add(info);
		}
		addWinnerSign();

		if (pickList != null)
			addPicker();

	}

	protected List<Card> pickList;
	private int pickIdx;
	protected Consumer<Card> pickFn;
	private boolean pickNullPossible;

	public void pickCard(List<Card> list, Consumer<Card> cb, boolean nullPossible) {
		assert pickList == null;

		if (list.isEmpty()) {
			cb.accept(null);
			return;
		}

		pickList = list;
		pickIdx = 0;
		pickFn = cb;
		pickNullPossible = nullPossible;
		redraw();
	}

	private final List<Consumer<Object>> cardListeners = new ArrayList<>();
	private final List<Consumer<Object>> buttonListeners = new ArrayList<>();
	private final List<Consumer<Object>> rowListeners = new ArrayList<>();
	private final List<Consumer<Object>> bgListeners = new ArrayList<>();

	public Object addCardListener(Consumer<Object> cb) { cardListeners.add(cb); return cb; }
	public Object addButtonListener(Consumer<Object> cb) { buttonListeners.add(cb); return cb; }
	public Object addRowListener(Consumer<Object> cb) { rowListeners.add(cb); return cb; }
	public Object addBgListener(Consumer<Object> cb) { bgListeners.add(cb); return cb; }

	public void removeListener(Object obj) {
		cardListeners.remove(obj);
		buttonListeners.remove(obj);
		rowListeners.remove(obj);
		bgListeners.remove(obj);
	}

	private void notifyListeners(List<Consumer<Object>> callbacks, Object obj) {
		// we make a deep copy because someone might remove their listeners while we are iterating
		for (Consumer<Object> cb : new ArrayList<>(callbacks))
			cb.accept(obj);
	}

	private final List<Node> animationNodes = new ArrayList<>();
	private final List<Card> animationCards = new ArrayList<>();

	private class MoveAnimation extends Transition {
		private final Node node;
		private final Point2D from, to;

		public MoveAnimation(int duration, Node node, Point2D from, Point2D to) {
			animationNodes.add(node);
			this.node = node;
			this.from = from;
			this.to = to;
			setCycleCount(1);
			setCycleDuration(Duration.millis(duration));
			setOnFinished(e -> finish());
			play();
		}

		private void finish() {
			stop();
			animationNodes.remove(node);
			finishChild();
			redraw();
		}

		protected void finishChild() {}

		@Override
		protected void interpolate(double frac) {
			Point2D pos = from.multiply(1 - frac).add(to.multiply(frac));
			node.setLayoutX(pos.getX());
			node.setLayoutY(pos.getY());
		}
	}

	private class CardMoveAnimation extends MoveAnimation {
		private final Card card;

		public CardMoveAnimation(Card card, Node node, Point2D from, Point2D to) {
			super(500, node, from, to);
			this.card = card;
			animationCards.add(card);
		}

		@Override
		protected void finishChild() {
			animationCards.remove(card);
		}
	}

	public void reactTo(Card card, int reactId) {
		int side = controller.getActivePlayer() == controller.getTurn()? 1: 0;
		CardView cardView = cardMap.get(card);
		Point2D from = cardView != null
				? cardView.localToScene(0, 0)
				: new Point2D(Position.react[side].x(), Position.react[side].y());
		Point2D to = from.add(0, -0.1 * HEIGHT);

		ImageView animationView = new ImageView(R.image.reactions[reactId]);
		animationView.setFitWidth(Position.react[side].w());
		animationView.setFitHeight(Position.react[side].h());

		new MoveAnimation(1500, animationView, from, to);
	}

	private void animationTo(Card card, Point2D from, Point2D to) {
		CardView animationView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
		new CardMoveAnimation(card, animationView, from, to);
	}

	private void animationToHBox(Card card, Position.RectPos pos, List<Card> others) {
		double x1 = pos.centerX() + (Position.card.w() * (others.size() - 1)) / 2;
		double x2 = pos.x() + pos.w();
		Point2D to = new Point2D(x1 < x2? x1: x2, pos.y());
		CardView cardView = cardMap.get(card);
		Point2D from = cardView == null ? new Point2D(0, 0) : cardView.localToScene(0, 0);
		animationTo(card, from, to);
	}

	public void animationToRow(Card card, int actualRow) {
		int row = controller.getActivePlayer() == 1 ? 5 - actualRow : actualRow;
		animationToHBox(card, Position.row[row], controller.getRow(actualRow));
	}

	public void animationToSpecial(Card card, int actualRow) {
		int row = controller.getActivePlayer() == 1 ? 5 - actualRow : actualRow;
		animationToHBox(card, Position.special[row], controller.getSpecial(actualRow));
	}

	public void animationToHand(Card card) {
		animationToHBox(card, Position.hand, controller.getPlayer(controller.getActivePlayer()).handCards);
	}

	public void animationToWeather(Card card) {
		animationToHBox(card, Position.weather, controller.getWeather());
	}

	public void animationToDeck(Card card, int player) {
		animationToHBox(card, Position.deck[player == controller.getActivePlayer() ? 1 : 0], new ArrayList<>());
	}

	public void animationToUsed(Card card, int player) {
		animationToHBox(card, Position.used[player == controller.getActivePlayer() ? 1 : 0], new ArrayList<>());
	}

	public void animationSwap(Card c1, Card c2) {
		Point2D p1 = cardMap.containsKey(c1) ? cardMap.get(c1).localToScene(0, 0) : new Point2D(0, 0);
		Point2D p2 = cardMap.containsKey(c2) ? cardMap.get(c2).localToScene(0, 0) : new Point2D(0, 0);
		animationTo(c1, p1, p2);
		animationTo(c2, p2, p1);
	}

	public boolean isAnimationPlaying() {
		return !animationNodes.isEmpty() || !scorchCards.isEmpty() || isMessageShowing || messageAnimation;
	}

	private List<Card> scorchCards = new ArrayList<>();

	public void setScorchCards(List<Card> list) {
		scorchCards = list;
	}

	private void setCardViewScorch(CardView view) {
		if (view == null)
			return;
		Rectangle rect = new Rectangle(0, 0, Position.card.w(), Position.card.h());
		rect.setFill(new ImagePattern(R.image.scorch, 0, 0, 1, 1, true));
		view.getChildren().add(rect);
	}

	public static class FivePlaceGame extends Pane {
		private final GameMenu gameMenu;
		private final StackPane[] images = new StackPane[5];
		private final List<Card> pickList;
		private int currentIndex;
		private int pickIdx;

		protected FivePlaceGame(GameMenu gameMenu, boolean nullPossible, List<Card> pickList, Pane gamePane, int picIdx) {
			this.pickIdx = picIdx;
			if(pickIdx >= pickList.size())
				pickIdx = pickList.size() - 1;
			if(picIdx < 0)
				pickIdx = 0;
			currentIndex = picIdx;
			this.gameMenu = gameMenu;
			this.pickList = new ArrayList<>(pickList);

			if(pickList.size() == 0)
				return;
			if (nullPossible) {
				setOnMouseClicked(k -> {
					gameMenu.pickList = null;
					gameMenu.pickFn.accept(null);
					gameMenu.redraw();
					gamePane.getChildren().remove(this);
				});
			} else {
				setOnMouseClicked(Event::consume);
			}
			setPrefWidth(WIDTH);
			setPrefHeight(HEIGHT);
			addPlaces();
			gamePane.getChildren().add(this);
		}

		private StackPane getStackPane(double width, double height, Pos pos) {
			StackPane stackPane = new StackPane();
			stackPane.setAlignment(pos);
			stackPane.setMinWidth(width);
			stackPane.setMaxWidth(width);
			stackPane.setMinHeight(height);
			stackPane.setMaxHeight(height);
			return stackPane;
		}

		private void addPlaces() {
			for (int i = 0; i < 5; i++) {
				StackPane stackPane = new StackPane();
				final double height = WIDTH / 2.5 - (60 / 1280.0 * WIDTH) * (Math.abs(i - 2)) + 4;
				final double width = HEIGHT / 6.0 - (20 / 720.0 * HEIGHT) * (Math.abs(i - 2)) + 4;
				switch (i) {
					case 0 -> stackPane = getStackPane(width, height, Pos.TOP_RIGHT);
					case 1, 3 -> stackPane = getStackPane(width, height, Pos.CENTER);
					case 2 -> stackPane = getStackPane(width, height, Pos.BOTTOM_CENTER);
					case 4 -> stackPane = getStackPane(width, height, Pos.TOP_LEFT);
				}
				images[i] = stackPane;
				addImage(pickIdx + i - 2, i);
				stackPane.setLayoutX((50 + 240 * i) / 1280.0 * WIDTH);
				stackPane.setLayoutY((100 - 20 * Math.abs(2 - i)) / 720.0 * HEIGHT);
				this.getChildren().add(stackPane);
			}
		}

		private void addImage(int cardIndex, int placeIndex) {
			try {
				images[placeIndex].getChildren().clear();
				Card card = pickList.get(cardIndex);
				CardView view = CardView.newInfo(card.pathAddress, Position.info.w(), Position.info.h());
				view.setPrefWidth(WIDTH / 6.0 - 20 * (Math.abs(placeIndex - 2)));
				view.setPrefHeight(HEIGHT / 2.5 - 60 * (Math.abs(placeIndex - 2)));
				view.setStyle("-fx-background-radius: 50px");
				view.setStyle("-fx-background-radius: 50px");
				images[placeIndex].setStyle("-fx-background-radius: 50px");
				DropShadow dropShadow = new DropShadow();
				dropShadow.setOffsetX(0);
				dropShadow.setOffsetY(0);
				if (placeIndex == 2) {
					dropShadow.setRadius(80);
					dropShadow.setColor(Color.rgb(8, 247, 63));
				} else {
					dropShadow.setRadius(60);
					dropShadow.setColor(Color.rgb(247, 211, 10));
				}
				view.setEffect(dropShadow);
				images[placeIndex].getChildren().add(view);
				view.setOnMouseClicked(k -> {
					if (placeIndex == 2) {
						if (k.getClickCount() > 1) clickedOn(placeIndex, card, cardIndex);
					} else clickedOn(placeIndex, card, cardIndex);
					k.consume();
				});

			} catch (IndexOutOfBoundsException ignored) {
			}
		}

		private void clickedOn(int index, Card card, int indexInList) {
			currentIndex += index - 2;
			setCurrentImage(currentIndex);
			if (index == 2) {
				gameMenu.pickList = null;
				gameMenu.pickFn.accept(card);
				gameMenu.changePicIdx(indexInList);
				gameMenu.redraw();
			}
		}

		private void setCurrentImage(int index) {
			for (int i = -2; i <= 2; i++) {
				addImage(index + i, 2 + i);
			}
		}
	}

	public static class MessageGame extends Pane {
		private final Pane gamePane;
		private final Pane self = this;
		private WaitExec waitExec = new WaitExec(false);

		MessageGame(Pane gamePane, Image image, String txt) {
			this.gamePane = gamePane;
			setPrefWidth(PreGameMenu.screenWidth);
			setPrefHeight(PreGameMenu.screenHeight);
			this.setLayoutX(0);
			this.setLayoutY(0);
			setOnMouseClicked(Event::consume);
			addImageView(R.getImage("icons/black.png"), 1280, 100, 0, 300);
			addImageView(image, 200, 200, 300, 230);
			addText(txt);
		}

		public void show(int firstTime) {
			waitExec.run(firstTime, () -> gamePane.getChildren().add(self));
			waitExec.run(firstTime + 1000, () -> gamePane.getChildren().remove(self));
		}

		private void addImageView(Image image, int width, int height, int x, int y) {
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(width / 1280.0 * WIDTH);
			imageView.setFitHeight(height / 720.0 * HEIGHT);
			imageView.setX(x/1280.0 * WIDTH);
			imageView.setY(y/720.0 * HEIGHT);
			getChildren().add(imageView);
		}

		private void addText(String comment) {
			Text text = new Text(comment);
			text.setStyle("-fx-font-family: 'Yrsa SemiBold'; -fx-font-size: 50px");
			text.setFill(Color.GOLD);
			text.setY(365 / 720.0 * HEIGHT);
			text.setX(500 / 1280.0 * WIDTH);
			getChildren().add(text);
		}
	}
}
