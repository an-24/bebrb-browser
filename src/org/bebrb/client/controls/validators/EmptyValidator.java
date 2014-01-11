package org.bebrb.client.controls.validators;

import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

public class EmptyValidator extends Validator {

	public EmptyValidator(Control ctrl, String message) {
		super(ctrl, message);
	}
	
	public EmptyValidator(Control ctrl, String message, boolean directly) {
		super(ctrl, message, directly);
	}

	@Override
	protected boolean validate() {
		return !((TextInputControl)getControl()).getText().isEmpty();
	}

}
