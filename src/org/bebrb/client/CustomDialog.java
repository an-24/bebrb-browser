package org.bebrb.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class CustomDialog {
	
	public enum ButtonType {Ok,Yes,No,Cancel};
	
	protected Pane root;
	protected Node source;
	protected boolean visible;
	protected DialogResult handlerOk;
	private Node firstInFocus = null;
	private StackPane maskPane;
	private List<Node> lockedControls=new ArrayList<Node>();
	private FormController ctrlDialog;

	public CustomDialog(Pane root, Node source) {
		this.root = root;
		this.source = source;
	}

	public boolean isVisible() {
		return visible;
	}
	

	public Node getFirstInFocus() {
		return firstInFocus;
	}

	public void setFirstInFocus(Node firstInFocus) {
		this.firstInFocus = firstInFocus;
	}

	public void showForResult(DialogResult handler) {
		try {
			handlerOk = handler;
			Pane dlg = beforeShow();
			dlg.getChildren().add(source);
			root.getChildren().add(maskPane);
			root.getChildren().add(ctrlDialog.getRoot());
			visible = true;
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).severe("Dialog show fault");;
		}
	}

	public void show() {
		try {
			handlerOk = null;
			Pane dlg = beforeShow();
			dlg.getChildren().add(source);
			root.getChildren().add(maskPane);
			root.getChildren().add(ctrlDialog.getRoot());
			visible = true;
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).severe("Dialog show fault");;
		}
	}
	
	public void close() {
		visible = false;

		//ctrlDialog.getRoot().setDisable(true);;
		//root.getScene().getAccelerators().remove();
		
		root.getChildren().remove(ctrlDialog.getRoot());
		root.getChildren().remove(maskPane);
		for (Node n : lockedControls) n.setDisable(false);
		lockedControls.clear();
	}

	protected FormController loadForm() throws IOException {
		return null;
	}
	
	protected FormController getController() {
		return ctrlDialog;
	}
	
	private Pane beforeShow() throws IOException {
		// mask pane
		maskPane = new StackPane();
		maskPane.setStyle("-fx-background-color:black;");
		maskPane.setOpacity(0.5);
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
		ctrlDialog = loadForm();
		final Pane dlg = ctrlDialog.getRoot();
		
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
					if(focusnode==null) focusnode = getDefaultControl();
				} else
					focusnode = firstInFocus; 
				focusnode.requestFocus();
			}
		});
		
		return ctrlDialog.getContent();
	}

	protected Node getDefaultControl() {
		return null;
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

	
	public interface DialogResult{
		public boolean before(CustomDialog dialog);
		public boolean handle(CustomDialog dialog, ButtonType btnClick);
		public void after(CustomDialog dialog);
	}


}
