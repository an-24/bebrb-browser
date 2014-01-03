package application;

import java.net.URI;
import java.net.URISyntaxException;

import org.bebrb.context.ApplicationContext;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class TabInnerController {
	@FXML
	private BorderPane root;
	@FXML
	private ComboBox<String> comboUri;
	
	private ApplicationContext app;
	
	@FXML
	public void initialize() {
		//FIXME demo
		comboUri.setValue("localhost:8080");
		
		
		comboUri.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				String input = comboUri.getValue();
				URI uri;
				try {
					uri = new URI(input);
				    String domain = uri.getHost();
				    int port = uri.getPort();
				    
					// TODO Auto-generated method stub
				} catch (URISyntaxException e) {
					printError(Main.getStrings().getString("ex-URISyntaxError"));
				}
			}
		});
	}
	
	private void printError(String err) {
		root.setCenter(new Label(err));
	}

}
