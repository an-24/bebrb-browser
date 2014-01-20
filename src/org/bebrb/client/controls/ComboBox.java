package org.bebrb.client.controls;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.SingleSelectionModel;
import utils.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ComboBox<T> extends javafx.scene.control.ComboBox<T> {
	
	private Set<T> historyList = new HashSet<>(); 
    private String historyLocation = null;
    private boolean loadedHistory = false;
	
	public ComboBox() {
		this(FXCollections.<T>observableArrayList());
		
	}

	public ComboBox(ObservableList<T> list) {
		super(list);
		if(historyProperty().get())
			addEventHandler(ON_SHOWING, new EventHandler<Event>() {
				@Override
				public void handle(Event event) {
					if(!loadedHistory) {
						loadFromHistory();
		                getItems().clear();
						getItems().addAll(historyList);
						loadedHistory=true;
					}	
				}
				
			});
		
		
		setSelectionModel(new SingleSelectionModel<T>() {

			@Override
			protected T getModelItem(int index) {
	            final ObservableList<T> items = ComboBox.this.getItems();
	            if (items == null) return null;
	            if (index < 0 || index >= items.size()) return null;
	            return items.get(index);
			}

			@Override
			protected int getItemCount() {
	            final ObservableList<T> items = ComboBox.this.getItems();
	            return items == null ? 0 : items.size();
			}
		});
		//getSelectionModel().selectedItemProperty().removeListener(arg0);
		
		
	}

	private ObjectProperty<T> value;

    public ObjectProperty<T> valueProperty() {
    	if(value==null) 
    		value = new ValueProperty<T>(this, "value");
		return value;
	}

    public boolean isModify() {
    	return !getEditor().getText().equals(getValue());
    }
    
    void valueInvalidate() {
        fireEvent(new ActionEvent());
    }
    
    class ValueProperty<T> extends javafx.beans.property.SimpleObjectProperty<T> {

        T oldValue;
    	
		public ValueProperty(Object obj, String name) {
			super(obj, name);
		}
    	
        
        @Override 
        protected void invalidated() {
            super.invalidated();
            if (isModify()) {
            	valueInvalidate();
        		if(historyProperty().get()) {
        			saveToHistory();
        			getItems().clear();
        			getItems().addAll(historyList);
        		}	
            }
            oldValue = get();
        }
        
        public boolean isModify() {
        	T newValue = get();
        	return ((oldValue == null && newValue != null) ||
                    oldValue != null && ! oldValue.equals(newValue));
        }
    	
    }
    
    private BooleanProperty history;
    public BooleanProperty historyProperty() {
    	if(history==null) {
    		history = new SimpleBooleanProperty(this, "history");
    		history.set(true); //default
    	}	
		return history;
    }
    
    
	public String getHistoryLocation() {
		return historyLocation!=null?historyLocation:getId();
	}

	public void setHistoryLocation(String historyLocation) {
		this.historyLocation = historyLocation;
	}

	private void saveToHistory(){
		if(!historyProperty().get()) return;
		if(!loadedHistory) loadFromHistory();
    	historyList.add(getValue());
    	// save
    	if(getHistoryLocation()!=null) {
			try {
				File f = Files.openOrCreateFile(getHistoryLocation()+".history");
	    		try(FileWriter fwr = new FileWriter(f)) {
	        		Gson gson = new GsonBuilder().create();
	        		fwr.write(gson.toJson(historyList));
	    		}
			} catch (IOException e) {
			}
    	}
    	
    }

	
	private void loadFromHistory(){
		if(!historyProperty().get()) return;
    	if(getHistoryLocation()!=null) {
			try {
				File f = Files.openOrCreateFile(getHistoryLocation()+".history");
	       		Gson gson = new GsonBuilder().create();
	       		historyList = gson.fromJson(Files.readFile(f), HashSet.class);
			} catch (IOException e) {
			}
    	}
	}
	
}
