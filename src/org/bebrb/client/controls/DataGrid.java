package org.bebrb.client.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.bebrb.client.Cache.Cursor;
import org.bebrb.client.controls.skin.DataGridSkin;
import org.bebrb.client.data.DataPageImpl;
import org.bebrb.client.data.DataSourceImpl;
import org.bebrb.client.data.RecordWaiting;
import org.bebrb.client.utils.Resources;
import org.bebrb.data.Attribute;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.Record;
import org.bebrb.server.data.DataSourceImpl.SortAttribute;
import org.bebrb.server.net.CommandGetRecords;

import com.sun.javafx.collections.transformation.SortableList;
import com.sun.javafx.collections.transformation.TransformationList;
import com.sun.javafx.scene.control.TableColumnComparator;


public class DataGrid extends TableView<Record> implements ControlLink {
	private static final double CHECK_COLUMN_SIZE = 30;
	private static final double COLUMN_MIN_SIZE = 24;
	private TableColumn<Record, CheckMarker> check;
	private DataSource dataSource;
	private int lockControlLayout = 0;

	public DataGrid() {
		super();
		getSelectionModel().setCellSelectionEnabled(true);
		getStyleClass().add("data-grid");
		check = createFirstColumn();
		getColumns().add(check);
		setPlaceholder(new Label(Resources.getBungles().getString("tableContentNotFound")));
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				makeContextMenu();
			}
		});
	}
	
	private void makeContextMenu() {
		setContextMenu(new ContextMenu());
		MenuItem item;
		

		// refresh
		item = new MenuItem(Resources.getBungles().getString("grid-menu-refresh"));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				((DataSourceImpl) dataSource).refresh();
			}
		});
		getContextMenu().getItems().add(item);
		//
		

		// prop
		item = new MenuItem(Resources.getBungles().getString("grid-menu-property"));
		getContextMenu().getItems().add(item);
	}

	static public TableColumn<Record, String> createColumn(final Attribute attr) {
		TableColumn<Record, String> col = new TableColumn<>();
		col.setId(attr.getName());
		col.setMinWidth(COLUMN_MIN_SIZE);
		col.setText(attr.getCaption());
		col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Record,String>, ObservableValue<String>>() {
			int fieldNo = attr.getFieldNo();
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<Record, String> rec) {
				return new ReadOnlyStringWrapper((String) rec.getValue().getValues().get(fieldNo));
			}

		});
		// TODO Auto-generated method stub
		return col;
	}
	
	private TableColumn<Record, CheckMarker> createFirstColumn() {
		TableColumn<Record, CheckMarker> chcol = new TableColumn<>();
		chcol.setSortable(false);
		chcol.setId("_check");
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

			//if request data
			if(isLockControl()) finishRequestData(); 
			
			clearLayout();
			
			if(dataSource!=null && dataSource instanceof DataSourceLink)
				((DataSourceLink)this.dataSource).unRegisterControl(this);

			this.dataSource = ds;
			
			if(dataSource!=null  && dataSource instanceof DataSourceLink)
				((DataSourceLink)this.dataSource).registerControl(this);
			
			
			if(dataSource!=null && dataSource.isOpen()) {
				buildLayout(true);	
			}
		}
	}

	public DataSource getDataSet() {
		return this.dataSource;
	}

	private void buildLayout(final boolean createflds) {
		if(!Platform.isFxApplicationThread()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if(createflds) createFields();
					fillData();
				}
			});
		} else {
			if(createflds) createFields();
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
		// getItems may be null
		// Its throw exception (clear call sort method!)
		setItems(FXCollections.<Record>observableArrayList());
		getSortOrder().clear();
		ObservableList<TableColumn<Record, ?>> cols = getColumns();
		cols.clear();
		cols.add(check);
		//restore null
		setItems(null);
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
	public void linkActive(DataSourceLink dsl, ActiveMode active) {
		switch (active) {
		case Opening:
		case Refreshing:
			buildLayout(active==ActiveMode.Opening);
			break;
		case Closing:
			clearLayout();
			break;

		default:
			break;
		}
	}



	class RecordList extends TransformationList<Record,Record> implements SortableList<Record> {

		private int pageSize = dataSource.getMaxSizeDataPage();
		private Cursor cursor = ((DataSourceImpl) dataSource).getCursor();
		private List<DataPage> pages = cursor.getDataPages();
		private com.sun.javafx.collections.transformation.SortableList.SortMode mode = SortableList.SortMode.BATCH;
		private Comparator<? super Record> comparator;

		protected RecordList() {
			super(new ArrayList<Record>()); //stub
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
		public boolean setAll(Collection<? extends Record> list) {
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

		@Override
		public int getSourceIndex(int arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void onSourceChanged(Change<? extends Record> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Comparator<? super Record> getComparator() {
			return comparator;
		}

		@Override
		public SortableList.SortMode getMode() {
			return mode;
		}

		@Override
		public void setComparator(Comparator<? super Record> comparator) {
			this.comparator = comparator;
		}

		@Override
		public void setMode(SortableList.SortMode mode) {
			this.mode = mode;
			
		}

		@Override
		public void sort() {
			DataSourceImpl ds = ((DataSourceImpl) dataSource);
			// bug javafx: double call sort when click to column 
			// if columns(more than one) already in sort state
			if(ds.isRefreshProcess()) return;
			
			if(ds.isEof()) {
				// sort on client
				// all fetching and sorting on client
				ObservableList<Record> newlist = FXCollections.observableArrayList(this); 
		        FXCollections.sort(newlist, comparator);
		        // new simple list
		        DataGrid.this.setItems(newlist);
			} else {
				// reopen
				ArrayList<SortAttribute> sorting = new ArrayList<>();
				ObservableList<TableColumn<?, ?>> cols = ((TableColumnComparator)comparator).getColumns();
				for (TableColumn<?, ?> c : cols) 
					sorting.add(new SortAttribute(c.getId(),c.getSortType()==TableColumn.SortType.DESCENDING));
				ds.setSorting(sorting);
				ds.refresh();
			}
		}

	}


	private void requestPageData(DataPage page) {
		DataPageImpl dp = ((DataPageImpl)page);
		if(!dp.isRequest()) {
			startRequestData();
			dp.requestPageData(new Callback<CommandGetRecords.Response, Void>() {
				@Override
				public Void call(CommandGetRecords.Response r) {
					finishRequestData();
					return null;
				}
			}, new Callback<Exception, Void>() {

				@Override
				public Void call(Exception ex) {
					finishRequestData();
					return null;
				}
			});
		};
	}
	

	@Override
	public void startRequestData() {
		if(lockControlLayout==0) {
			DataGridSkin<?> skin = (DataGridSkin<?>)getSkin();
			if(skin!=null) {
				skin.requestLayout();
			}
		}
		lockControlLayout++;
	}
	
	@Override
	public void finishRequestData() {
		lockControlLayout--;
		if(lockControlLayout<=0) {
			DataGridSkin<?> skin = (DataGridSkin<?>)getSkin();
			if(skin!=null) {
				skin.requestLayout();
			}
			lockControlLayout = 0;
		};
	}
	
	public boolean isLockControl() {
		return lockControlLayout>0;
	}
}
