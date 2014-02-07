package org.bebrb.client;

import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DialogController extends FormController {

	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;
	@FXML
	private AnchorPane apContent;
	@FXML
	private VBox errorBox;
	@FXML
	private Pane root;
	
	
	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			org.bebrb.client.utils.Logger.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public Pane getRoot() {
		return root;
	}

	public Button getBtnOk() {
		return btnOk;
	}

	public Button getBtnCancel() {
		return btnCancel;
	}

	public VBox getErrorBox() {
		return errorBox;
	}

	@Override
	public AnchorPane getContent() {
		return apContent;
	}
}
