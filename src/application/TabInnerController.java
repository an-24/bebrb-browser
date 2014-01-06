package application;

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
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

import utils.LocaleUtils;

public class TabInnerController {
	@FXML
	private BorderPane root;
	@FXML
	private ComboBox<String> comboUri;
	
	private Tab owner;
	private Client query;
	private ApplicationContext app;

	private OnError errorHandler = new OnError() {
		@Override
		public void errorCame(Exception ex) {
	    	setProgress(false);
			if(ex instanceof ExecException) {
				String ms = ex.getMessage();
				if(ms==null || ms.isEmpty()) {
					ms = ex.getCause().getClass().getName();
					Main.log.log(Level.SEVERE, ex.getCause().getMessage(), ex.getCause());
				}
				printError(ms); 
			} else
				printError(String.format(Main.getStrings().getString("ex-NetError"),
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
		
		
		//FIXME demo
		comboUri.setValue("localhost:8080");
		
		
		comboUri.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				interrupt();
				String input = comboUri.getValue();
				URI uri;
				try {
					if(!input.startsWith(Main.DSP_PROTOCOL)) 
						input = Main.DSP_PROTOCOL+input;
					uri = new URI(input);
				    String domain = uri.getHost();
				    int port = uri.getPort();
				    query = new Client(domain,port,new OnResponse() {
						@Override
						public void replyСame(String message) throws Exception {
							try {
								// parse
								CommandHello.Response response =  CommandFactory.createGson().fromJson(message, CommandHello.Response.class);
								if(response.getStatus()!=Command.OK) { 
									Main.log.log(Level.SEVERE, response.getMessage()+" detail:"+response.getTrace());
									throw new Exception(String.format(Main.getStrings().getString("ex-OnServerError"), response.getMessage()));
								} else
									Main.log.log(Level.INFO, "response:"+message);
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
		if(query!=null) {
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
		box.setMaxSize(root.getScene().getWidth()*2D/3, Region.USE_PREF_SIZE);
		//box.setOpacity(0.85);
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
		box.setMaxSize(root.getScene().getWidth()*2D/3, Region.USE_PREF_SIZE);
		//box.setOpacity(0.85);
		root.setCenter(box);
	}
	
	private void setProgress(boolean b) {
		if(b) {
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
	 *  ====================================================================================================
	 *  ============ CommandHello
	 *  ====================================================================================================
	 */
	private void handleHello(final CommandHello.Response response) throws Exception {
		final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		final ApplicationListController ctrl = Main.loadNodeController("ApplicationList.fxml");
		
		final ListView<AppInfo> list = ctrl.getList(); 
		
		list.setCellFactory(new Callback<ListView<AppInfo>, ListCell<AppInfo>>() {
			@Override
			public ListCell<AppInfo> call(ListView<AppInfo> list) {
				return new ListCell<AppInfo>() {
					@Override
			        public void updateItem(AppInfo item, boolean empty) {
						super.updateItem(item, empty);
						if(item!=null)
							setGraphic(new Label(item.getTitle()+" ["+df.format(item.getRelease())+"]"));
					}
				};
			}
		});
		
	    Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(response.getApps()!=null) {
					for (AppInfo app : response.getApps()) {
						list.getItems().add(app);
					}
					ctrl.getButtonOk().setDisable(list.getItems().isEmpty());
					root.setCenter(ctrl.getRoot());
				} else {
					printInfo(Main.getStrings().getString("listApplicationIsEmpty"));
				}
					
			}
		}); 
	    Platform.runLater(new Runnable() {
			@Override
			public void run() {
				list.requestFocus();
				list.getSelectionModel().select(0);
		}});
	    
	    ctrl.getButtonOk().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					login(list.getSelectionModel().getSelectedItem());
				} catch (Exception e) {
					Main.log.log(Level.SEVERE, e.getMessage(),e);
				}
			}

		});
	}
	private void login(AppInfo app) throws Exception {
		final LoginController ctrl = Main.loadNodeController("Login.fxml");
		
		AnchorPane pane = new AnchorPane();
		root.setCenter(pane);
		Dialog dlg = new Dialog(pane, ctrl.getRoot());
		dlg.showForResult(new DialogResult() {
			
			@Override
			public boolean handle(boolean btnOk) {
				// TODO Auto-generated method stub
				return btnOk;
			}
		});
/*		
		Stage stage = new Stage();
	    stage.setScene(new Scene(ctrl.getRoot()));
		stage.setTitle("Login");
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(root.getScene().getWindow());
	    stage.show();
*/	    		
	}
	
}
