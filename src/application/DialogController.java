package application;

import java.util.logging.Level;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import utils.LocaleUtils;

public class DialogController {

	@FXML
	private Pane root;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;
	@FXML
	private AnchorPane apContent;
	@FXML
	private VBox errorBox;
	
	
	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
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

	public AnchorPane getContent() {
		return apContent;
	}

	public VBox getErrorBox() {
		return errorBox;
	}

}
