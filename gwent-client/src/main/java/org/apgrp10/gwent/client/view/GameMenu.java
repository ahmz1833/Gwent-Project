package org.apgrp10.gwent.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.model.CardView;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.GameController.PlayerData;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.utils.Callback;
import org.apgrp10.gwent.view.GameMenuInterface;

import javafx.animation.Transition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameMenu extends Application implements GameMenuInterface {
	private GameController controller;
	private Stage stage;
	private Pane rootPane;

	private static final int WIDTH = 1280, HEIGHT = 720;

	private static class Position {
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
			public static RectPos bySize(double x, double y, double w, double h) {
				return new RectPos(x, y, x+w, y+h);
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
			new RectPos(0.0666, 0.2824, 0.2260, 0.3324),
			new RectPos(0.0666, 0.6546, 0.2260, 0.7046),
		};
		public static final RectPos leader[] = {
			new RectPos(0.0713, 0.0740, 0.1244, 0.1990),
			new RectPos(0.0713, 0.7712, 0.1244, 0.8962),
		};
	}

	public GameMenu(Stage stage) {
		this.stage = stage;
	}

	public void setController(GameController controller) {
		this.controller = controller;
	}

	public void start() { start(stage); }

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		rootPane = new Pane();
		Scene scene = new Scene(rootPane);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setWidth(WIDTH);
		stage.setHeight(HEIGHT);
		stage.centerOnScreen();
		redraw();
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

	private void addButton(Pane parent, String str, String cmd, Position.RectPos pos) {
		Button btn = new Button(str);
		btn.setOnAction(e -> notifyListeners(buttonListeners, cmd));
		if (pos != null)
			pos.setBounds(btn);
		parent.getChildren().add(btn);
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

	private void addPicker() {
		Rectangle rect = new Rectangle(0, 0, WIDTH, HEIGHT);
		rect.setFill(Color.color(0, 0, 0, 0.5));
		if (pickNullPossible) {
			rect.setOnMouseClicked(e -> {
				pickList = null;
				pickFn.call(null);
				redraw();
			});
		}

		// TODO: make this more beautiful
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);
		for (Card card : pickList) {
			CardView view = CardView.newInfo(card.pathAddress, Position.info.w(), Position.info.h());
			final int idx = hbox.getChildren().size();
			view.setOnMouseClicked(e -> {
				if (pickIdx != idx) {
					((CardView)hbox.getChildren().get(pickIdx)).getChildren().remove(pickSelectedText);
					pickIdx = idx;
					((CardView)hbox.getChildren().get(pickIdx)).getChildren().add(pickSelectedText);
				} else {
					pickList = null;
					pickFn.call(card);
					redraw();
				}
			});
			hbox.getChildren().add(view);
		}

		pickSelectedText = new Text("Selected");
		pickSelectedText.setFill(Color.GOLD);
		pickSelectedText.setFont(new Font(32));
		pickSelectedText.setLayoutY(Position.info.h()/2);
		((CardView)hbox.getChildren().get(pickIdx)).getChildren().add(pickSelectedText);

		ScrollPane scroller = new ScrollPane(hbox);
		scroller.setMaxSize(WIDTH, HEIGHT);

		rootPane.getChildren().add(rect);
		rootPane.getChildren().add(scroller);
	}

	public void addWeatherOverlay(Position.RectPos pos, Image image) {
		Rectangle rect = new Rectangle(pos.x(), pos.y(), pos.w(), pos.h());
		rect.setMouseTransparent(true);
		rect.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
		rootPane.getChildren().add(rect);
	}

	private void addCheatButtons() {
		HBox cheats = new HBox();
		addButton(cheats, "Cheat: take from deck", "cheat_0", null);
		addButton(cheats, "Cheat: add hp", "cheat_1", null);
		addButton(cheats, "Cheat: restore leader", "cheat_2", null);
		addButton(cheats, "Cheat: clear weather", "cheat_3", null);
		addButton(cheats, "Cheat: restore graveyard", "cheat_4", null);
		addButton(cheats, "Cheat: show opponent's hand", "cheat_5", null);
		addButton(cheats, "Cheat: horn", "cheat_6", null);
		for (Node node : cheats.getChildren())
			node.setStyle("-fx-font-size:8");
		rootPane.getChildren().add(cheats);
	}

	public void redraw() {
		final int player = controller.getActivePlayer();
		activeCardView = null;

		for (Card card : controller.getPlayer(0).handCards) {
			CardView view = cardMap.get(card);
			if (view == null)
				continue;
		}
		rootPane.getChildren().clear();

		addBackground(R.image.board[controller.getActivePlayer()]);

		// TODO: hide these
		addCheatButtons();
		addButton(rootPane, "Pass", "pass", Position.pass);

		addCardHBox(Position.hand, controller.getPlayer(player).handCards, false, true);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1? 5 - i: i;
			addCardHBox(Position.row[i], controller.getRow(actualRow), true, true);
			setInfoOnClicked(rootPane.getChildren().getLast(), controller.getRow(actualRow), false);
			addCardHBox(Position.special[i], controller.getSpecial(actualRow), false, true);
			int score = controller.calcRowScore(actualRow);
			addText(String.valueOf(score), Position.rowScore[i]);
		}
		addCardHBox(Position.weather, controller.getWeather(), false, true);
		setInfoOnClicked(rootPane.getChildren().getLast(), controller.getWeather(), false);

		for (int i = 0; i < 2; i++) {
			PlayerData data = controller.getPlayer(i == player? 1: 0);
			List<Card> leaderList = new ArrayList<>();
			List<Card> empty = new ArrayList<>();
			leaderList.add(data.deck.getLeader());
			addCardHBox(Position.leader[i], data.leaderUsed? empty: leaderList, false, false);
			setInfoOnClicked(rootPane.getChildren().getLast(), leaderList, true);
		}

		addText(String.valueOf(controller.calcPlayerScore(1 - player)), Position.totalScore[0]);
		addText(String.valueOf(controller.calcPlayerScore(player)), Position.totalScore[1]);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1? 5 - i: i;

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
					((Pane)node).getChildren().remove(view);
		}
		rootPane.getChildren().addAll(animationNodes);

		for (Card card : scorchCards)
			setCardViewScorch(cardMap.get(card));

		for (int i = 0; i < 2; i++) {
			HBox healthBar = new HBox();
			Position.hp[i].setBounds(healthBar);
			for (int j = 0; j < controller.getPlayer(i == player? 1: 0).hp; j++)
				healthBar.getChildren().add(new ImageView(R.image.gem_on));
			rootPane.getChildren().add(healthBar);
		}

		// TODO: use some highlight or something
		if (activeCardView != null) {
			Text text = new Text("Sel");
			text.setFill(Color.GOLD);
			text.setLayoutY(Position.card.h()/2);
			activeCardView.getChildren().add(text);

			CardView info = CardView.newInfo(activeCardView.getAddress(), Position.info.w(), Position.info.h());
			Position.info.setBounds(info);
			rootPane.getChildren().add(info);
		}

		if (pickList != null)
			addPicker();
	}

	private List<Card> pickList;
	private int pickIdx;
	private Callback<Card> pickFn;
	private boolean pickNullPossible;

	public void pickCard(List<Card> list, Callback<Card> cb, boolean nullPossible) {
		assert pickList == null;

		if (list.isEmpty()) {
			cb.call(null);
			return;
		}

		pickList = list;
		pickIdx = 0;
		pickFn = cb;
		pickNullPossible = nullPossible;
		redraw();
	}

	private final List<Callback<Object>> cardListeners = new ArrayList<>();
	private final List<Callback<Object>> buttonListeners = new ArrayList<>();
	private final List<Callback<Object>> rowListeners = new ArrayList<>();
	private final List<Callback<Object>> bgListeners = new ArrayList<>();

	public Object addCardListener(Callback<Object> cb) { cardListeners.add(cb); return cb; }
	public Object addButtonListener(Callback<Object> cb) { buttonListeners.add(cb); return cb; }
	public Object addRowListener(Callback<Object> cb) { rowListeners.add(cb); return cb; }
	public Object addBgListener(Callback<Object> cb) { bgListeners.add(cb); return cb; }

	public void removeListener(Object obj) {
		cardListeners.remove(obj);
		buttonListeners.remove(obj);
		rowListeners.remove(obj);
		bgListeners.remove(obj);
	}

	private void notifyListeners(List<Callback<Object>> callbacks, Object obj) {
		// we make a deep copy because someone might remove their listeners while we are iterating
		for (Callback<Object> cb : new ArrayList<>(callbacks))
			cb.call(obj);
	}

	private final List<Node> animationNodes = new ArrayList<>();
	private final List<Card> animationCards = new ArrayList<>();

	private class MoveAnimation extends Transition {
		private final Node node;
		private final Card card;
		private final Point2D from, to;

		public MoveAnimation(Card card, Node node, Point2D from, Point2D to) {
			animationNodes.add(node);
			animationCards.add(card);
			this.card = card;
			this.node = node;
			this.from = from;
			this.to = to;
			setCycleCount(1);
			setCycleDuration(Duration.millis(500));
			setOnFinished(e -> finish());
			play();
		}

		private void finish() {
			stop();
			animationNodes.remove(node);
			animationCards.remove(card);
			redraw();
		}

		@Override
		protected void interpolate(double frac) {
			Point2D pos = from.multiply(1 - frac).add(to.multiply(frac));
			node.setLayoutX(pos.getX());
			node.setLayoutY(pos.getY());
		}
	}

	private void animationTo(Card card, Point2D from, Point2D to) {
		CardView animationView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
		new MoveAnimation(card, animationView, from, to);
	}

	private void animationToHBox(Card card, Position.RectPos pos, List<Card> others) {
		Point2D to = new Point2D(
			pos.centerX() + (Position.card.w() * (others.size() - 1)) / 2,
			pos.y()
		);
		CardView cardView = cardMap.get(card);
		Point2D from = cardView == null? new Point2D(0, 0): cardView.localToScene(0, 0);
		animationTo(card, from, to);
	}

	public void animationToRow(Card card, int actualRow) {
		int row = controller.getActivePlayer() == 1? 5 - actualRow: actualRow;
		animationToHBox(card, Position.row[row], controller.getRow(actualRow));
	}
	public void animationToSpecial(Card card, int actualRow) {
		int row = controller.getActivePlayer() == 1? 5 - actualRow: actualRow;
		animationToHBox(card, Position.special[row], controller.getSpecial(actualRow));
	}
	public void animationToHand(Card card) {
		animationToHBox(card, Position.hand, controller.getPlayer(controller.getActivePlayer()).handCards);
	}
	public void animationToWeather(Card card) {
		animationToHBox(card, Position.weather, controller.getWeather());
	}
	public void animationToDeck(Card card, int player) {
		animationToHBox(card, Position.deck[player == controller.getActivePlayer()? 1: 0], new ArrayList<>());
	}
	public void animationToUsed(Card card, int player) {
		animationToHBox(card, Position.used[player == controller.getActivePlayer()? 1: 0], new ArrayList<>());
	}

	public void animationSwap(Card c1, Card c2) {
		Point2D p1 = cardMap.containsKey(c1)? cardMap.get(c1).localToScene(0, 0): new Point2D(0, 0);
		Point2D p2 = cardMap.containsKey(c2)? cardMap.get(c2).localToScene(0, 0): new Point2D(0, 0);
		animationTo(c1, p1, p2);
		animationTo(c2, p2, p1);
	}

	public boolean isAnimationPlaying() { return !animationNodes.isEmpty() || !scorchCards.isEmpty(); }

	private List<Card> scorchCards = new ArrayList<>();
	public void setScorchCards(List<Card> list) { scorchCards = list; }
	private void setCardViewScorch(CardView view) {
		if (view == null)
			return;
		Rectangle rect = new Rectangle(0, 0, Position.card.w(), Position.card.h());
		rect.setFill(new ImagePattern(R.image.scorch, 0, 0, 1, 1, true));
		view.getChildren().add(rect);
	}
}
