package org.bebrb.client;

import java.io.IOException;
import java.util.logging.Level;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.bebrb.client.utils.LocaleUtils;
import application.DialogConfirmController;
import application.DialogInfoController;
import application.Main;

public class MessageDialog extends CustomDialog {
	
	public enum Type{Info,Confirm,Warning,Error}

	private Type type;
	private Node defFocus;
	protected ButtonType pushedButton;

	public MessageDialog(Pane root, Type t, String message) {
		super(root, new Label(message));
		this.type = t;
		((Label)source).setWrapText(true);
	}
	
	protected FormController loadForm() throws IOException {
		String dlgname = null;
		switch (type) {
		case Info:
			dlgname = "Dialog-Info.fxml";
			break;
		case Confirm:
			dlgname = "Dialog-Confirm.fxml";
			break;
		case Warning:
			dlgname = "Dialog-Warning.fxml";
			break;
		case Error:
			dlgname = "Dialog-Error.fxml";
			break;
		}
		FormController ctrl = Main.loadNodeController(dlgname);
		try {
			LocaleUtils.localeFields(ctrl);
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
		switch (type) {
		case Info:
		case Error:
			((DialogInfoController) ctrl).getBtnOk().setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					pushedButton = ButtonType.Ok;
					close();
				}
			});
			defFocus = ((DialogInfoController) ctrl).getBtnOk();
			break;
		case Confirm:
		case Warning:
			Button btn;
			final EventHandler<ActionEvent> h = new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if(handlerOk==null) {
						close();
						return;
					}
					if(!handlerOk.before(MessageDialog.this)) return;
					try {
						if(handlerOk.handle(MessageDialog.this,pushedButton)) close();
					} finally {
						handlerOk.after(MessageDialog.this);
					}
				}
			}; 
			
			btn = ((DialogConfirmController) ctrl).getBtnOk();
			if(btn!=null)
			btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					pushedButton = ButtonType.Ok;
					h.handle(event);
				}
			});

			btn = ((DialogConfirmController) ctrl).getBtnYes();
			if(btn!=null)
			btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					pushedButton = ButtonType.Yes;
					h.handle(event);
				}
			});

			btn = ((DialogConfirmController) ctrl).getBtnNo();
			if(btn!=null)
			btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					pushedButton = ButtonType.No;
					h.handle(event);
				}
			});
			
			btn = ((DialogConfirmController) ctrl).getBtnCancel();
			btn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					pushedButton = ButtonType.Cancel;
					if(handlerOk==null) {
						close();
						return;
					}
					if(handlerOk.handle(MessageDialog.this,ButtonType.Cancel)) close();
				}
			});
			
			defFocus = ((DialogConfirmController) ctrl).getBtnCancel();
			break;
		}
		return ctrl;
	}

	protected Node getDefaultControl() {
		return defFocus;
	}

}
