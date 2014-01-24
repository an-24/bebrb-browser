package org.bebrb.client.controls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class SuggestBox<T> extends TextField {

	private Timer inputTimer;
	private EventHandler<MouseEvent> emptyButtonHandler;
	private final static String SUGGESTBOX_STYLE = "suggestbox";
	private final static int WIDTH_BUTTON_CLEAR = 24;
	private boolean showingCleanButton;
	
	private List<T> items;
	private FilterItems<T> onFilterItem;
	private CellFactory<T> cellFactory = new DefaultFactory();
	private Map<Node,T> nodeMap = new HashMap<>();
	private Map<T,Node> itemMap = new HashMap<>();
	
	private boolean showingSuggestBox;
	private Popup popup;
	private T selectedItem = null;
	private T currItem = null;
	private int lockPopup = 0; 

	public SuggestBox() {
		super();
		addListeners();
	}

	private void addListeners() {
		getContent().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!oldValue.equals(newValue)) selectedItem = null;
				if(!newValue.isEmpty() && oldValue.isEmpty()) setVisibleCleanButton(true);
				if(newValue.isEmpty() && !oldValue.isEmpty()) setVisibleCleanButton(false);
				if(!newValue.isEmpty())	startSuggestTimeout(newValue);
								   else {
									   stopSuggestTimeout();
									   hideSuggestPopup();
								   }
			}
		});
		
		emptyButtonHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double x = event.getX();
				if(x>getWidth()-WIDTH_BUTTON_CLEAR)
					setText("");
			}
		};
		
		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double x = event.getX();
				if(x>getWidth()-WIDTH_BUTTON_CLEAR && showingCleanButton){
					SuggestBox.this.setCursor(Cursor.HAND); 
				} else {
					SuggestBox.this.setCursor(Cursor.TEXT);	
				}
						
			}
			
		};
		addEventHandler(MouseEvent.MOUSE_MOVED,mouseHandler);
		addEventHandler(MouseEvent.MOUSE_ENTERED,mouseHandler);
		addEventHandler(MouseEvent.MOUSE_EXITED,mouseHandler);
		
		
		addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				// ctrl+arrow down || F4
				if((event.isControlDown() && event.getCode()==KeyCode.DOWN) ||
					event.getCode()==KeyCode.F4) {
					if(showingSuggestBox) return;
					showSuggestPopup();
					event.consume();
				};
				// arrow down
				if(event.getCode()==KeyCode.DOWN) {
					if(!showingSuggestBox) return;
					deSelectItemInList();
					//deSelectItemInList(currItem);
					currItem = nextItem(currItem);
					if(currItem==null) currItem = getLastItem();
					selectItemInList(currItem);
					event.consume();
				} else
				// arrow up
				if(event.getCode()==KeyCode.UP) {
					deSelectItemInList();
					//deSelectItemInList(currItem);
					if(!showingSuggestBox) return;
					currItem = priorItem(currItem);
					if(currItem==null) currItem = getFirstItem();
					selectItemInList(currItem);
					event.consume();
				}
				//ENTER
				if(event.getCode()==KeyCode.ENTER) {
					if(currItem!=null) choise(currItem);
					hideSuggestPopup();
				}
			}
		});
		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> val, Boolean nval,
					Boolean oldval) {
				hideSuggestPopup();
			}
		});
	}

	private void selectItemInList(T itm) {
		if(itm==null) return;
		Node n = itemMap.get(itm);
		n.setStyle("-fx-background-color:-fx-cell-hover-color;");
		lockPopup++;
		setText(itm.toString());
		end();
		lockPopup--;
	}
	
	private void deSelectItemInList(T itm) {
		if(itm==null) return;
		Node n = itemMap.get(itm);
		n.setStyle("-fx-background-color:transparent;");
	}

	private void deSelectItemInList() {
		for(Node n :nodeMap.keySet()) {
			n.setStyle("-fx-background-color:transparent;");
		}
	}
	
	private void stopSuggestTimeout() {
		if(inputTimer!=null) inputTimer.cancel();
		inputTimer = null;
	}

	private void startSuggestTimeout(String newValue) {
		stopSuggestTimeout();

		if(lockPopup>0) return;
		
		inputTimer = new Timer();
		inputTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						showSuggestPopup();
					}
				});
			}
		},200);
	}

	private void setVisibleCleanButton(boolean b) {
		showingCleanButton = b;
		getStyleClass().remove(SUGGESTBOX_STYLE+"-noempty");
		getStyleClass().remove(SUGGESTBOX_STYLE+"-empty");
		if(b) {
			String cname = SUGGESTBOX_STYLE+"-noempty";
			getStyleClass().add(cname);
			addEventHandler(MouseEvent.MOUSE_CLICKED, emptyButtonHandler);
		} else {
			getStyleClass().add(SUGGESTBOX_STYLE+"-empty");
			removeEventHandler(MouseEvent.MOUSE_CLICKED, emptyButtonHandler);
		}
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public FilterItems<T> getOnFilterItem() {
		return onFilterItem;
	}

	public void setOnFilterItem(FilterItems<T> onFilterItem) {
		this.onFilterItem = onFilterItem;
	}

	public void hideSuggestPopup() {
		if(!showingSuggestBox) return;
		popup.hide();
	}
	
	public void showSuggestPopup() {
		if(lockPopup>0) return;

		selectedItem = null;
		
		if(showingSuggestBox) {
			VBox box = (VBox) popup.getContent().get(0);
			box.getChildren().clear();
			if(!fillItemsInBox(box)) {
				popup.hide();
			}
			return;
		}
		
		VBox box = new VBox();
		box.setPrefWidth(getWidth());
		box.getStyleClass().add("suggestbox-popup");
		box.setFocusTraversable(true);
		
		if(!fillItemsInBox(box)) return;

		popup = new Popup();
		popup.getContent().add(box);
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setWidth(getWidth());
		popup.setOnHidden(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				showingSuggestBox = false;
				popup = null;
			}
		});
		
		Point2D p = localToScene(0, 0);
		Scene scene = getScene();
		Window w = getScene().getWindow();
		

		popup.show(w, p.getX() + w.getX() + scene.getX()-5, p.getY() + w.getY()
				+ scene.getY() + getHeight()-5);
		
		showingSuggestBox = true;
	}

	private boolean fillItemsInBox(VBox box) {
		currItem = null;
		if(onFilterItem==null && items==null) return false;
		List<T> list;
		if(onFilterItem!=null) {
					list = onFilterItem.filter(items, getText());
					if(list==null) return false;
				} else list = items;
		nodeMap.clear();
		itemMap.clear();
		for (T t : list) {
			Node n = cellFactory.create(t);
			box.getChildren().add(n);
			addItemListeners(n);
			nodeMap.put(n, t);
			itemMap.put(t,n);
		}
		return true;
	}
	

	private void addItemListeners(final Node n) {
		
		n.addEventHandler(MouseEvent.MOUSE_ENTERED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				n.setStyle("-fx-background-color:-fx-cell-hover-color;");
			}
			
		});
		n.addEventHandler(MouseEvent.MOUSE_EXITED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				n.setStyle("-fx-background-color:transparent;");
			}
			
		});
		n.addEventHandler(MouseEvent.MOUSE_CLICKED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Node n = (Node) event.getSource();
				T item = nodeMap.get(n);
				choise(item);
			}
			
		});
	}


	protected void choise(T item) {
		lockPopup++;
		if(popup!=null) popup.hide();
		if(item!=null) {
			setText(item.toString());
			selectAll();
		} else setText("");
		selectedItem = item;
		lockPopup--;
	}


	public interface FilterItems<T> {
		public List<T> filter(List<T> source,String inputtext);
	}
	
	public interface CellFactory<T> {
		public Node create(T item);
	}
	
	class DefaultFactory implements CellFactory<T> {

		@Override
		public Node create(T item) {
			Label l = new Label(item.toString());
			l.setPrefWidth(getWidth());
			return l;
		}
		
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(T selectedItem) {
		choise(selectedItem);
	}

	private T getFirstItem() {
		VBox box = (VBox) popup.getContent().get(0);
		return nodeMap.get(box.getChildren().get(0));
	}

	private T getLastItem() {
		VBox box = (VBox) popup.getContent().get(0);
		ObservableList<Node> childs = box.getChildren();
		return nodeMap.get(childs.get(childs.size()-1));
	}
	
	private T nextItem(T item) {
		Node nd;
		VBox box = (VBox) popup.getContent().get(0);
		ObservableList<Node> childs = box.getChildren();
		if(item==null) nd = childs.get(0);else {
			nd = itemMap.get(item);
			int idx = childs.indexOf(nd);
			if(idx<childs.size()-1) nd = childs.get(idx+1);
							else return null;
		}
		return nodeMap.get(nd);
	}

	private T priorItem(T item) {
		Node nd;
		VBox box = (VBox) popup.getContent().get(0);
		ObservableList<Node> childs = box.getChildren();
		if(item==null) nd = childs.get(childs.size()-1);else {
			nd = itemMap.get(item);
			int idx = childs.indexOf(nd);
			if(idx>0) nd = childs.get(idx-1);
				 else return null;
		}
		return nodeMap.get(nd);
	}

	public void setValue(T v) {
		choise(v);
	}

	public T getValue() {
		return selectedItem;
	}

	public CellFactory<T> getCellFactory() {
		return cellFactory;
	}

	public void setCellFactory(CellFactory<T> cellFactory) {
		this.cellFactory = cellFactory;
	}
}
