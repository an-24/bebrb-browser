package application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.bebrb.server.net.CommandLogin;

import utils.LocaleUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;


public class ApplicationWorkspaceController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane root;

	private TabInnerController tabController;


    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}


    }


	public Node getRoot() {
		return root;
	}


	public void setTabController(TabInnerController tabController) {
		this.tabController = tabController;
	}


	public void setup(CommandLogin.Response response) {
		// TODO Auto-generated method stub
		
	}

}
