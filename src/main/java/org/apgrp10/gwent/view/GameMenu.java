package org.apgrp10.gwent.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apgrp10.gwent.R;
import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.model.card.Card;
import org.apgrp10.gwent.model.card.CardView;

import javafx.animation.Transition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

	public void redraw() {
		for (Card card : controller.getPlayer(0).handCards) {
			CardView view = cardMap.get(card);
			if (view == null)
				continue;
		}
		rootPane.getChildren().clear();

		ImageView background = new ImageView(R.image.board);
		background.setFitWidth(WIDTH);
		background.setFitHeight(HEIGHT);
		background.setOnMouseClicked(e -> notifyListeners(bgListeners, null));
		rootPane.getChildren().add(background);

		Button hello = new Button("Hello");
		hello.setOnAction(e -> notifyListeners(buttonListeners, "hello"));
		rootPane.getChildren().add(hello);

		final int player = controller.getActivePlayer();

		CardView activeCardView = null;

		HBox hand = new HBox();
		hand.setAlignment(Pos.CENTER);
		Position.hand.setBounds(hand);
		for (Card card : controller.getPlayer(player).handCards) {
			CardView cardView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
			hand.getChildren().add(cardView);
			cardView.setOnMouseClicked(e -> notifyListeners(cardListeners, card));
			if (controller.getActiveCard() == card)
				activeCardView = cardView;
			cardMap.put(card, cardView);
		}
		rootPane.getChildren().add(hand);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1? 5 - i: i;

			HBox hbox = new HBox();
			hbox.setAlignment(Pos.CENTER);
			Position.row[i].setBounds(hbox);
			for (Card card : controller.getRow(actualRow)) {
				CardView cardView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
				hbox.getChildren().add(cardView);
				cardMap.put(card, cardView);
			}
			rootPane.getChildren().add(hbox);

			int score = controller.calcRowScore(actualRow);
			addText(String.valueOf(score), Position.rowScore[i]);
		}

		addText(String.valueOf(controller.calcPlayerScore(1 - player)), Position.totalScore[0]);
		addText(String.valueOf(controller.calcPlayerScore(player)), Position.totalScore[1]);

		for (int i = 0; i < 6; i++) {
			int actualRow = player == 1? 5 - i: i;

			Position.RectPos pos = Position.row[i];
			Rectangle rect = new Rectangle(pos.x(), pos.y(), pos.w(), pos.h());
			rect.setOnMouseClicked(e -> notifyListeners(rowListeners, actualRow));
			Color col;
			if (player == controller.getTurn()
					&& controller.getPlayer(player).handCards.contains(controller.getActiveCard())
					&& controller.canPlace(player, actualRow, controller.getActiveCard()))
				col = Color.color(0.5, 0.5, 0, 0.3);
			else
				col = Color.color(0, 0, 0, 0);
			rect.setFill(col);
			rootPane.getChildren().add(rect);
		}

		if (animationNode != null) {
			CardView view = cardMap.get(animationCard);
			cardMap.remove(animationCard);
			for (Node node : rootPane.getChildren()) {
				if (node instanceof Pane) {
					List<Node> list = ((Pane)node).getChildren();
					for (int i = 0; i < list.size(); i++)
						if (list.get(i) == view)
							list.remove(i);
				}
			}
			rootPane.getChildren().add(animationNode);
		}

		// TODO: use some highlight or something
		if (activeCardView != null) {
			Text text = new Text("Sel");
			text.setFill(Color.GOLD);
			activeCardView.getChildren().add(text);

			CardView info = CardView.newInfo(activeCardView.getAddress(), Position.info.w(), Position.info.h());
			Position.info.setBounds(info);
			rootPane.getChildren().add(info);
		}
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
		for (Callback cb : callbacks)
			cb.call(obj);
	}

	private Node animationNode;
	private Card animationCard;

	private class MoveAnimation extends Transition {
		private final Node node;
		private final Point2D from, to;

		public MoveAnimation(Node node, Point2D from, Point2D to) {
			animationNode = node;
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
			if (animationNode == node) {
				animationNode = null;
				redraw();
			}
		}

		@Override
		protected void interpolate(double frac) {
			Point2D pos = from.multiply(1 - frac).add(to.multiply(frac));
			node.setLayoutX(pos.getX());
			node.setLayoutY(pos.getY());
		}
	}

	private void animationTo(Card card, Point2D from, Point2D to) {
		animationCard = card;

		CardView animationView = CardView.newHand(card.pathAddress, Position.card.w(), Position.card.h());
		new MoveAnimation(animationView, from, to);
	}

	public void animationToRow(Card card, int actualRow) {
		int row = controller.getActivePlayer() == 1? 5 - actualRow: actualRow;
		Point2D to = new Point2D(
			Position.row[row].centerX() + (Position.card.w() * (controller.getRow(actualRow).size() - 1)) / 2,
			Position.row[row].y()
		);

		CardView cardView = cardMap.get(card);
		Point2D from = cardView == null? new Point2D(0, 0): cardView.localToScene(0, 0);

		animationTo(card, from, to);
	}

	public void animationToHand(Card card) {
		List<Card> hand = controller.getPlayer(controller.getActivePlayer()).handCards;
		Point2D to = new Point2D(
			Position.hand.centerX() + (Position.card.w() * (hand.size() - 1)) / 2,
			Position.hand.y()
		);
		animationTo(card, new Point2D(0, 0), to);
	}
}
