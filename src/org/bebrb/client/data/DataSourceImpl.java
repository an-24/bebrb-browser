package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.util.Callback;

import org.bebrb.client.Cache;
import org.bebrb.client.Cache.Cursor;
import org.bebrb.client.Client;
import org.bebrb.client.Host;
import org.bebrb.client.controls.ControlLink;
import org.bebrb.client.controls.DataSourceLink;
import org.bebrb.client.controls.ControlLink.ActiveMode;
import org.bebrb.data.Attribute;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.OnValidate;
import org.bebrb.data.Record;
import org.bebrb.server.data.DataSourceImpl.SortAttribute;
import org.bebrb.server.net.CommandGetAppContext;
import org.bebrb.server.net.CommandOpenDataSource;
import org.bebrb.server.net.CommandOpenDataSource.Page;
import org.bebrb.server.net.CommandOpenDataSource.Response;

public class DataSourceImpl extends BaseDataSetImpl implements DataSource, DataSourceLink {
	private boolean lazy;
	private boolean readonly;
	private boolean canEdit;
	private boolean canAdd;
	private boolean canDelete;
	private int maxSizeDataPage;
	private String sessionId;
	private List<SortAttribute> sorting;
	private Cursor cursor;
	private Host host;
	private Client currentQuery;
	private boolean active;
	private boolean eof = true;
	private HashSet<ControlLink> linkControls = new HashSet<>();
	private Map<String, Object> safeParams;
	private OnOpen safeOpenCallback;
	private Boolean refreshing = false;

	public static DataSource createDataSet(Host host,String sessionId,CommandGetAppContext.DataSource ds) {
		return new DataSourceImpl(host,sessionId,ds);
	}

	public static DataSource createDataSet(Host host, String sessionId,CommandGetAppContext.View view) {
		return new DataSourceImpl(host,sessionId,view);
	}
	
	protected DataSourceImpl(Host host, String sessionId, CommandGetAppContext.DataSource ds) {
		super(ds);
		this.lazy = ds.getLazy();
		this.readonly = ds.getReadOnly();
		this.canEdit = ds.getCanEdit();
		this.canAdd = ds.getCanAdd();
		this.canDelete = ds.getCanDelete();
		this.maxSizeDataPage = ds.getMaxSizeDataPage();
		this.sessionId = sessionId;
		this.host = host;
	}

	protected DataSourceImpl(Host host,String sessionId, CommandGetAppContext.View view) {
		super(view);
		this.lazy = view.getLazy();
		this.canEdit = view.getReferenceBook().getCanEdit();
		this.canAdd = view.getReferenceBook().getCanAdd();
		this.canDelete = view.getReferenceBook().getCanDelete();
		this.readonly = !canEdit && !canAdd && !canDelete;
		this.maxSizeDataPage = DataSource.DEFAULT_PAGE_MAXSIZE;
		this.sessionId = sessionId;
		this.host = host;
	}

	@Override
	public boolean isPublished() {
		return true; //always for client
	}

	@Override
	public boolean isLazy() {
		return lazy;
	}

	@Override
	public boolean isReadOnly() {
		return readonly;
	}

	@Override
	public boolean isCanAdd() {
		return canAdd;
	}

	@Override
	public boolean isCanDelete() {
		return canDelete;
	}

	@Override
	public boolean isCanEdit() {
		return canEdit;
	}

	@Override
	public int getMaxSizeDataPage() {
		return maxSizeDataPage;
	}

	@Override
	public void open(Map<String, Object> params, final OnOpen callback) {
		if(active) return;
		
		safeParams = params;
		safeOpenCallback = callback;
		
		cursor = null;
		final Callback<Exception, Void> openError = new Callback<Exception, Void>() {
			@Override
			public Void call(Exception ex) {
				// notify controls
				for (ControlLink c : linkControls) c.finishRequestData();
				callback.onError(ex);
				currentQuery = null;
				refreshing = false;
				return null;
			}
			
		};
		Callback<Response, Void> openHandler = new Callback<CommandOpenDataSource.Response, Void>() {
			@Override
			public Void call(CommandOpenDataSource.Response r) {
				List<Page> sourcePages = r.getPages();
				List<DataPage> pages =  new ArrayList<>(sourcePages.size());
				int idx = 0;
				for (final Page page : sourcePages) {
					DataPage dp = new DataPageImpl(idx,page,DataSourceImpl.this);
					pages.add(dp);
					idx++;
				}
				cursor = new Cursor(r.getCursorId(),getCacheControl(),pages,r.getRecordCount());
				active = true;
				eof = sourcePages.get(sourcePages.size()-1).getAlive();
				// notify controls
				for (ControlLink c : linkControls) {
					c.finishRequestData();
					c.linkActive(DataSourceImpl.this, refreshing?ActiveMode.Refreshing:ActiveMode.Opening);
				}
				callback.onAfterOpen();
				currentQuery = null;
				refreshing = false;
				return null;
			}
		};
		// notify controls
		for (ControlLink c : linkControls) c.startRequestData();
		// open
		if(getCacheControl() == CacheControl.None) {
			currentQuery = Cache.openDirect(host, sessionId,getId(),params,maxSizeDataPage,sorting,openHandler,openError);			
		} else {
			currentQuery = Cache.openFromCache(host, getCacheControl(), getActualDate(), sessionId,getId(),params,maxSizeDataPage,sorting,openHandler,openError);
		}	
	}

