package application;

import java.util.logging.Level;

import org.bebrb.client.FormController;
import org.bebrb.client.utils.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DialogInfoController extends FormController {
	    @FXML
	    private HBox apContent;
	    @FXML
	    private Button btnOk;
	    @FXML
	    private VBox root;


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

}
