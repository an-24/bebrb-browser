package application;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import org.bebrb.client.Client;
import org.bebrb.client.Client.EmptyBodyException;
import org.bebrb.client.Client.ExecException;
import org.bebrb.client.Client.OnError;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.CustomDialog;
import org.bebrb.client.CustomDialog.ButtonType;
import org.bebrb.client.CustomDialog.DialogResult;
import org.bebrb.client.Dialog;
import org.bebrb.client.controls.InfoBox;
import org.bebrb.client.controls.SuggestBox;
import org.bebrb.client.controls.SuggestBox.CellFactory;
import org.bebrb.client.controls.SuggestBox.FilterItems;
import org.bebrb.client.utils.DomainProperties;
import org.bebrb.client.utils.LocalStore;
import org.bebrb.client.utils.LocaleUtils;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandHello;
import org.bebrb.server.net.CommandHello.AppInfo;
import org.bebrb.server.net.CommandLogin;
import org.bebrb.server.net.CommandLogout;

import application.NavigateStack.CommandPoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class TabInnerController {
	@FXML
	private BorderPane root;
	@FXML
	private SuggestBox<DomainInfo> comboUri;
	@FXML
	private Button btnBack;
	@FXML
	private Button btnNext;
	@FXML
	private MenuButton btnMenu;

	private Tab owner;
	private Client query;
	private ApplicationWorkspaceController appWorkspace;

	private NavigateStack navStack = new NavigateStack(new Callback<Void, Void>() {
		@Override
		public Void call(Void arg0) {
			reset();
			return null;
		}
	});
	private DomainInfo currentLocation = null;
	private int waitCount;

	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		btnMenu.getStyleClass().clear();
		btnMenu.getStyleClass().add("m-button");
		// main menu
		MenuItem item;

		// disconnect
		item = new MenuItem("[dyn]");
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evn) {
				stopApplication();
				reset();
			}
		});
		final MenuItem mnuDisconnect = item;
		btnMenu.getItems().add(item);
		
		
		btnMenu.getItems().add(new SeparatorMenuItem());
		// exit
		item = new MenuItem(Main.getStrings().getString("menu-Exit"));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evn) {
				Main.exit();
			}
		});
		btnMenu.getItems().add(item);
		
		// menu show event 
		btnMenu.showingProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if(newValue) {
	            	mnuDisconnect.setDisable(appWorkspace==null);
	            	String mname = Main.getStrings().getString("menu-Disconnect");
	            	if(appWorkspace!=null) {
	            		mname += "..."+appWorkspace.getCurrentUser();
	            	}
	            	mnuDisconnect.setText(mname);
	            }
	        }
	    });
		
		
		btnBack.setGraphic(new ImageView(new Image(ClassLoader.getSystemResourceAsStream("application/images/back.png"))));
		btnBack.setDisable(true);
		btnBack.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				navStack.back();
				lockControl();
			}
		});

		btnNext.setGraphic(new ImageView(new Image(ClassLoader.getSystemResourceAsStream("application/images/next.png"))));
		btnNext.setDisable(true);
		btnNext.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				navStack.next();
				lockControl();
			}
		});

		setWelcomePage();
		
		comboUri.setItems(loadDomainHistory());
		
		comboUri.setOnFilterItem(new FilterItems<TabInnerController.DomainInfo>() {

			@Override
			public List<DomainInfo> filter(List<DomainInfo> source,
					String inputtext) {
				Collections.sort(source,new Comparator<DomainInfo>() {
					@Override
					public int compare(DomainInfo o1, DomainInfo o2) {
						if(o1.saveDate==null && o2.saveDate!=null)
							return 1; else
						if(o1.saveDate!=null && o2.saveDate==null)
							return -1; else
						if(o1.saveDate==null && o2.saveDate==null)
							return 0;
						return o1.saveDate.before(o2.saveDate)?1:
							   o1.saveDate.after(o2.saveDate)?-1:0;
					}
				});
				List<DomainInfo> list = new ArrayList<TabInnerController.DomainInfo>();
				inputtext = inputtext.toLowerCase();
				for (DomainInfo di : source) {
					if(di.host!=null)
					if((di.host.domain!=null && di.host.domain.toLowerCase().contains(inputtext)) ||
					   (di.host.path!=null && di.host.path.toLowerCase().contains(inputtext)) ||
					   (di.title!=null && di.title.toLowerCase().contains(inputtext))) {
						list.add(di);
						if(list.size()>5)
							return list;
					}
				}
				if(list.size()==0) return null;
				return list;
			}
		});
		
		comboUri.setCellFactory(new CellFactory<TabInnerController.DomainInfo>() {
			@Override
			public Node create(DomainInfo item) {
				HBox hb = new HBox();
				hb.setSpacing(5);
				hb.setPadding(new Insets(5));
				VBox vb = new VBox();
				vb.setSpacing(5);
				vb.getChildren().add(new Label(item.toString()));
				vb.getChildren().add(new Label(item.title.isEmpty()?"Меню":item.title));
				ImageView iv = new ImageView();
				String fname;
				if(item.favorite) fname = "application/images/selected-star.png";
							else  fname = "application/images/unselected-star.png";
				iv.setImage(new Image(fname));
				hb.setAlignment(Pos.CENTER_LEFT);
				hb.getChildren().add(iv);
				hb.getChildren().add(vb);
				return hb;
			}
		});
		
		comboUri.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				interrupt();
				try {
					navigate(getHost());
				} catch (URISyntaxException e) {
					printError(Main.getStrings().getString("ex-URISyntaxError"));
				}
			}
		});

	}

	protected void jump(DomainInfo value, final Callback<Host, Void> callback) {
		setLocation(value);
		// push save point
		CommandPoint savepoint = new CommandPoint() {
			DomainInfo location = currentLocation;

			@Override
			public void restore() {
				if(location!=null) {
					comboUri.setValue(location);
					callback.call(location.host);
					setLocation(location);
				} else {
					reset();
				}	
			}
		};
		// push
		navStack.push(savepoint);
		callback.call(currentLocation.host);
		lockControl();
	}

	protected int findDomain(DomainInfo target) {
		List<DomainInfo> items = comboUri.getItems();
		int i =0;
		for (DomainInfo dinf : items) {
			if(dinf.equals(target))
				return i;
			i++;
		}
		return -1;
	}

	protected boolean findDomain(List<DomainInfo> items, DomainInfo target) {
		for (DomainInfo dinf : items) {
			if(dinf.equals(target))
				return true;
		}
		return false;
	}
	
	private void saveDomainHistory() {
		try {
			File f = LocalStore.openStore();
			f = new File(f,"domain.history");
			if(!f.exists()) f.createNewFile();
			Gson gson = CommandFactory.createGson();
			String text = gson.toJson(comboUri.getItems());
			LocalStore.writeTextFile(f,text);
		} catch (IOException e) {
			Main.log.log(Level.SEVERE, e.getCause().getMessage(),
					e.getCause());
		}
		
	} 

	private List<DomainInfo> loadDomainHistory() {
		List<DomainInfo> list = new ArrayList<TabInnerController.DomainInfo>();
		try {
			File f = LocalStore.openStore();
			f = new File(f,"domain.history");
			if(!f.exists()) f.createNewFile();
			String text = LocalStore.readTextFile(f);
			Gson gson = CommandFactory.createGson();
			JsonArray array = gson.fromJson(text, JsonArray.class);
			if(array!=null)
				for (int i = 0; i < array.size(); i++) {
					DomainInfo dm = gson.fromJson(array.get(i), DomainInfo.class);
					if(dm.host==null || dm.host.domain==null) continue;
					if(!findDomain(list,dm)) list.add(dm); 
				}
		} catch (IOException e) {
			Main.log.log(Level.SEVERE, e.getCause().getMessage(),
					e.getCause());
		}
		return list;
	}

	@Override
	protected void finalize() throws Throwable {
		interrupt();
		super.finalize();
	}

	private void interrupt() {
		if (query != null) {
			ready();
			query.interrupt();
		}

	}

	private void printError(String err) {
		HBox box = new InfoBox();
		box.getStyleClass().add("errorbox");
		Label l = new Label(err);
		l.setTextAlignment(TextAlignment.CENTER);
		l.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setWrapText(true);
		box.getChildren().add(l);
		box.setAlignment(Pos.CENTER);
		box.setMaxSize(root.getScene().getWidth() * 2D / 3,
				Region.USE_PREF_SIZE);
		// box.setOpacity(0.85);
		root.setCenter(box);
	}

	private void printInfo(String err) {
		HBox box = new InfoBox();
		box.getStyleClass().add("infobox");
		Label l = new Label(err);
		l.setTextAlignment(TextAlignment.CENTER);
		l.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		l.setWrapText(true);
		box.getChildren().add(l);
		box.setAlignment(Pos.CENTER);
		box.setMaxSize(root.getScene().getWidth() * 2D / 3,
				Region.USE_PREF_SIZE);
		// box.setOpacity(0.85);
		root.setCenter(box);
	}

	private void setProgress(final boolean b) {
		if(Main.getFXThread()!=Thread.currentThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setProgressInner(b);
				}
			});
			
		} else 
			setProgressInner(b);
	}
		
	private void setProgressInner(boolean b) {
		if (b) {
			ProgressIndicator indicator = new ProgressIndicator();
			indicator.setMinSize(16, 16);
			indicator.setPrefSize(16, 16);
			owner.setGraphic(indicator);
		} else
			owner.setGraphic(null);
	}

	public Tab getOwner() {
		return owner;
	}

	public void setOwner(Tab owner) {
		this.owner = owner;
	}

	/**
	 * ========================================================================
	 * ============================ ============ CommandLogin
	 * ====================
	 * ======================================================
	 * ==========================
	 * @param user 
	 */
	private void handleLogin(String user, final CommandLogin.Response response)
			throws Exception {
		final ApplicationWorkspaceController ctrl = Main.loadNodeController("ApplicationWorkspace.fxml");
		ctrl.setTabController(this);
		ctrl.setup(user,response);
		appWorkspace = ctrl;
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cleanScreen();
				ctrl.embedded((Pane) root.getCenter());
			}
		});
		navStack.clear();
		lockControl();
	}

	/**
	 * ========================================================================
	 * ============================ ============ CommandHello
	 * ====================
	 * ======================================================
	 * ==========================
	 */
	private void handleHello(final CommandHello.Response response)
			throws Exception {
		final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		final ApplicationListController ctrl = Main
				.loadNodeController("ApplicationList.fxml");

		@SuppressWarnings("unchecked")
		final ListView<AppInfo> list = ctrl.getList();

		list.setCellFactory(new Callback<ListView<AppInfo>, ListCell<AppInfo>>() {
			@Override
			public ListCell<AppInfo> call(ListView<AppInfo> list) {
				return new ListCell<AppInfo>() {
					@Override
					public void updateItem(AppInfo item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null)
							setGraphic(new Label(item.getTitle() + " ["
									+ df.format(item.getRelease()) + "]"));
					}
				};
			}
		});

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (response.getApps() != null) {
					for (AppInfo app : response.getApps()) {
						list.getItems().add(app);
					}
					ctrl.getButtonOk().setDisable(list.getItems().isEmpty());
					// show
					cleanScreen();
					Pane lv = ctrl.getRoot();
					((Pane) root.getCenter()).getChildren().add(lv);
					AnchorPane.setTopAnchor(lv, 40D);
					AnchorPane.setLeftAnchor(lv, 60D);
					AnchorPane.setRightAnchor(lv, 60D);
					AnchorPane.setBottomAnchor(lv, 40D);

				} else {
					printInfo(Main.getStrings().getString(
							"listApplicationIsEmpty"));
				}

			}
		});
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				list.requestFocus();
				list.getSelectionModel().select(0);
			}
		});

		ctrl.getButtonOk().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					AppInfo selectedApp = list.getSelectionModel().getSelectedItem();
					login(selectedApp.getName());
				} catch (Exception e) {
					Main.log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

		});
	}

	private void cleanScreen() {
		AnchorPane anchor = new AnchorPane();
		root.setCenter(anchor);
	}
	
	private void cleanScreen(boolean onlyInfoBox) {
		if(onlyInfoBox && root.getCenter() instanceof InfoBox)
			cleanScreen();else
				if(!onlyInfoBox) cleanScreen(); 
	}
	

	
	public void login(final String appName) throws Exception {
		cleanScreen(true);
		final LoginController ctrl = Main.loadNodeController("Login.fxml");
		Dialog dlg = new Dialog((Pane) root.getCenter(), ctrl.getRoot());
		
		dlg.setFirstInFocus(ctrl.getUserNameControl());

		//default value
		final DomainProperties props = Main.getDomainProperties(getHost().domain);
		ctrl.getUserNameControl().setText(props.getProperty("user.name"));
		
		dlg.showForResult(new DialogResult() {
			Dialog dialog;
			@Override
			public boolean handle(CustomDialog dlg, ButtonType btn) {
				dialog = (Dialog) dlg;
				if (btn == ButtonType.Cancel) {
					dialog.close();
					return true;
				}
				boolean r = ctrl.getValidateControl().action(ctrl.getRoot());
				if (r) {
					final String user = ctrl.getUserNameControl().getText();
					interrupt();
					try {
						Host host = getHost();
						query = new Client(host.domain, host.port,
								new OnResponse() {
									@Override
									public void replyСame(String message)
											throws Exception {
										try {
											// parse
											CommandLogin.Response response = CommandFactory.createGson()
													.fromJson(message,CommandLogin.Response.class);
											if (response.getStatus() != Command.OK) {
												Main.log.log(Level.SEVERE,response.getMessage()+ " detail:"+ response.getTrace());
												dialog.addActionMessage(response.getMessageForUser());
											} else {
												Main.log.log(Level.INFO,"response:" + message);
												//close
												Platform.runLater(new Runnable() {
													@Override
													public void run() {
														dialog.close();
													}
												});
												// handle
												handleLogin(user,response);
											};	
										} finally {
											// finish
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													ready();
													dialog.ready();
												}
											});
										}
									}
								}, getErrorHandler(dialog));
						try {
							try {
								// save default
								props.setProperty("user.name",user);
								props.save();
							} catch (Exception e) {
								Main.log.log(Level.SEVERE,e.getMessage(),e);
							}
							r = false;
							query.send(new CommandLogin(appName, user, ctrl
									.getPasswordControl().getText()));
						} finally {
							query = null;
						}
					} catch (URISyntaxException e) {
						dialog.addActionMessage(Main.getStrings().getString(
								"ex-URISyntaxError"));
					}
				} else {
					ready();
					dialog.ready();
				}
				return r;
			}

			@Override
			public boolean before(CustomDialog dialog) {
				TabInnerController.this.waiting();
				((Dialog) dialog).waiting();
				return true;
			}

			@Override
			public void after(CustomDialog dialog) {
			}
		});
	}

	public void waiting() {
		if(waitCount==0)
			setProgress(true);
		waitCount++;
	}
	
	public void ready() {
		waitCount--;
		if(waitCount==0)
			setProgress(false);
	}

	private void setWelcomePage() {
		cleanScreen();
		if(owner!=null)
			owner.setText(Main.getStrings().getString("newpage"));
		//TODO
	}

	public static Host getHostFromString(String s) throws URISyntaxException {
		if(s==null || s.isEmpty()) return null;
		if (!s.startsWith(Main.DSP_PROTOCOL) || !s.startsWith(Main.DSP_PROTOCOL_SECURY))
			s = Main.DSP_PROTOCOL_SECURY + s;
		URI uri = new URI(s);
		String domain = uri.getHost();
		int port = uri.getPort();
		if(port<0) port = 80; //default port
		boolean security = false; 
		if(s.startsWith(Main.DSP_PROTOCOL_SECURY))
			security = true;
		String path = uri.getPath();
		return new Host(domain, port,security,path==null?"":path);
	}
	
	public Host getHost() throws URISyntaxException {
		DomainInfo input = comboUri.getValue();
		if(input==null) return getHostFromString(comboUri.getText());
				   else return input.host;
	}

	protected void lockControl() {
		btnBack.setDisable(!navStack.isBackPossible());
		btnNext.setDisable(!navStack.isNextPossible());
	}

	private void callLogout(Host host) {
		query = new Client(host.domain, host.port,
				new OnResponse() {
					@Override
					public void replyСame(String message) throws Exception {
						// nothing
					}
				}, getErrorHandler());
		try {
			query.send(new CommandLogout(appWorkspace.getSession().getId()));
		} finally {
			query = null;
		}
	}
	
	private void callHello(Host host) {
		query = new Client(host.domain, host.port,
				new OnResponse() {
					@Override
					public void replyСame(String message) throws Exception {
						try {
							// parse
							CommandHello.Response response = CommandFactory
									.createGson()
									.fromJson(message,CommandHello.Response.class);
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
							handleHello(response);
						} finally {
							// finish
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									ready();
								}
							});
						}
					}
				}, getErrorHandler());
		waiting();
		try {
			query.send(new CommandHello());
		} finally {
			query = null;
		}
	}

	private void callByUri(Host h) throws Exception {
		if(h.path.isEmpty()) callHello(h); 
					    else login(h.path); // application name
		//TODO возможно еще что-то?
	}
	
	private void setLocation(DomainInfo di) {
		currentLocation = di;
	}

	public OnError getErrorHandler() {
		return getErrorHandler(null);
	}
	
	private OnError getErrorHandler(final Dialog dlg) {
		return new OnError() {
			@Override
			public void errorCame(Exception ex) {
				ready();
				if(dlg!=null) dlg.ready();
				if(ex instanceof EmptyBodyException) {
					Main.log.log(Level.SEVERE,"Wrong responce (null or empty)");
					show(String.format(Main.getStrings().getString("ex-ServerUnknownResponse")));
				} else
				if (ex instanceof ExecException) {
					String ms = ex.getMessage();
					if (ms == null || ms.isEmpty()) {
						if(ex.getCause()!=null) {
							ms = ex.getCause().getClass().getName();
							Main.log.log(Level.SEVERE, ex.getCause().getMessage(),ex.getCause());
						} else {
							ms = ex.getClass().getName();
							Main.log.log(Level.SEVERE, ms,ex);
						}	
					}
					show(ms);
				} else {
					Main.log.log(Level.SEVERE, ex.getMessage(),ex);
					show(String.format(
							Main.getStrings().getString("ex-NetError"),
							ex.getMessage()));
				}	
			}
			
			private void show(String msg) {
				if(dlg==null || !dlg.isVisible()) printError(msg);else
					dlg.addActionMessage(msg);
			}
		};
	}
	
	public DomainInfo newDomainInfo(Host host) {
		DomainInfo di = new DomainInfo();
		di.host =  host;
		di.saveDate = new Date();
		int idx = findDomain(di);
		if(idx<0) {
			comboUri.getItems().add(di);
			saveDomainHistory();
		} else {
			// replace
			comboUri.getItems().remove(idx);
			comboUri.getItems().add(di);
		}
		return di;
	}

	public void navigate(String uri) throws URISyntaxException {
		navigate(getHostFromString(uri));
	}
	
	private void navigate(Host host) {
		// title
		if(host==null) {
			reset();
		} else {
			// new domain
			if(comboUri.getValue()==null) {
				DomainInfo di = newDomainInfo(host);
				comboUri.setValue(di);
			}
			jump(comboUri.getValue(), new Callback<Host, Void>() {
				@Override
				public Void call(Host h) {
					try {
						callByUri(h);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					return null;
				}
			});
		}
	}

	public void reset() {
		comboUri.setValue(null);
		setLocation(null);
		setWelcomePage();
	}

	static public class Host {
		public final boolean security;
		public final String domain;
		public final int port;
		public final String path;

		public Host(String domain, int port, boolean security, String path) {
			this.domain = domain;
			this.port = port;
			this.security = security;
			this.path = path;
		}

		public String getLocation() {
			return toString()+path;
		}
		
		public String toString() {
			return domain+(port>0 && port!=80?":"+port:"");
		}
	}
	
	public class DomainInfo {
		private Host host;
		private String title ="";
		private boolean favorite;
		private Date saveDate;
		
		public String toString() {
			return host.toString()+host.path;
		}
		
		public boolean equals(DomainInfo obj) {
			return host.domain.equals(obj.host.domain) && 
				   host.port==obj.host.port &&
				   host.security==obj.host.security &&
				   host.path.equals(obj.host.path);
			
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
			owner.setText(title);
		}
	}

	public void startApplication(DomainInfo di) {
		setLocation(di);
		comboUri.setValue(di);
		comboUri.setDisable(true);
	}
	
	public void stopApplication() {
		if(appWorkspace==null) return;
		callLogout(appWorkspace.getHost());
		comboUri.setDisable(false);
		appWorkspace = null;
	}
}