	public void refresh() {
		// columns don't need remove
		//if(active) close();
		active = false;
		refreshing = true;
		open(safeParams,safeOpenCallback);
	}

	@Override
	public void stop() {
		if(currentQuery!=null) {
			currentQuery.interrupt();
			currentQuery = null;
		}	
	}

	@Override
	public void close() {
		if(!active) return;
		active = false;
		eof = true;
		// notify controls
		for (ControlLink c : linkControls) c.linkActive(DataSourceImpl.this, ActiveMode.Closing); 
		cursor = null;
	}

	@Override
	public boolean isOpen() {
		return active;
	}


	@Override
	public Record findRecord(Object value, boolean onServer) throws Exception {
		Map<Attribute, Object> vmap = new HashMap<>();
		vmap.put(getKey(), value);
		List<Record> recs = forRecords(vmap,onServer,true);
		return recs.size()>0?recs.get(0):null;
	}

	@Override
	public Record findRecord(Object value) throws Exception {
		return findRecord(value,false);
	}

	@Override
	public List<Record> findRecord(Map<Attribute, Object> values,
			boolean onServer) throws Exception {
		return forRecords(values,onServer,false);
	}

	@Override
	public List<Record> findRecord(Map<Attribute, Object> values)
			throws Exception {
		return findRecord(values,false);
	}

	
	private List<Record> forRecords(Map<Attribute, Object> values,
			boolean onServer, boolean firsrRecord)  throws Exception {
		List<Record> outrecs = new ArrayList<>();
		
		int[] mapIdx = new int[values.size()];
		Object[] samples = values.values().toArray();
		
		int i = 0;
		for (Attribute attr : values.keySet()) {
			mapIdx[i] = attr.getFieldNo();
			i++;
		};
		
		List<DataPage> pages = cursor.getDataPages();
		for (DataPage dp : pages) {
			if(!dp.isAlive()) {
				if(onServer) {
					((DataPageImpl)dp).requestPageData(null, new Callback<Exception, Void>() {
						@Override
						public Void call(Exception ex) {
							safeOpenCallback.onError(ex);
							return null;
						}
					});
					((DataPageImpl)dp).waitAlive();
				} else
					break; 
			}
			List<Record> recs = dp.getRecords();
			for (Record r : recs) {
				List<Object> vlist = r.getValues();
				boolean predict = true;
				for (int j = 0; j < mapIdx.length; j++) {
					Object source = vlist.get(mapIdx[j]);
					Object sample = samples[i];
					predict = predict && source==null?sample==null:
								sample==null?false:source.equals(sample);
					if(!predict) break;
				}
				if(predict) {
					outrecs.add(r);
					if(firsrRecord) break;
				}	
			}
		}
		return outrecs;
	}

	@Override
	public Record edit(Record r) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record add() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record add(Record r) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record delete(Record r) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(List<Record> records, OnValidate onvalidate)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(List<Record> records) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addValidator(OnValidate onvalidate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeValidator(OnValidate onvalidate) {
		// TODO Auto-generated method stub
		
	}

	public List<SortAttribute> getSorting() {
		return sorting;
	}

	public void setSorting(List<SortAttribute> sorting) {
		this.sorting = sorting;
	}

	public void setMaxSizeDataPage(int maxSizeDataPage) {
		this.maxSizeDataPage = maxSizeDataPage;
	}

	public Cursor getCursor() {
		return cursor;
	}

	@Override
	public void registerControl(ControlLink control) {
		this.linkControls.add(control);
	}

	@Override
	public void unRegisterControl(ControlLink control) {
		this.linkControls.remove(control);
	}

	@Override
	public int getRecordCount() {
		return cursor==null?0:cursor.getRecordCount();
	}

	public String getSessionId() {
		return sessionId;
	}

	public Host getHost() {
		return host;
	}

	public boolean isEof() {
		return eof;
	}

	public void RequestOfPage(Client query) {
		currentQuery = query;
	}

	public boolean isRefreshProcess() {
		synchronized(refreshing) {
			return refreshing;
		}
	}


}
