package application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import org.bebrb.client.Client;
import org.bebrb.client.Client.ExecException;
import org.bebrb.client.Client.OnError;
import org.bebrb.client.Client.OnResponse;
import org.bebrb.client.Dialog;
import org.bebrb.client.Dialog.DialogResult;
import org.bebrb.context.ApplicationContext;
import org.bebrb.server.net.Command;
import org.bebrb.server.net.CommandFactory;
import org.bebrb.server.net.CommandHello;
import org.bebrb.server.net.CommandHello.AppInfo;
import org.bebrb.server.net.CommandLogin;

import utils.DomainProperties;
import utils.LocaleUtils;
import application.NavigateStack.CommandPoint;

public class TabInnerController {
	@FXML
	private BorderPane root;
	@FXML
	private ComboBox<String> comboUri;
	@FXML
	private Button btnBack;
	@FXML
	private Button btnNext;

	private Tab owner;
	private Client query;
	private ApplicationContext app;

	private NavigateStack navStack = new NavigateStack();

	private OnError errorHandler = new OnError() {
		@Override
		public void errorCame(Exception ex) {
			setProgress(false);
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
		btnBack.setDisable(true);
		btnBack.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				navStack.back();
				lockControl();
			}
		});

		btnNext.setDisable(true);
		btnNext.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				navStack.next();
				lockControl();
			}
		});

		// FIXME demo
		comboUri.setValue("localhost:8080");

		comboUri.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
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
												setProgress(false);
											}
										});
									}
								}
							}, errorHandler);
					setProgress(true);
					try {
						query.send(new CommandHello());
					} finally {
						query = null;
					}
				} catch (URISyntaxException e) {
					printError(Main.getStrings().getString("ex-URISyntaxError"));
				}
			}
		});

	}

	@Override
	protected void finalize() throws Throwable {
		interrupt();
		super.finalize();
	}

	private void interrupt() {
		if (query != null) {
			setProgress(false);
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
	 * ============================ ============ CommandHello
	 * ====================
	 * ======================================================
	 * ==========================
	 */
	private void handleLogin(final CommandLogin.Response response)
			throws Exception {

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
						public void retry() {
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
													setProgress(false);
												}
											});
										}
									}
								}, new OnError() {
									@Override
									public void errorCame(Exception ex) {
										setProgress(false);
										if (ex instanceof ExecException) {
											String ms = ex.getMessage();
											if (ms == null || ms.isEmpty()) {
												ms = ex.getCause().getClass().getName();
												Main.log.log(Level.SEVERE, ex.getCause()
														.getMessage(), ex.getCause());
											}
											dlg.addActionMessage(ms);
										} else
											dlg.addActionMessage(String
													.format(Main.getStrings().getString("ex-NetError"),
															ex.getMessage()));
									}
								});
						setProgress(true);
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
				}
				return r;
			}
		});
	}

	private Host getHost() throws URISyntaxException {
		String input = comboUri.getValue();
		URI uri;
		if (!input.startsWith(Main.DSP_PROTOCOL))
			input = Main.DSP_PROTOCOL + input;
		uri = new URI(input);
		String domain = uri.getHost();
		int port = uri.getPort();
		return new Host(domain, port);
	}

	protected void lockControl() {
		btnBack.setDisable(!navStack.isBackPossible());
		btnNext.setDisable(!navStack.isNextPossible());
	}

	public class Host {
		public final String domain;
		public final int port;

		public Host(String domain, int port) {
			this.domain = domain;
			this.port = port;
		}
	}
}
