package application;

import java.util.logging.Level;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.bebrb.client.FormController;
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
			Main.log.log(Level.SEVERE, e.getMessage(), e);
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
