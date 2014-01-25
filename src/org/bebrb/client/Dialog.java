package org.bebrb.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import application.DialogController;
import application.Main;

public class Dialog {
	private Pane root;
	private Node source;
	private StackPane maskPane;
	private List<Node> lockedControls=new ArrayList<Node>();
	private DialogController ctrlDialog;
	private DialogResult handlerOk;
	private Node firstInFocus = null;
	private boolean visible;
	private int waitCount;

	public Dialog(Pane root, Node source) {
		this.root = root;
		this.source = source;
	}
	
	public void showForResult(DialogResult handler) throws IOException {
		handlerOk = handler;
		Pane dlg = beforeShow();
		dlg.getChildren().add(source);
		visible = true;
	}
	
	public void close() {
		visible = false;
		root.getChildren().remove(ctrlDialog.getRoot());
		root.getChildren().remove(maskPane);
		for (Node n : lockedControls) n.setDisable(false);
		lockedControls.clear();
	}
	
	public Node getFirstInFocus() {
		return firstInFocus;
	}

	public void setFirstInFocus(Node firstInFocus) {
		this.firstInFocus = firstInFocus;
	}
	
	public void addActionMessage(String message) {
		final Label l = new Label(message);
		final Image image = new Image(ClassLoader.getSystemResourceAsStream("application/images/error-small.png"));
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					l.setGraphic(new ImageView(image));
					ctrlDialog.getErrorBox().getChildren().add(l);
				}
			});
			
		} else {
			l.setGraphic(new ImageView(image));
			ctrlDialog.getErrorBox().getChildren().add(l);
		}	
	}

	public void clearActionMessages() {
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					ctrlDialog.getErrorBox().getChildren().clear();
				}
			});
			
		} else {
			ctrlDialog.getErrorBox().getChildren().clear();
		}	
	}
	
	public void waiting() {
		if(waitCount==0) {
			ctrlDialog.getRoot().setDisable(true);
			setProgress(true);
		}
		waitCount++;
	}

	public void ready() {
		waitCount--;
		if(waitCount==0) {
			ctrlDialog.getRoot().setDisable(false);
			setProgress(false);
		}
	}

	public boolean isVisible() {
		return visible;
	}
	
	public boolean isReady() {
		return waitCount==0;
	} 
	
	private Pane beforeShow() throws IOException {
		// mask pane
		maskPane = new StackPane();
		maskPane.setStyle("-fx-background-color:black;");
		maskPane.setOpacity(0.5);
		root.getChildren().add(maskPane);
		AnchorPane.setLeftAnchor(maskPane, 0D);AnchorPane.setRightAnchor(maskPane, 0D);
		AnchorPane.setTopAnchor(maskPane, 0D);AnchorPane.setBottomAnchor(maskPane, 0D);
		//p.setPrefSize(root.getWidth(), root.getHeight());
		// disable focus control
		ObservableList<Node> children = root.getChildren();
		for(int i=0,len=children.size()-1; i<len; i++) {
			Node child = children.get(i);
			if(!child.isDisable()) {
				child.setDisable(true);
				lockedControls.add(child);
			}
		}
		// load skeleton
		ctrlDialog = Main.loadNodeController("Dialog.fxml");
		try {
			LocaleUtils.localeFields(ctrlDialog);
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
		final Pane dlg = ctrlDialog.getRoot();
		if(source instanceof Region) {
			double min = ((Region)source).getMinWidth();
			if(min>=0) dlg.setMinWidth(min+20D); 
			double max = ((Region)source).getMaxWidth();
			if(max>=0) dlg.setMaxWidth(max+20D); 
		}
		
		root.getChildren().add(dlg);
		
		ctrlDialog.getBtnOk().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(!handlerOk.before()) return;
				clearActionMessages();
				try {
					if(handlerOk.handle(true)) close();
				} finally {
					handlerOk.after();
				}
			}
		});

		ctrlDialog.getBtnCancel().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(handlerOk.handle(false)) close();
			}
		});
		
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				dlg.setLayoutX((root.getWidth()-dlg.getWidth())/2);
				double anchor = dlg.getLayoutX();
				if(anchor<20) anchor = 30; 
				AnchorPane.setLeftAnchor(dlg, anchor);
				AnchorPane.setRightAnchor(dlg, anchor);
				
				AnchorPane.setLeftAnchor(source, 0D);
				AnchorPane.setRightAnchor(source, 0D);

				Node focusnode;
				if(firstInFocus==null) {
					focusnode = findFirstFocusNode((Parent) source);
					if(focusnode==null) focusnode = ctrlDialog.getBtnOk();
				} else
					focusnode = firstInFocus; 
				focusnode.requestFocus();
			}
		});
		
		return ctrlDialog.getContent();
	}

	private Node findFirstFocusNode(Parent node) {
		ObservableList<Node> list = node.getChildrenUnmodifiable();
		for (int i = 0, len = list.size(); i < len; i++) {
			Node n = list.get(i);
			if(!n.isDisable() && n.isFocusTraversable())
				return n;
			if(n instanceof Parent) {
				n = findFirstFocusNode((Parent) n);
				if(n!=null) return n;
			}
		}
		return null;
		
	}

	private void setProgress(final boolean b) {
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setProgressInner(b);
				}
			});
			
		} else {
			setProgressInner(b);
		}	
	}
	
	private void setProgressInner(boolean b) {
		if (b) {
			ProgressIndicator indicator = new ProgressIndicator();
			indicator.setMinSize(16, 16);
			indicator.setPrefSize(16, 16);
			indicator.setBlendMode(BlendMode.RED);
			ctrlDialog.getBtnOk().setGraphic(indicator);
		} else
			ctrlDialog.getBtnOk().setGraphic(null);
	}
	
	public interface DialogResult{
		public boolean before();
		public boolean handle(boolean btnOk);
		public void after();
	}

	

}
