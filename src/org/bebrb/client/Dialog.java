package org.bebrb.client;

import java.io.IOException;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import org.bebrb.client.utils.LocaleUtils;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;

import application.DialogController;
import application.Main;

public class Dialog extends CustomDialog {
	private int waitCount;

	public Dialog(Pane root, Node source) {
		super(root,source);
	}
	
	public void addActionMessage(String message) {
		final Label l = new Label(message);
		final Image image = new Image(ClassLoader.getSystemResourceAsStream("application/images/error-small.png"));
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					l.setGraphic(new ImageView(image));
					((DialogController) getController()).getErrorBox().getChildren().add(l);
				}
			});
			
		} else {
			l.setGraphic(new ImageView(image));
			((DialogController) getController()).getErrorBox().getChildren().add(l);
		}	
	}

	public void clearActionMessages() {
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					((DialogController) getController()).getErrorBox().getChildren().clear();
				}
			});
			
		} else {
			((DialogController) getController()).getErrorBox().getChildren().clear();
		}	
	}
	
	public void waiting() {
		if(waitCount==0) {
			getController().getRoot().setDisable(true);
			setProgress(true);
		}
		waitCount++;
	}

	public void ready() {
		waitCount--;
		if(waitCount==0) {
			getController().getRoot().setDisable(false);
			setProgress(false);
		}
	}

	public boolean isReady() {
		return waitCount==0;
	} 

	protected Node getDefaultControl() {
		return ((DialogController) getController()).getBtnOk();
	}

	protected FormController loadForm() throws IOException {
		DialogController ctrl = Main.loadNodeController("Dialog.fxml");
		try {
			LocaleUtils.localeFields(ctrl);
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
		final Pane dlg = ctrl.getRoot();
		if(source instanceof Region) {
			double min = ((Region)source).getMinWidth();
			if(min>=0) dlg.setMinWidth(min+20D); 
			double max = ((Region)source).getMaxWidth();
			if(max>=0) dlg.setMaxWidth(max+20D); 
		}
		
		ctrl.getBtnOk().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(!handlerOk.before(Dialog.this)) return;
				clearActionMessages();
				try {
					if(handlerOk.handle(Dialog.this,ButtonType.Ok)) close();
				} finally {
					handlerOk.after(Dialog.this);
				}
			}
		});

		ctrl.getBtnCancel().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(handlerOk.handle(Dialog.this,ButtonType.Cancel)) close();
			}
		});
		return ctrl;
	}
	
	public void close() {
		// bug javafx: акселератор не проверяет видимость
		((DialogController)getController()).getBtnOk().setDisable(true);
		((DialogController)getController()).getBtnCancel().setDisable(true);
		super.close();
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
			((DialogController) getController()).getBtnOk().setGraphic(indicator);
		} else
			((DialogController) getController()).getBtnOk().setGraphic(null);
	}

}
