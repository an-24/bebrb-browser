package application;

import java.util.logging.Level;

import utils.LocaleUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class LoginController {
	@FXML
	private Pane root;
	@FXML
	private RadioButton rbPassword;
	@FXML
	private RadioButton rbToken;
	@FXML
	private TextField tfUserName;
	@FXML
	private TextField tfPasswordOrPin;
	@FXML
	private Label lUserName;
	@FXML
	private Label lPasswordOrPin;
	
	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
		ToggleGroup group = new ToggleGroup();
		rbPassword.setToggleGroup(group);
		rbToken.setToggleGroup(group);

	}

	public Pane getRoot() {
		return root;
	}

}
