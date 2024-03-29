package application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.bebrb.client.Client;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.Host;
import org.bebrb.client.MessageDialog;
import org.bebrb.client.controls.PaneControl;
import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandGetAppContext;
import org.bebrb.server.net.CommandGetAppContext.DataSource;
import org.bebrb.server.net.CommandGetAppContext.Response;
import org.bebrb.server.net.CommandGetAppContext.View;
import org.bebrb.server.net.CommandLogin;
import org.bebrb.server.net.CommandLogin.SessionInfo;

import application.NavigateStack.CommandPoint;
import application.TabInnerController.DomainInfo;


public class ApplicationWorkspaceController {
	@FXML
    private BorderPane root;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnR;
    @FXML
    private Button btnSettings;

	private TabInnerController tabController;
	private SessionInfo session;
	private Client query;

	private Host currentHost;
	private String currentUser;
	private CommandGetAppContext.Response appContext;

	private PaneControl pagectrl;
	private HomePageController ctrlHome;
	private DataSourcePageController ctrlDataSource;
	
	private DataSource activeDataSource = null;
	private View activeReferenceBook = null;

	private int lockHistory = 0;
	
    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		btnHome.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				choiceHome();
			}
		});
		btnR.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				choiceDataPage();
			}
		});
		btnHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				choiceHistoryPage();
			}
		});
		btnSettings.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				choiceSettingsPage();
			}
		});
    }

	public Pane getRoot() {
		return root;
	}

	public void setTabController(TabInnerController tabController) {
		this.tabController = tabController;
	}


	public void setup(String user, CommandLogin.Response response) throws URISyntaxException, IOException {
		session = response.getSession();
		this.currentUser = user;
		currentHost = tabController.getHost();
		pagectrl = new PaneControl();
		root.setCenter(pagectrl);
		
		// home
		ctrlHome = Main.loadNodeController("HomePage.fxml");
		ctrlHome.setOwner(this);
		pagectrl.getPages().add(ctrlHome.getRoot());
		
		ctrlDataSource = Main.loadNodeController("DataSourcePage.fxml");
		ctrlDataSource.setOwner(this);
		pagectrl.getPages().add(ctrlDataSource.getRoot());
		
		pagectrl.getPages().add(null);
		pagectrl.getPages().add(null);
		
		callGetAppContext();
	}

	private void callGetAppContext() throws URISyntaxException {
		btnHome.setDisable(true);
		query = new Client(currentHost.domain, currentHost.port,
				new OnResponse() {
			@Override
			public void replyСame(String message) throws Exception {
				try {
					// parse
					CommandGetAppContext.Response response = CommandFactory
							.createGson()
							.fromJson(message,CommandGetAppContext.Response.class);
					if (response.getStatus() != Command.OK) {
						Main.log.log(Level.SEVERE,
								response.getMessage()+ " detail:"+ response.getTrace());
						throw new Exception(
								String.format(Main.getStrings().getString("ex-OnServerError"),
										response.getMessage()));
					} else
						Main.log.log(Level.INFO,
								"response:" + message);
					// handle
					handleAppContext(response);
				} finally {
					// finish
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							tabController.ready();
						}
					});
				}
			}
		}, tabController.getErrorHandler());
		tabController.waiting();
		try {
			query.send(new CommandGetAppContext(session.getId()));
		} finally {
			query = null;
		}
	}


	private void handleAppContext(final Response response) throws URISyntaxException {
		appContext = response;
		btnHome.setDisable(false);
		String location = currentHost.toString()+"/"+response.getName()+"/"+session.getVersion();
		final DomainInfo di = tabController.newDomainInfo(TabInnerController.getHostFromString(location));
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				di.setTitle(response.getTitle());
				tabController.startApplication(di);
			}
		});
	}


	public SessionInfo getSession() {
		return session;
	}


	public Host getHost() {
		return currentHost;
	}


	public Response getAppContext() {
		return appContext;
	}


	public String getCurrentUser() {
		return currentUser;
	}


	public void embedded(Pane pane) {
		pane.getChildren().add(root);
		AnchorPane.setBottomAnchor(root,0D);
		AnchorPane.setTopAnchor(root,0D);
		AnchorPane.setLeftAnchor(root,0D);
		AnchorPane.setRightAnchor(root,0D);
	}

	protected void choiceDefault() {
		pagectrl.setActivePage(-1);
		activeDataSource = null;
		activeReferenceBook = null;
	}
	
	protected void choiceHome() {
		ctrlHome.fillTreeData();
		setActivePage(0);
	}
	
	protected void choiceDataPage() {
		if(isAccessDataSet()) 
			pagectrl.getPages().set(1, ctrlDataSource.getRoot());else
			pagectrl.getPages().set(1, null);
		setActivePage(1);
	}

	protected void choiceHistoryPage() {
		setActivePage(2);
	}
	
	protected void choiceSettingsPage() {
		setActivePage(3);
	}

	public boolean isAccessDataSet() {
		return (activeDataSource != null || activeReferenceBook != null); 
	}


	private void setActivePage(int idx) {
		pagectrl.setActivePage(idx);
		// push save point
		if(lockHistory==0) {
			CommandPoint savepoint = new CommandPoint() {
				final Pane backpane = pagectrl.getActivePage();
				final CommandGetAppContext.DataSource ds = activeDataSource;
				final CommandGetAppContext.View ref = activeReferenceBook;
				@Override
				public void restore() {
					lockHistory++;
					pagectrl.setActivePage(backpane);
					lockHistory--;
				}
			};
			// push
			tabController.getHistory().push(savepoint);
		}
		tabController.lockControl();
	}

	public void openDataSource(CommandGetAppContext.DataSource data, boolean newTab) throws Exception {
		if(newTab) {
			// TODO Auto-generated method stub
		}
		activeDataSource = data;
		activeReferenceBook = null;
		ctrlDataSource.setDataSource(data);
		choiceDataPage();
	}

	public void openReferenceView(CommandGetAppContext.View data, boolean newTab) throws Exception{
		if(newTab) {
			// TODO Auto-generated method stub
		}
		activeDataSource = null;
		activeReferenceBook = data;
		ctrlDataSource.setReferenceView(data);
		choiceDataPage();
	}
	
	public void openDataSource(CommandGetAppContext.DataSource data) throws Exception {
		openDataSource(data,false);
	}

	public void openReferenceView(CommandGetAppContext.View data) throws Exception {
		openReferenceView(data,false);
	}

	public void openReferenceView(CommandGetAppContext.Reference ref, boolean newTab) throws Exception {
		CommandGetAppContext.View view = ref.getViews().get(ref.getDefaultView());
		openReferenceView(view,newTab);
	}

	public void showError(Exception e) {
		new MessageDialog((Pane)root.getParent(),MessageDialog.Type.Error,e.getMessage()).show();
	}

	public void showError(String message) {
		new MessageDialog((Pane)root.getParent(),MessageDialog.Type.Error,message).show();
	}
	
}
