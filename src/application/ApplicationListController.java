package application;

import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

public class ApplicationListController {
	@FXML
	private Pane root;
	@FXML
	private Button bOk;
	@FXML
	private TitledPane tpApplications;
	@FXML
	private ListView lvApplications;
	
	
	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public ListView getList() {
		return lvApplications;
	}

	public Pane getRoot() {
		return root;
	}

	public Button getButtonOk() {
		return bOk;
	}

}
