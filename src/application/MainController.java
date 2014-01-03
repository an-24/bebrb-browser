package application;

import org.bebrb.client.controls.DataGridFX;
import org.bebrb.client.data.RecordImpl;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainController {
	
	@FXML
	private TabPane mainPageControl;

	private int lockTabs = 0;
	
	public Tab newPage() {
		lockTabs++;
		final ObservableList<Tab> tabs = mainPageControl.getTabs();
		//remove +
		if(tabs.size()>0) {
			tabs.remove(tabs.size()-1);
		}
		Tab appTab = new Tab(Main.getStrings().getString("newpage"));
		appTab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
			    tabs.get(0).setClosable(tabs.size()>2);
			}
		});
		tabs.add(appTab);
	    tabs.get(0).setClosable(tabs.size()>1);
		appTab.setContent(Main.loadNode("TabInner.fxml"));
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
		return appTab;
	}
	
	@FXML
	public void initialize() {
		Tab newtab = newPage();
		BorderPane pane = (BorderPane) newtab.getContent();
		//FIXME example controls
		
		
		/*
		MenuButton menu = new MenuButton();
		menu.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		menu.setPrefSize(24,24);
		menu.setMinWidth(24);
		menu.setMinHeight(24);
		pane.setLeft(menu);
*/		
		//pane.getStyleClass().add("apptest");
		DataGridFX grid = new DataGridFX();
		BorderPane.setAlignment(grid, Pos.CENTER);
		BorderPane.setMargin(grid, new Insets(12,12,12,12));
		pane.setCenter(grid);
		//grid.setPrefSize(200, 600);
		//pane.autosize();
		
	}
}
