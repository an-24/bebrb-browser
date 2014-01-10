package org.bebrb.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import utils.LocaleUtils;
import application.DialogController;
import application.Main;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class Dialog {
	private Pane root;
	private Node source;
	private StackPane maskPane;
	private List<Node> lockedControls=new ArrayList<Node>();
	private DialogController ctrlDialog;
	private DialogResult handlerOk;

	public Dialog(Pane root, Node source) {
		this.root = root;
		this.source = source;
	}
	
	public void showForResult(DialogResult handler) throws IOException {
		handlerOk = handler;
		Pane dlg = beforeShow();
		dlg.getChildren().add(source);
	}
	
	public void close() {
		root.getChildren().remove(ctrlDialog.getRoot());
		root.getChildren().remove(maskPane);
		for (Node n : lockedControls) n.setDisable(false);
		lockedControls.clear();
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
		root.getChildren().add(dlg);
		
		ctrlDialog.getBtnOk().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(handlerOk.handle(true)) close();
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
			}
		});
		
		return ctrlDialog.getContent();
	}

	public interface DialogResult{
		public boolean handle(boolean btnOk);
	}

}