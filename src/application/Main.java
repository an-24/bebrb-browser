package application;
	
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.UTF8Control;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	static Locale locale = Locale.getDefault();
	static ResourceBundle resStrings;
	static Logger log = Logger.getLogger("bebrb");
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)loadNode("Main.fxml");
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("caspian-silver.css").toExternalForm());
			primaryStage.setTitle("Bebrb");
			//primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("images/icon.png")));
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public static void main(String[] args) {
		//TODO read locale in settings
		resStrings = ResourceBundle.getBundle("bundles.strings", locale,new UTF8Control());
		launch(args);
	}
	
	public static Parent loadNode(String name) {
		try {
			return FXMLLoader.load(Main.class.getResource(name),resStrings);
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public static ResourceBundle getStrings() {
		return resStrings;
	} 
}
