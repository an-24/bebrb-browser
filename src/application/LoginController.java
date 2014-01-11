package application;

import java.util.logging.Level;

import org.bebrb.client.controls.validators.EmptyValidator;
import org.bebrb.client.controls.validators.ValidateController;

import utils.LocaleUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
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
	
	private ValidateController validControl;
	
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
		
		validControl = new ValidateController();
		validControl.add(new EmptyValidator(tfUserName, "Имя пользователя не может быть пустым"));
		validControl.add(new EmptyValidator(tfPasswordOrPin, "Пароль не может быть пустым"));

	}

	public Pane getRoot() {
		return root;
	}

	public ValidateController getValidateControl() {
		return validControl;
	}

	public TextField getUserNameControl() {
		return tfUserName;
	}

	public TextField getPasswordControl() {
		return tfPasswordOrPin;
	}
	
	

}
