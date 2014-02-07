package application;

import java.util.Map;
import java.util.logging.Level;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

import org.bebrb.client.Client.EmptyBodyException;
import org.bebrb.client.controls.DataGrid;
import org.bebrb.client.data.DataSourceImpl;
import org.bebrb.client.data.ViewImpl;
import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.client.utils.Resources;
import org.bebrb.data.DataSource;
import org.bebrb.data.DataSource.OnOpen;
import org.bebrb.server.net.CommandGetAppContext;


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
    private TitledPane tpHeader;

    @FXML
    private DataGrid tvData;

	private ApplicationWorkspaceController appController;

	private DataSource dataSource;

	private Map<String, Object> params;

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


	public void setDataSource(CommandGetAppContext.DataSource data) throws Exception {
		tpHeader.setText(data.getName());
		// new datasource
		dataSource = DataSourceImpl.createDataSet(appController.getHost(), appController.getSession().getId(), data);
		// lock function
		btnAdd.setDisable(!data.getCanAdd());
		btnEdit.setDisable(!data.getCanEdit());
		btnDelete.setDisable(!data.getCanDelete());
		//close for safe resource
		if(tvData.getDataSet()!=null) tvData.getDataSet().close();
		// open
		tvData.setDataSet(dataSource);
		if(!dataSource.isOpen()) 
			dataSource.open(params, new OnOpen() {
				
				@Override
				public void onAfterOpen() {
				}
				
				@Override
				public void onError(Exception e) {
					appController.showError(e);
				}
			});
	}


	public void setReferenceView(CommandGetAppContext.View data) throws Exception {
		tpHeader.setText(data.getReferenceBook().getMetaData().getReferenceTitle()+":"+data.getTitle());
		// new datasource
		dataSource = ViewImpl.createView(appController.getHost(), appController.getSession().getId(),data).getDataSource();
		// lock function
		btnAdd.setDisable(!data.getReferenceBook().getCanAdd());
		btnEdit.setDisable(!data.getReferenceBook().getCanEdit());
		btnDelete.setDisable(!data.getReferenceBook().getCanDelete());
		// open
		if(!dataSource.isOpen()) 
			dataSource.open(params, new OnOpen() {
				
				@Override
				public void onAfterOpen() {
					tvData.setDataSet(dataSource);
				}
				
				@Override
				public void onError(Exception e) {
					if(e instanceof EmptyBodyException)
						 appController.showError(Resources.getBungles().getString("ex-ServerUnknownResponse"));
					else appController.showError(e);
				}
			});
		
		
	}

}
