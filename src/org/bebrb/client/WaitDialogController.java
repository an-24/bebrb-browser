package org.bebrb.client;

import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;


public class WaitDialogController {
    @FXML
    private Button btnCancel;

    @FXML
    private ProgressBar pbProgress;

    @FXML
    private AnchorPane root;

    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			org.bebrb.client.utils.Logger.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
    }

	public Button getBtnCancel() {
		return btnCancel;
	}

	public ProgressBar getProgress() {
		return pbProgress;
	}

	public AnchorPane getRoot() {
		return root;
	}

}
