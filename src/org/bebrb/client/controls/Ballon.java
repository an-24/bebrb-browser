package org.bebrb.client.controls;

import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Ballon {

	private static final double BALLON_WIDTH_DEFAULT = 200;
	private String message;
	private AnchorPane paneContent;
	private Popup popup;
	private Label label;
	private double width;

	public Ballon(String message) {
		this(message, BALLON_WIDTH_DEFAULT);
	}

	public Ballon(String message, double width) {
		this.message = message;
		this.width = width;
		paneContent = new AnchorPane();
		popup = new Popup();
		popup.getContent().add(paneContent);
		popup.setAutoHide(true);
		popup.setAutoFix(true);

		label = new Label(message);
		label.setWrapText(true);
		label.setPrefWidth(width - 20D);
		Image image = new Image(
				ClassLoader
						.getSystemResourceAsStream("application/images/error-small.png"));
		label.setGraphic(new ImageView(image));

		AnchorPane.setLeftAnchor(label, 10D);
		AnchorPane.setRightAnchor(label, 10D);
		AnchorPane.setTopAnchor(label, 20D);
		AnchorPane.setBottomAnchor(label, 10D);

		paneContent.getChildren().addAll(label);

	}

	public String getMessage() {
		return message;
	}

	public void show(Control node) {
		if (!popup.isShowing()) {
			Point2D p = node.localToScene(0, 0);
			Scene scene = node.getScene();
			Window w = node.getScene().getWindow();

			paneContent.setOpacity(0);
			popup.show(w, p.getX() + w.getX() + scene.getX(), p.getY() + w.getY()
					+ scene.getY() + node.getHeight() - 6);

			if (paneContent.getChildren().size() == 1) {
				Shape ballon = makeBallon(label.getHeight() + 20D, width);
				paneContent.getChildren().add(0, ballon);
			}
			FadeTransition ft = new FadeTransition(Duration.millis(2000),
					paneContent);
			ft.setFromValue(0);
			ft.setToValue(1);
			ft.play();
		}
	}

	public void show(Control node, int timeVisible) {
		if (timeVisible < 0) {
			show(node);
		} else {
			show(node);
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					hide();
				}
			}, timeVisible, 0);
		}
	}

	public void hide() {
		if (popup.isShowing()) {
			FadeTransition ft = new FadeTransition(Duration.millis(2000),
					paneContent);
			ft.setFromValue(1);
			ft.setToValue(0);
			ft.play();
			ft.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					popup.hide();
				}
			});
		}
	}
	public void hideDirectly() {
		popup.hide();
	}

	private Shape makeBallon(double height, double width) {
		double dX = 10, dY = 10;

		Rectangle r = new Rectangle();
		r.setLayoutY(dY);
		r.setWidth(width);
		r.setHeight(height);
		r.setArcWidth(20);
		r.setArcHeight(20);

		Polygon pg = new Polygon(dX, dY, dX, 0, 2 * dX, dY);

		final Shape ballon = Shape.union(r, pg);

		Stop[] stops = new Stop[] { new Stop(0, Color.WHITE),
				new Stop(1, Color.GOLD) };
		LinearGradient lg1 = new LinearGradient(0, 0, 0, 1, true,
				CycleMethod.NO_CYCLE, stops);

		ballon.setFill(lg1);
		ballon.setStroke(Color.GRAY);
		ballon.setStrokeType(StrokeType.OUTSIDE);

		DropShadow shadow = new DropShadow();
		shadow.setOffsetX(2);
		shadow.setOffsetY(2);
		ballon.setEffect(shadow);

		return ballon;

	}
}
