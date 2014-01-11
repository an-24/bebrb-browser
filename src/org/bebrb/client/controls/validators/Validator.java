package org.bebrb.client.controls.validators;

import org.bebrb.client.controls.Ballon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public abstract class Validator {

	private Control control;
	private boolean invalidate;
	private String message;
	private Ballon ballon;

	public Validator(Control ctrl, String message) {
		this(ctrl,message,true);
	}
	
	public Validator(Control ctrl, String message, boolean directly) {
		this.control = ctrl;
		this.message = message;
		ballon = new Ballon(message);
		if(directly)
			control.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {
					if(!newValue) {
						action();
					};
				}
			});
		if(directly) 
			control.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					restoreValidateControl();
				}
			});		
		this.control.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(invalidate) {
					ballon.show(control);
				}
				
			}
		});
		this.control.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				ballon.hide();
			}
		});
		this.control.setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if(invalidate) {
					ballon.show(control);
				}
			}
		});
	}
	
	public void action() {
		restoreValidateControl();
		invalidate = !validate(); 
		if(invalidate) 
			showInvalidateControl();
	}

	protected abstract boolean validate();

	protected void showInvalidateControl() {
		control.getStyleClass().add("errorinput");
	}
	
	protected void restoreValidateControl() {
		ballon.hideDirectly();
		int idx = control.getStyleClass().indexOf("errorinput");
		if(idx>=0) { 
			control.getStyleClass().remove(idx);
		}	
	}

	public Control getControl() {
		return control;
	}

	public boolean isInvalidate() {
		return invalidate;
	}
}
