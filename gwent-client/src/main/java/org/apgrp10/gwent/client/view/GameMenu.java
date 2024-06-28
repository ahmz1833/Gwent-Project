package org.apgrp10.gwent.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apgrp10.gwent.client.R;
import org.apgrp10.gwent.client.controller.GameController;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.client.model.CardView;

import javafx.animation.Transition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class GameMenu extends Application {
	private GameController controller;
	private Stage stage;
	private Pane rootPane;

	public static GameMenu currentMenu;
	private static final int WIDTH = 1280, HEIGHT = 720;

	private static class Position {
		public static record RectPos(double posX, double posY, double posX2, double posY2) {
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
	}


	public GameMenu(GameController gameController, Stage stage) {
		controller = gameController;
		currentMenu = this;
		start(stage);
	}

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

	private void addButton(String str, String cmd, Position.RectPos pos) {
		Button btn = new Button(str);
		btn.setOnAction(e -> notifyListeners(buttonListeners, cmd));
		if (pos != null) // TODO: this if should be removed in the future
			pos.setBounds(btn);
		rootPane.getChildren().add(btn);
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

	private void addCardHBox(Position.RectPos pos, List<Card> cards) {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);
		pos.setBounds(hbox);
		for (Card card : cards) {
			CardView cardView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
			hbox.getChildren().add(cardView);
			cardView.setOnMouseClicked(e -> {
				e.consume();
				notifyListeners(cardListeners, card);
			});
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
					pickIdx = idx;
				} else {
					pickList = null;
					pickFn.call(card);
				}
				redraw();
			});
			hbox.getChildren().add(view);
		}

		Text text = new Text("Selected");
		text.setFill(Color.GOLD);
		text.setFont(new Font(32));
		((CardView)hbox.getChildren().get(pickIdx)).getChildren().add(text);

		rootPane.getChildren().add(rect);
		rootPane.getChildren().add(hbox);
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

		addBackground(R.image.board);

		addButton("Hello", "hello", null);
		addButton("Pass", "pass", Position.pass);

		addCardHBox(Position.hand, controller.getPlayer(player).handCards);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1? 5 - i: i;
			addCardHBox(Position.row[i], controller.getRow(actualRow));
			setInfoOnClicked(rootPane.getChildren().get(rootPane.getChildren().size() - 1), controller.getRow(actualRow), false);
			addCardHBox(Position.special[i], controller.getSpecial(actualRow));
			int score = controller.calcRowScore(actualRow);
			addText(String.valueOf(score), Position.rowScore[i]);
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

		for (Card card : animationCards) {
			CardView view = cardMap.get(card);
			cardMap.remove(card);
			for (Node node : rootPane.getChildren())
				if (node instanceof Pane)
					((Pane)node).getChildren().remove(view);
		}
		rootPane.getChildren().addAll(animationNodes);

		// TODO: use some highlight or something
		if (activeCardView != null) {
			Text text = new Text("Sel");
			text.setFill(Color.GOLD);
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
	private Callback pickFn;
	private boolean pickNullPossible;

	public void pickCard(List<Card> list, Callback cb, boolean nullPossible) {
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

	public static interface Callback { public void call(Object object); }
	private final List<Callback> cardListeners = new ArrayList<>();
	private final List<Callback> buttonListeners = new ArrayList<>();
	private final List<Callback> rowListeners = new ArrayList<>();
	private final List<Callback> bgListeners = new ArrayList<>();

	public Object addCardListener(Callback cb) { cardListeners.add(cb); return cb; }
	public Object addButtonListener(Callback cb) { buttonListeners.add(cb); return cb; }
	public Object addRowListener(Callback cb) { rowListeners.add(cb); return cb; }
	public Object addBgListener(Callback cb) { bgListeners.add(cb); return cb; }

	public void removeListener(Object obj) {
		cardListeners.remove(obj);
		buttonListeners.remove(obj);
		rowListeners.remove(obj);
		bgListeners.remove(obj);
	}

	private void notifyListeners(List<Callback> callbacks, Object obj) {
		// we make a deep copy because someone might remove their listeners while we are iterating
		List<Callback> copy = new ArrayList<>();
		copy.addAll(callbacks);
		for (Callback cb : copy)
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

	public void animationSwap(Card c1, Card c2) {
		Point2D p1 = cardMap.containsKey(c1)? cardMap.get(c1).localToScene(0, 0): new Point2D(0, 0);
		Point2D p2 = cardMap.containsKey(c2)? cardMap.get(c2).localToScene(0, 0): new Point2D(0, 0);
		animationTo(c1, p1, p2);
		animationTo(c2, p2, p1);
	}
}