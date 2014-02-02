package application;

import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.data.Record;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;


public class DataSourcePageController {

    @FXML
    private Pane root;
	
    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEdit;

    @FXML
    private MenuButton btnMore;

    @FXML
    private TitledPane tpFilter;

    @FXML
    private TableView<Record> tvData;

	private ApplicationWorkspaceController appController;

    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}

    }


	public void setOwner(
			ApplicationWorkspaceController appWorkspace) {
		appController = appWorkspace;
	}


	public Pane getRoot() {
		return root;
	}

}
