package application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.bebrb.client.Client;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.controls.PaneControl;
import org.bebrb.client.utils.DataFilter;
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
import application.NavigateStack.CommandPoint;
import application.TabInnerController.DomainInfo;
import application.TabInnerController.Host;
import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
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
	private boolean defaultExpanded;

	
	private static Color clCellBlue = Color.web("blue", 0.7);
	private static Color clCellWhite = Color.web("white", 0.36);
	
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
		ctrlHome.getSearchField().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				filterDataSources(ctrlHome.getSearchField().getText());
			}
		});
		ctrlHome.getTreeData().setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                TreeCell<String> cell=new TreeCell<String>(){
                	@Override
					public void updateItem(String s, boolean empty) {
                		super.updateItem(s, empty);
                		setText(s!=null?s:"");
                		if(!empty) {
                			NodeData data = ((TreeItemData) getTreeItem()).nodeData;
                			// icon
                			if(data.ntype==NodeType.DSItem || data.ntype==NodeType.RefItem) {
                				Circle circle = new Circle(4, clCellBlue);
                	            circle.setStrokeType(StrokeType.OUTSIDE);
                	            circle.setStroke(clCellWhite);
                	            circle.setStrokeWidth(2);				
                				setGraphic(circle);
                			};
                			//color
                			if(data.ntype==NodeType.DSItem || data.ntype==NodeType.RefItem || data.ntype==NodeType.ViewItem) {
                				getStyleClass().add("tree-linkcell");
                				if(data.ntype==NodeType.ViewItem) {
                					setStyle("-fx-padding: 3 3 3 15;");
                				}
                			}
                			
                		}
                	}
                };
                return cell;
            }
        });
		pagectrl.getPages().add(ctrlHome.getRoot());

		pagectrl.getPages().add(null);
		pagectrl.getPages().add(null);
		pagectrl.getPages().add(null);
		
	}


	private void filterDataSources(String text) {
		tree.setRoot(null); //clean
		defaultExpanded = !text.isEmpty();
		final String lowtext = text.toLowerCase();
		if(text.isEmpty()) fillTreeData();else {
			fillTreeData(new DataFilter<ApplicationWorkspaceController.TreeItemData>() {
				@Override
				public boolean filter(TreeItemData record) {
					return record.getValue().toLowerCase().contains(lowtext);
				}
			});
		}
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
		setActivePage(0);
	}

	protected void choiceDataPage() {
		setActivePage(1);
	}

	protected void choiceHistoryPage() {
		setActivePage(2);
	}
	
	protected void choiceSettingsPage() {
		setActivePage(3);
	}

	private void setActivePage(int idx) {
		// push save point
		CommandPoint savepoint = new CommandPoint() {
			Pane backpane = pagectrl.getActivePage();
			@Override
			public void restore() {
				pagectrl.setActivePage(backpane);
			}
		};
		// push
		tabController.getHistory().push(savepoint);
		tabController.lockControl();
		pagectrl.setActivePage(idx);
	}
	
	private void fillTreeData(DataFilter<TreeItemData> filter) {
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
			fillDataSourceBranch(list, item, filter);
		
		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("HomeTreeData.ds")));
		root.getChildren().add(item);
		list = appContext.getDataSources();
		if(list!=null) 
			fillDataSourceBranch(list, item, filter);
		
		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("HomeTreeData.dict")));
		root.getChildren().add(item);
		List<Reference> rlist = appContext.getReferences();
		if(list!=null) 
			fillReferenceBranch(rlist, item, filter);
		
		root.setExpanded(true);
	}

	private void fillTreeData() {
		fillTreeData(null);
	}

	private void fillReferenceBranch(List<Reference> rlist, TreeItemData toItem, DataFilter<TreeItemData> filter) {
		Collections.sort(rlist,new Comparator<Reference>() {
			@Override
			public int compare(Reference o1, Reference o2) {
				return o1.getMetaData().getName().compareTo(o2.getMetaData().getName());
			}
		});
		for (Reference ds : rlist) {
			TreeItemData itm = new TreeItemData(new NodeData(NodeType.RefItem, ds.getMetaData().getName(),ds));
			if(filter==null || filter.filter(itm)) {
				toItem.getChildren().add(itm);
				fillViewBranch(ds.getViews().values(),itm);
			}
		}
	}

	private void fillDataSourceBranch(List<DataSource> list, TreeItemData toItem, DataFilter<TreeItemData> filter) {
		Collections.sort(list,new Comparator<DataSource>() {
			@Override
			public int compare(DataSource o1, DataSource o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (DataSource ds : list) {
			TreeItemData itm = new TreeItemData(new NodeData(NodeType.DSItem, ds.getName(),ds));
			if(filter==null || filter.filter(itm)) {
				toItem.getChildren().add(itm);
			}
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
			setExpanded(defaultExpanded);
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
