package application;
	
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.bebrb.client.utils.DomainProperties;
import org.bebrb.client.utils.Resources;


public class Main extends Application {
	public static final String DSP_PROTOCOL = "dsp://";
	public static final String DSP_PROTOCOL_SECURY = "dsps://";
	public static String wallPaperName = "images/default.jpg"; //TODO config
	
	
	static Locale locale = Locale.getDefault();
	static ResourceBundle resStrings;
	static Logger log = org.bebrb.client.utils.Logger.getLogger();
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = (Pane)loadNode("Main.fxml");
			Scene scene = new Scene(root, 700,600);
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
		resStrings = Resources.getBungles();
		launch(args);
	}
	
	public static Parent loadNode(String name) {
		try {
			return FXMLLoader.load(Main.class.getResource("fxml/"+name),resStrings);
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public static <T> T loadNodeController(String name) throws IOException {
		FXMLLoader loader = getLoader(name);
		loader.load();
		return loader.getController();
	} 
	
	public static FXMLLoader getLoader(String name) {
		return new FXMLLoader(Main.class.getResource("fxml/"+name),resStrings);
	}

	public static ResourceBundle getStrings() {
		return resStrings;
	}

	public static Logger getLogger() {
		return log;
	}

	
	public static DomainProperties getDomainProperties(String domain) throws FileNotFoundException, IOException {
		return new DomainProperties(domain);
	}

	public static void exit() {
		// TODO Auto-generated method stub
	}
	
}
