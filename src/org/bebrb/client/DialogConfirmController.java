package org.bebrb.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import org.bebrb.client.utils.LocaleUtils;


public class DialogConfirmController extends FormController {

	@FXML
	private Button btnOk;
	@FXML
	private Button btnYes;
	@FXML
	private Button btnNo;
	@FXML
	private Button btnCancel;
	@FXML
	private Pane apContent;
	@FXML
	private Pane root;
	
	
	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Logger.getLogger("bebrb").log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public Pane getRoot() {
		return root;
	}

	@Override
	public Pane getContent() {
		return apContent;
	}

	public Button getBtnOk() {
		return btnOk;
	}

	public Button getBtnYes() {
		return btnYes;
	}

	public Button getBtnNo() {
		return btnNo;
	}

	public Button getBtnCancel() {
		return btnCancel;
	}

}
