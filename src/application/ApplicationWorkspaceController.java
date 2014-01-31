package application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.bebrb.client.Client;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.MessageDialog;
import org.bebrb.client.MessageDialog.Type;
import org.bebrb.client.controls.PaneControl;
import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandGetAppContext;
import org.bebrb.server.net.CommandGetAppContext.DataSource;
import org.bebrb.server.net.CommandGetAppContext.Reference;
import org.bebrb.server.net.CommandGetAppContext.Response;
import org.bebrb.server.net.CommandGetAppContext.View;
import org.bebrb.server.net.CommandLogin;
import org.bebrb.server.net.CommandLogin.SessionInfo;

import application.ApplicationWorkspaceController.NodeData.NodeType;
import application.TabInnerController.DomainInfo;
import application.TabInnerController.Host;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;


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

	private TreeView<String> tree;



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

	public Node getRoot() {
		return root;
	}

	public void setTabController(TabInnerController tabController) {
		this.tabController = tabController;
	}


	public void setup(String user, CommandLogin.Response response) throws URISyntaxException, IOException {
		session = response.getSession();
		this.currentUser = user;
		currentHost = tabController.getHost();
		callGetAppContext();
		
		pagectrl = new PaneControl();
		root.setCenter(pagectrl);
		
		// home
		ctrlHome = Main.loadNodeController("HomePage.fxml");
		pagectrl.getPages().add(ctrlHome.getRoot());

		pagectrl.getPages().add(null);
		pagectrl.getPages().add(null);
		pagectrl.getPages().add(null);
		
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
	
	private void choiceHome() {
		fillTreeData();
		pagectrl.setActivePage(0);
	}


	protected void choiceDataPage() {
		pagectrl.setActivePage(1);
	}

	protected void choiceHistoryPage() {
		pagectrl.setActivePage(2);
	}
	
	protected void choiceSettingsPage() {
		pagectrl.setActivePage(3);
	}


	private void fillTreeData() {
		tree = ctrlHome.getTreeData();
		// clean tree
		if(tree.getRoot()!=null) return;
		
		// root
		tree.setRoot(new TreeItemData(new NodeData(NodeType.RootFolder,Main.getStrings().getString("HomeTreeData.root"))));
		
		// root folders
		TreeItemData item;
		List<DataSource> list;
		TreeItemData root = (TreeItemData) tree.getRoot();

		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("HomeTreeData.docs")));
		root.getChildren().add(item);
		list = appContext.getDocs();
		if(list!=null) 
			fillDataSourceBranch(list, item);
		
		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("HomeTreeData.ds")));
		root.getChildren().add(item);
		list = appContext.getDataSources();
		if(list!=null) 
			fillDataSourceBranch(list, item);
		
		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("HomeTreeData.dict")));
		root.getChildren().add(item);
		List<Reference> rlist = appContext.getReferences();
		if(list!=null) 
			fillReferenceBranch(rlist, item);
		
		root.setExpanded(true);
	}

	private void fillReferenceBranch(List<Reference> rlist, TreeItemData toItem) {
		Collections.sort(rlist,new Comparator<Reference>() {
			@Override
			public int compare(Reference o1, Reference o2) {
				return o1.getMetaData().getName().compareTo(o2.getMetaData().getName());
			}
		});
		for (Reference ds : rlist) {
			TreeItemData itm = new TreeItemData(new NodeData(NodeType.RefItem, ds.getMetaData().getName(),ds));
			toItem.getChildren().add(itm);
			fillViewBranch(ds.getViews().values(),itm);
		}
	}

	private void fillDataSourceBranch(List<DataSource> list, TreeItemData toItem) {
		Collections.sort(list,new Comparator<DataSource>() {
			@Override
			public int compare(DataSource o1, DataSource o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (DataSource ds : list) {
			TreeItemData itm = new TreeItemData(new NodeData(NodeType.DSItem, ds.getName(),ds));
			toItem.getChildren().add(itm);
		}
	}

	private void fillViewBranch(Collection<View> vlist, TreeItemData toItem) {
		List<View> list = new ArrayList<>();
		list.addAll(vlist);
		Collections.sort(list,new Comparator<View>() {
			@Override
			public int compare(View o1, View o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
		});
		for (View view : list) {
			TreeItemData itm = new TreeItemData(new NodeData(NodeType.ViewItem, view.getTitle(),view));
			toItem.getChildren().add(itm);
		}
	}
	
	class TreeItemData extends TreeItem<String> {
		private NodeData nodeData;

		public TreeItemData(NodeData data) {
			super(data.text);
			this.nodeData = data;
		}

		
	} 


	static public class NodeData {
		static public enum NodeType {RootFolder, DocFolder, DSFolder, DictFolder, DSItem,RefItem,ViewItem};
		
		NodeType ntype;
		String text;
		Object data;

		public NodeData(NodeType nt, String txt) {
			this(nt,txt,null);
		}
		
		public NodeData(NodeType nt, String txt, Object data) {
			text = txt;
			ntype = nt;
			this.data = data;
		}

	}

}
