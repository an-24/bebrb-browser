package org.bebrb.client.controls.skin;

import java.io.IOException;
import java.util.logging.Level;

import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import org.bebrb.client.Dialog;
import org.bebrb.client.DialogController;
import org.bebrb.client.WaitDialogController;
import org.bebrb.client.controls.DataGrid;
import org.bebrb.client.utils.Resources;

import application.Main;

import com.sun.javafx.scene.control.skin.TableViewSkin;

public class DataGridSkin<T> extends TableViewSkin<T> {

	private StackPane maskPane;
	private DataGrid grid;
	private WaitDialogController cancelDlg;

	public DataGridSkin(DataGrid grid) {
		super(grid);
		this.grid = grid;
		maskPane = new StackPane();
		maskPane.setStyle("-fx-background-color:black;-fx-opacity:0.3;");
		
		
		FXMLLoader loader = new FXMLLoader(Dialog.class.getResource("fxml/WaitDialog.fxml"),Resources.getBungles());
		try {
			loader.load();
			cancelDlg = loader.getController();
		} catch (IOException e) {
			//internal error
			org.bebrb.client.utils.Logger.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
		
		
/*		
		HBox hbox = new HBox();
		hbox.setStyle("-fx-background-color:red;");
		hbox.setAlignment(Pos.CENTER);
		maskPane.getChildren().add(hbox);
		
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setMaxSize(16, 16);
		indicator.setPrefSize(16, 16);
		hbox.getChildren().add(indicator);
*/		
	}

    @Override 
    protected void layoutChildren() {
    	super.layoutChildren();
    	setProgressIndicator(grid.isLockControl());
    }
    
    private void setProgressIndicator(boolean visible) {
		maskPane.setVisible(visible);
    	if(visible) {
    		if(getChildren().indexOf(maskPane)<0) {
    			getChildren().add(maskPane);
    			getChildren().add(cancelDlg.getRoot());
    		}
    		
    		double w = getWidth() - (getInsets().getLeft() + getInsets().getRight());
            double h = getHeight() - (getInsets().getTop() + getInsets().getBottom());
            layoutInArea(maskPane, 0, 0, w, h, 0, HPos.LEFT, VPos.TOP);
            
            layoutInArea(cancelDlg.getRoot(), 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
            
    		/*
    		maskPane.setLayoutX(0);maskPane.setLayoutY(0);
    		maskPane.setPrefHeight(200);maskPane.setPrefWidth(200);
    		AnchorPane.setLeftAnchor(maskPane, 0D);AnchorPane.setRightAnchor(maskPane, 0D);
    		AnchorPane.setTopAnchor(maskPane, 0D);AnchorPane.setBottomAnchor(maskPane, 0D);
    		
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setMinSize(16, 16);
		indicator.setPrefSize(16, 16);
		maskPane.getChildren().add(indicator);
    		
    		*/
            
            
            
    	} else {
    		getChildren().remove(maskPane);
			getChildren().remove(cancelDlg.getRoot());
    	}
	};
	
}
