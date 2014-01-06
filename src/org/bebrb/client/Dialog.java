package org.bebrb.client;

import java.io.IOException;

import application.DialogController;
import application.Main;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class Dialog {
	private Pane root;
	private Node source;

	public Dialog(Pane root, Node source) {
		this.root = root;
		this.source = source;
	}
	
	public void showForResult(DialogResult handler) throws IOException {
		Pane dlg = beforeShow();
		dlg.getChildren().add(source);
	}
	
	private Pane beforeShow() throws IOException {
		// mask pane
		Pane p = new StackPane();
		p.setStyle("-fx-background-color:black;");
		p.setOpacity(0.2);
		root.getChildren().add(p);
		AnchorPane.setLeftAnchor(p, 0D);AnchorPane.setRightAnchor(p, 0D);
		AnchorPane.setTopAnchor(p, 0D);AnchorPane.setBottomAnchor(p, 0D);
		//p.setPrefSize(root.getWidth(), root.getHeight());
		// disable focus control
		ObservableList<Node> children = root.getChildren();
		for(int i=0,len=children.size()-1; i<len; i++)
			children.get(i).setDisable(true);
		// load skeleton
		final DialogController ctrl = Main.loadNodeController("Dialog.fxml");
		final Pane dlg = ctrl.getRoot();
		root.getChildren().add(dlg);
		
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
		
		return ctrl.getContent();
	}

	public interface DialogResult{
		public boolean handle(boolean btnOk);
	}

}
