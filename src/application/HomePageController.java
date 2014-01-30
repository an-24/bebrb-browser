package application;

import java.util.logging.Level;

import org.bebrb.client.utils.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;


public class HomePageController {
    @FXML
    private Pane root;
    @FXML
    private TextField tfSearch;
    @FXML
    private TreeView<String> tvData;


    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
    }


	public Pane getRoot() {
		return root;
	}


	public TextField getSearchField() {
		return tfSearch;
	}


	public TreeView<String> getTreeData() {
		return tvData;
	}

}
