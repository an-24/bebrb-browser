package application;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {
	
	@FXML
	private TabPane mainPageControl;

	private int lockTabs = 0;
	
	public void newPage() {
		lockTabs++;
		ObservableList<Tab> tabs = mainPageControl.getTabs();
		//remove +
		if(tabs.size()>0) {
			tabs.remove(tabs.size()-1);
		}
		Tab appTab = new Tab("Новая вкладка");
		tabs.add(appTab);
		//add +
		Tab plusTab = new Tab("+");
		plusTab.setClosable(false);
		plusTab.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				if(lockTabs==0) newPage();
			}
		});
		tabs.add(plusTab);
		mainPageControl.getSelectionModel().select(appTab);
		lockTabs--;
	}
	
	@FXML
	public void initialize() {
		newPage();
	}
}
