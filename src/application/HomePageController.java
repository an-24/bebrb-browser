package application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.bebrb.client.MessageDialog;
import org.bebrb.client.utils.DataFilter;
import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.server.net.CommandGetAppContext.DataSource;
import org.bebrb.server.net.CommandGetAppContext.Reference;
import org.bebrb.server.net.CommandGetAppContext.View;

import com.sun.javafx.scene.control.skin.LabeledText;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.util.Callback;


public class HomePageController {
	static public enum NodeType {RootFolder, DocFolder, DSFolder, DictFolder, DSItem,RefItem,ViewItem};
	
    @FXML
    private Pane root;
    @FXML
    private TextField tfSearch;
    @FXML
    private TreeView<String> tvData;
	
    private boolean defaultExpanded = false;
	private ApplicationWorkspaceController appController;

	private static Color clCellBlue = Color.web("blue", 0.7);
	private static Color clCellWhite = Color.web("white", 0.36);

    @FXML
    void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		tfSearch.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				filterDataSources(tfSearch.getText());
			}
		});
		
		tvData.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                TreeCell<String> cell=new TreeCell<String>(){
					@Override
					public void updateItem(String s, boolean empty) {
                		super.updateItem(s, empty);
                		setText(s!=null?s:"");
                		setGraphic(null);
                		if(!empty) {
                			final NodeData data = ((TreeItemData) getTreeItem()).nodeData;
                			
                			switch (data.ntype) {
								case DocFolder:
									setGraphic(new ImageView(new Image("application/images/documents.png",26,26,false,false)));
									break;
								case DSFolder:
									setGraphic(new ImageView(new Image("application/images/data.png",26,26,false,false)));
									break;
								case DictFolder:
									
									setGraphic(new ImageView(new Image("application/images/refs.png",26,26,false,false)));
									break;
								default:
									break;
							}
                			// icon
                			if(data.ntype==NodeType.DSItem || data.ntype==NodeType.RefItem) {
                				Circle circle = new Circle(4, clCellBlue);
                	            circle.setStrokeType(StrokeType.OUTSIDE);
                	            circle.setStroke(clCellWhite);
                	            circle.setStrokeWidth(2);				
                				setGraphic(circle);
                			};
                			//color & link
                			if(data.ntype==NodeType.DSItem || data.ntype==NodeType.RefItem || data.ntype==NodeType.ViewItem) {
                				getStyleClass().add("tree-linkcell");
                				if(data.ntype==NodeType.ViewItem) {
                					setStyle("-fx-padding: 3 3 3 15;");
                				}
                				// FIXME
                				// нужно чтобы рука была только на label, но
                				// нет могу найти LabeledText в Cell
                				setOnMouseMoved(new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent event) {
										if(event.getTarget() instanceof LabeledText) {
											((LabeledText)event.getTarget()).setCursor(Cursor.HAND);
										}
									}
                				});
                				setOnMouseClicked(new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent event) {
										if(event.getTarget() instanceof LabeledText) {
											open(data,false);
										}
									}
								});
                				// context menu
                				ContextMenu ctxmenu = new ContextMenu();
                				MenuItem item;
                				
                				item = new MenuItem(Main.getStrings().getString("homeTreeData-menu-1"));
                				item.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										open(data,false);
									}
								});
                				ctxmenu.getItems().add(item);
                				
                				item = new MenuItem(Main.getStrings().getString("homeTreeData-menu-2"));
                				item.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										open(data, true);
									}
								});
                				ctxmenu.getItems().add(item);
                				
                				ctxmenu.getItems().add(new SeparatorMenuItem());
                				
                				item = new MenuItem(Main.getStrings().getString("homeTreeData-menu-3"));
                				item.setOnAction(new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										//TODO
									}
								});
                				ctxmenu.getItems().add(item);
                				
                				setContextMenu(ctxmenu);
                			}
                			
                		}
                	}
                };
                return cell;
            }
        });
		
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

	public boolean isDefaultExpanded() {
		return defaultExpanded;
	}


	public void setDefaultExpanded(boolean defaultExpanded) {
		this.defaultExpanded = defaultExpanded;
	}
	
	public void fillTreeData(DataFilter<TreeItemData> filter) {
		// clean tree
		if(tvData.getRoot()!=null) return;
		
		// root
		tvData.setRoot(new TreeItemData(new NodeData(NodeType.RootFolder,Main.getStrings().getString("homeTreeData.root"))));
		
		// root folders
		TreeItemData item;
		List<DataSource> list;
		TreeItemData root = (TreeItemData) tvData.getRoot();

		item = new TreeItemData(new NodeData(NodeType.DocFolder, Main.getStrings().getString("homeTreeData.docs")));
		root.getChildren().add(item);
		list = appController.getAppContext().getDocs();
		if(list!=null) 
			fillDataSourceBranch(list, item, filter);
		
		item = new TreeItemData(new NodeData(NodeType.DSFolder, Main.getStrings().getString("homeTreeData.ds")));
		root.getChildren().add(item);
		list = appController.getAppContext().getDataSources();
		if(list!=null) 
			fillDataSourceBranch(list, item, filter);
		
		item = new TreeItemData(new NodeData(NodeType.DictFolder, Main.getStrings().getString("homeTreeData.dict")));
		root.getChildren().add(item);
		List<Reference> rlist = appController.getAppContext().getReferences();
		if(list!=null) 
			fillReferenceBranch(rlist, item, filter);
		
		root.setExpanded(true);
	}

	public void fillTreeData() {
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

	private void filterDataSources(String text) {
		tvData.setRoot(null); //clean
		defaultExpanded = !text.isEmpty();
		final String lowtext = text.toLowerCase();
		if(text.isEmpty()) fillTreeData();else {
			fillTreeData(new DataFilter<TreeItemData>() {
				@Override
				public boolean filter(TreeItemData record) {
					return record.getValue().toLowerCase().contains(lowtext);
				}
			});
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


	public void setOwner(
			ApplicationWorkspaceController appWorkspaceController) {
		this.appController = appWorkspaceController;
	}


	public void open(final NodeData data, boolean newTab) {
		try {
			switch (data.ntype) {
				case DSItem:
					appController.openDataSource((DataSource)data.data,newTab);
					break;
				case ViewItem:
					appController.openReferenceView((View)data.data,newTab);
					break;
				case RefItem:
					appController.openReferenceView((Reference)data.data,newTab);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
			appController.showError(e);
		}
	}


	
}
