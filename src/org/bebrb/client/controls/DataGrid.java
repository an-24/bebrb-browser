package org.bebrb.client.controls;

import java.util.List;
import java.util.Map;

import org.bebrb.data.Attribute;
import org.bebrb.data.BaseDataSet;
import org.bebrb.data.DataSource;
import org.bebrb.data.Field;
import org.bebrb.data.Record;

import application.Main;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class DataGrid extends TableView<Record> {
	private static final double CHECK_COLUMN_SIZE = 30;
	private TableColumn<Record, CheckMarker> check;
	private BaseDataSet dataSource;

	public DataGrid() {
		super();
		check = createFirstColumn();
		getColumns().add(check);
		setPlaceholder(new Label(Main.getStrings().getString("tableContentNotFound")));
	}
	
	static public TableColumn<Record, Field<?>> createColumn(Attribute attr) {
		TableColumn<Record, Field<?>> col = new TableColumn<>();
		col.setId(attr.getName());
		col.setText(attr.getCaption());
		// TODO Auto-generated method stub
		return col;
	}
	
	private TableColumn<Record, CheckMarker> createFirstColumn() {
		TableColumn<Record, CheckMarker> chcol = new TableColumn<>();
		chcol.setSortable(false);
		chcol.setMaxWidth(CHECK_COLUMN_SIZE);
		chcol.setMinWidth(CHECK_COLUMN_SIZE);
		chcol.setCellFactory(new Callback<TableColumn<Record,CheckMarker>, TableCell<Record,CheckMarker>>() {
			
			@Override
			public TableCell<Record, CheckMarker> call(
					final TableColumn<Record, CheckMarker> param) {
				return new TableCell<Record, CheckMarker>() {
					@Override
					protected void updateItem(final CheckMarker item, boolean empty) {
						if(item==null) return;
						CheckBox box = new CheckBox();
						box.setSelected(item.getMarked());
						box.selectedProperty().addListener(new ChangeListener<Boolean>() {
							@Override
							public void changed(
									ObservableValue<? extends Boolean> observable,
									Boolean oldValue, Boolean newValue) {
								item.setMarked(newValue);
							}
						});
						setGraphic(box);
					}
				};
			}
		});
		chcol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Record,CheckMarker>, ObservableValue<CheckMarker>>() {
			
			@Override
			public ObservableValue<CheckMarker> call(
					CellDataFeatures<Record, CheckMarker> param) {
				return new SimpleObjectProperty<>(makeMarker(param.getValue()));
			}
		});
		
		MenuButton menu = new MenuButton();
		menu.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		menu.setPrefSize(CHECK_COLUMN_SIZE-5,CHECK_COLUMN_SIZE-8);
		menu.setMinWidth(CHECK_COLUMN_SIZE-5);
		menu.setMinHeight(CHECK_COLUMN_SIZE-8);
		menu.setAlignment(Pos.CENTER);
		menu.setStyle("-fx-background-color: transparent;");
		menu.getItems().addAll(new MenuItem(Main.getStrings().getString("grid-menu-1")),
				new MenuItem(Main.getStrings().getString("grid-menu-2")), 
				new MenuItem(Main.getStrings().getString("grid-menu-3")));
		menu.getItems().get(0).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				setMarkAll(true);
			}
		});
		menu.getItems().get(1).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				setMarkAll(false);
			}
		});
		menu.getItems().get(2).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent action) {
				invertMarkers(false);
			}
		});
		chcol.setGraphic(menu);
		
		return chcol;
	}


	public void invertMarkers(boolean b) {
		// TODO Auto-generated method stub
		refreshLayout();
	}


	public void setMarkAll(boolean b) {
		// TODO Auto-generated method stub
		refreshLayout();
	}


	private void refreshLayout() {
		getColumns().get(0).setVisible(false);
		getColumns().get(0).setVisible(true);
	}


	private CheckMarker makeMarker(Record rec) {
		int ccount = rec.getFields().size();
		List<Object> values = rec.getValues();
		CheckMarker obj = (CheckMarker) (values.size()>ccount?values.get(ccount):null);
		if(obj==null) {
			obj = new CheckMarker(rec);
			values.add(obj);
		}
		return obj;
	}


	class CheckMarker {
		Record record;
		boolean checked;
		public CheckMarker(Record rec) {
			record = rec;
		}
		public boolean getMarked() {
			return  checked;
		}
		public void setMarked(boolean b) {
			checked = b;
		}
	}


	public void setDataSet(DataSource dataSource, Map<String, Object> params) throws Exception {
		if(this.dataSource!=dataSource) {
			this.dataSource = dataSource;
			createFields();
			//dataSource.open(params);
		}
	}

	private void createFields() {
		//clean columns
		ObservableList<TableColumn<Record, ?>> cols = getColumns();
		while(cols.size()>1) cols.remove(1);
		//new column list
		List<Attribute> attrs = ((DataSource)dataSource).getAttributes();
		for (Attribute attr :attrs) {
			if(attr.isVisible())
				getColumns().add(DataGrid.createColumn(attr));
		}
	}

}
