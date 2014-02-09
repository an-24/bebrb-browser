package org.bebrb.client.controls;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.bebrb.client.Cache.Cursor;
import org.bebrb.client.data.DataPageImpl;
import org.bebrb.client.data.DataSourceImpl;
import org.bebrb.client.data.RecordWaiting;
import org.bebrb.client.utils.Resources;
import org.bebrb.data.Attribute;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.Field;
import org.bebrb.data.Record;
import org.bebrb.server.net.CommandGetRecords;

public class DataGrid extends TableView<Record> implements ControlLink {
	private static final double CHECK_COLUMN_SIZE = 30;
	private TableColumn<Record, CheckMarker> check;
	private DataSource dataSource;

	public DataGrid() {
		super();
		check = createFirstColumn();
		getColumns().add(check);
		setPlaceholder(new Label(Resources.getBungles().getString("tableContentNotFound")));
	}
	
	static public TableColumn<Record, String> createColumn(final Attribute attr) {
		TableColumn<Record, String> col = new TableColumn<>();
		col.setId(attr.getName());
		col.setText(attr.getCaption());
		col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Record,String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<Record, String> rec) {
				return new ReadOnlyStringWrapper((String) rec.getValue().getValues().get(attr.getFieldNo()));
			}

		});
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
		menu.getItems().addAll(new MenuItem(Resources.getBungles().getString("grid-menu-1")),
				new MenuItem(Resources.getBungles().getString("grid-menu-2")), 
				new MenuItem(Resources.getBungles().getString("grid-menu-3")));
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


	public void setDataSet(DataSource ds) {
		if(this.dataSource!=ds) {

			clearLayout();
			
			if(dataSource!=null && dataSource instanceof DataSourceLink)
				((DataSourceLink)this.dataSource).unRegisterControl(this);

			this.dataSource = ds;
			
			if(dataSource!=null  && dataSource instanceof DataSourceLink)
				((DataSourceLink)this.dataSource).registerControl(this);
			
			
			if(dataSource!=null && dataSource.isOpen()) {
				buildLayout();	
			}
		}
	}

	public DataSource getDataSet() {
		return this.dataSource;
	}

	private void buildLayout() {
		if(!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					createFields();
					fillData();
				}
			});
		} else {
			createFields();
			fillData();
		}
	}

	private void clearLayout() {
		if(!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					clearData();
					clearFields();
				}
			});
		} else {
			clearData();
			clearFields();
		}
	}

	private void fillData() {
		setItems(new RecordList());
	}

	private void clearData() {
		setItems(null);
	}
	
	private void clearFields() {
		//clean columns
		ObservableList<TableColumn<Record, ?>> cols = getColumns();
		while(cols.size()>1) cols.remove(1);
	}

	private void createFields() {
		//new column list
		List<Attribute> attrs = ((DataSource)dataSource).getAttributes();
		for (Attribute attr :attrs) {
			if(attr.isVisible())
				getColumns().add(DataGrid.createColumn(attr));
		}
	}

	@Override
	public void linkActive(DataSourceLink dsl, boolean active) {
		if(active) {
			buildLayout();	
		} else {
			clearLayout();
		}
	}



	class RecordList extends AbstractList<Record> implements ObservableList<Record> {
		private int pageSize = dataSource.getMaxSizeDataPage();
		private Cursor cursor = ((DataSourceImpl) dataSource).getCursor();
		private List<DataPage> pages = cursor.getDataPages();

		@Override
		public void addListener(InvalidationListener arg0) {
		}

		@Override
		public void removeListener(InvalidationListener arg0) {
		}

		@Override
		public void removeListener(ListChangeListener<? super Record> arg0) {
		}

		@Override
		public void addListener(ListChangeListener<? super Record> arg0) {
		}
		
		@Override
		public boolean addAll(Record... arg0) {
			throw new UnsupportedOperationException();
		}


		@Override
		public void remove(int arg0, int arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Record... arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Record... arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean setAll(Record... arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean setAll(Collection<? extends Record> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Record get(int idx) {
			DataPage page = pages.get(idx/pageSize);
			if(!page.isAlive()) {
				requestPageData(page);
				return new RecordWaiting((DataPageImpl)page);
			} else
			return page.getRecords().get(idx%pageSize);
		}

		@Override
		public int size() {
			return ((DataSourceLink)dataSource).getRecordCount();
		}
		
	}


	private void requestPageData(DataPage page) {
		DataPageImpl dp = ((DataPageImpl)page);
		if(!dp.isRequest()) dp.requestPageData(new Callback<CommandGetRecords.Response, Void>() {
			@Override
			public Void call(CommandGetRecords.Response r) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
}
