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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import org.bebrb.client.Client;
import org.bebrb.client.Client.ExecException;
import org.bebrb.client.Client.OnError;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.Dialog;
import org.bebrb.client.Dialog.DialogResult;
import org.bebrb.client.controls.SuggestBox;
import org.bebrb.client.controls.SuggestBox.CellFactory;
import org.bebrb.client.controls.SuggestBox.FilterItems;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandHello;
import org.bebrb.server.net.CommandHello.AppInfo;
import org.bebrb.server.net.CommandLogin;

import utils.DomainProperties;
import utils.LocalStore;
import utils.LocaleUtils;
import application.NavigateStack.CommandPoint;
import application.NavigateStack.CommandPointEx;

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

	private Tab owner;
	private Client query;
	private AppInfo selectedApplication;
	private ApplicationWorkspaceController appWorkspace;

	private NavigateStack navStack = new NavigateStack();
	private DomainInfo currentLocation = null;
	private DomainInfo oldLocation = null;

	private OnError errorHandler = new OnError() {
		@Override
		public void errorCame(Exception ex) {
			ready();
			if (ex instanceof ExecException) {
				String ms = ex.getMessage();
				if (ms == null || ms.isEmpty()) {
					ms = ex.getCause().getClass().getName();
					Main.log.log(Level.SEVERE, ex.getCause().getMessage(),
							ex.getCause());
				}
				printError(ms);
			} else
				printError(String.format(
						Main.getStrings().getString("ex-NetError"),
						ex.getMessage()));
		}
	};

	@FXML
	public void initialize() {
		try {
			LocaleUtils.localeFields(this);
		} catch (Exception e) {
			Main.log.log(Level.SEVERE, e.getMessage(), e);
		}
		
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

		setTitlePage();
		
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
					if(di.host.domain.toLowerCase().contains(inputtext) ||
							di.appPath.toLowerCase().contains(inputtext) ||
							di.title.toLowerCase().contains(inputtext)) {
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
					Host host = getHost();
					// new domain
					if(host==null) {
						comboUri.setValue(null);
						setTitlePage();
					} else
					if(comboUri.getValue()==null) {
						DomainInfo di = new DomainInfo();
						di.host =  host;
						di.saveDate = new Date();
						if(!findDomain(di)) {
							comboUri.getItems().add(di);
							saveDomainHistory();
						}
						comboUri.setValue(di);
					}
					setLocation(comboUri.getValue());
					// push save point
					final DomainInfo saveLocation = oldLocation;
					CommandNavigatePoint savepoint = new CommandNavigatePoint() {
						DomainInfo location = currentLocation;

						@Override
						public void back() {
							if(saveLocation!=null) {
								comboUri.setValue(saveLocation);
								callHello(saveLocation.host);
							} else {
								comboUri.setValue(null);
								setTitlePage();
							}
							setLocation(saveLocation);
						}

						@Override
						public void next() {
							CommandPoint n = navStack.getNext(this);
							if(n!=null && n instanceof CommandNavigatePoint) ((CommandNavigatePoint)n).call();	
						}
						
						@Override
						public void call() {
							if(location==null) {
								setTitlePage();
							} else {
								comboUri.setValue(location);
								callHello(location.host);
								setLocation(location);
							}	
						}
						
					};
					navStack.push(savepoint);
					savepoint.call();
					lockControl();
				} catch (URISyntaxException e) {
					printError(Main.getStrings().getString("ex-URISyntaxError"));
				}
			}
		});

	}
	
	protected boolean findDomain(DomainInfo target) {
		List<DomainInfo> items = comboUri.getItems();
		for (DomainInfo dinf : items) {
			if(dinf.equals(target))
				return true;
		}
		return false;
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
		HBox box = new HBox();
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
		HBox box = new HBox();
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

	private void setProgress(boolean b) {
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
	 */
	private void handleLogin(final CommandLogin.Response response)
			throws Exception {
		final ApplicationWorkspaceController ctrl = Main.loadNodeController("ApplicationWorkspace.fxml");
		ctrl.setTabController(this);
		ctrl.setup(response);
		appWorkspace = ctrl;
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cleanScreen();
				root.setCenter(ctrl.getRoot());
			}
		});
		// push save point
		CommandPointEx savepoint = new CommandPointEx() {
			@Override
			public void back() {
				try {
					cleanScreen();
					login(selectedApplication);
				} catch (Exception e) {
					Main.log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

			@Override
			public void next() {
				try {
					handleLogin(response);
				} catch (Exception e) {
					Main.log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

			@Override
			public void afterBack() {
				//cut stack
				navStack.cut(navStack.getTop());
				lockControl();
			}

			@Override
			public void afterNext() {
			}
		};
		navStack.push(savepoint);
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
					final AppInfo selectedApp = list.getSelectionModel()
							.getSelectedItem();
					// push save point
					CommandPoint savepoint = new CommandPoint() {
						@Override
						public void back() {
							try {
								handleHello(response);
							} catch (Exception e) {
								Main.log.log(Level.SEVERE, e.getMessage(), e);
							}
						}

						@Override
						public void next() {
							try {
								login(selectedApp);
							} catch (Exception e) {
								Main.log.log(Level.SEVERE, e.getMessage(), e);
							}
						}
					};
					navStack.push(savepoint);
					// next command
					savepoint.next();
					lockControl();
				} catch (Exception e) {
					Main.log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

		});
	}

	private void cleanScreen() {
		root.setCenter(new AnchorPane());
	}

	private void login(final AppInfo app) throws Exception {
		final LoginController ctrl = Main.loadNodeController("Login.fxml");
		final Dialog dlg = new Dialog((Pane) root.getCenter(), ctrl.getRoot());
		
		this.selectedApplication = app;
		dlg.setFirstInFocus(ctrl.getUserNameControl());

		//default value
		final DomainProperties props = Main.getDomainProperties(getHost().domain);
		ctrl.getUserNameControl().setText(props.getProperty("user.name"));
		
		dlg.showForResult(new DialogResult() {
			@Override
			public boolean handle(boolean btnOk) {
				if (!btnOk) {
					navStack.back();
					return true;
				}
				boolean r = ctrl.getValidateControl().action(ctrl.getRoot());
				if (r) {
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
												dlg.addActionMessage(response.getMessageForUser());
											} else {
												Main.log.log(Level.INFO,"response:" + message);
												//close
												Platform.runLater(new Runnable() {
													@Override
													public void run() {
														dlg.close();
													}
												});
												// handle
												handleLogin(response);
											};	
										} finally {
											// finish
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													ready();
													dlg.ready();
												}
											});
										}
									}
								}, new OnError() {
									@Override
									public void errorCame(Exception ex) {
										Main.log.log(Level.SEVERE, ex.getCause().getMessage(), ex.getCause());
										ready();
										dlg.ready();
										if (ex instanceof ExecException) {
											String ms = ex.getMessage();
											if (ms == null || ms.isEmpty()) {
												ms = ex.getCause().getClass().getName();
											}
											dlg.addActionMessage(ms);
										} else {
											dlg.addActionMessage(String
													.format(Main.getStrings().getString("ex-NetError"),
															ex.getMessage()));
										}	
									}
								});
						try {
							try {
								// save default
								props.setProperty("user.name",ctrl.getUserNameControl().getText());
								props.save();
							} catch (Exception e) {
								Main.log.log(Level.SEVERE,e.getMessage(),e);
							}
							r = false;
							query.send(new CommandLogin(app.getName(), ctrl
									.getUserNameControl().getText(), ctrl
									.getPasswordControl().getText()));
						} finally {
							query = null;
						}
					} catch (URISyntaxException e) {
						dlg.addActionMessage(Main.getStrings().getString(
								"ex-URISyntaxError"));
					}
				} else {
					ready();
					dlg.ready();
				}
				return r;
			}

			@Override
			public boolean before() {
				TabInnerController.this.waiting();
				dlg.waiting();
				return true;
			}

			@Override
			public void after() {
			}
		});
	}

	protected void waiting() {
		setProgress(true);
	}
	
	protected void ready() {
		setProgress(false);
	}

	private void setTitlePage() {
		// TODO Auto-generated method stub
		cleanScreen();
	}

	private Host getHost() throws URISyntaxException {
		DomainInfo input = comboUri.getValue();
		if(input==null) {
			String s = comboUri.getText();
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
			return new Host(domain, port,security);
		} else
			return input.host;
	}

	protected void lockControl() {
		btnBack.setDisable(!navStack.isBackPossible());
		btnNext.setDisable(!navStack.isNextPossible());
	}

	private void callHello(Host host) {
		query = new Client(host.domain, host.port,
				new OnResponse() {
					@Override
					public void replyСame(String message)
							throws Exception {
						try {
							if(message==null || message.isEmpty()) {
								Main.log.log(Level.SEVERE,"Wrong responce (null or empty)");
								throw new Exception(
										String.format(Main.getStrings().getString("ex-ServerUnknownResponse")));
							}
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
				}, errorHandler);
		waiting();
		try {
			query.send(new CommandHello());
		} finally {
			query = null;
		}
	}

	private void setLocation(DomainInfo di) {
		oldLocation = currentLocation; 
		currentLocation = di;
	}
	
	public interface CommandNavigatePoint extends CommandPoint {
		void call();
	}

	public class Host {
		public final boolean security;
		public final String domain;
		public final int port;

		public Host(String domain, int port, boolean security) {
			this.domain = domain;
			this.port = port;
			this.security = security;
		}
	}
	
	public class DomainInfo {
		private Host host;
		private String title ="";
		private String appPath ="";
		private boolean favorite;
		private Date saveDate;
		
		public String toString() {
			return host.domain+(host.port>0 && host.port!=80?":"+host.port:"")+appPath;
		}
		
		public boolean equals(DomainInfo obj) {
			return host.domain.equals(obj.host.domain) && 
				   host.port==obj.host.port &&
				   host.security==obj.host.security &&
				   appPath.equals(obj.appPath);
			
		}
	}
}
