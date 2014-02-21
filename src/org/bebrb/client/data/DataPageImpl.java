package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Callback;

import org.bebrb.client.Cache;
import org.bebrb.client.Client;
import org.bebrb.data.DataPage;
import org.bebrb.data.Record;
import org.bebrb.server.net.CommandGetRecords;
import org.bebrb.server.net.CommandOpenDataSource;

public class DataPageImpl implements DataPage {
	private CommandOpenDataSource.Page page;
	private List<Record> records;
	private DataSourceImpl dataSource;
	private boolean request;
	private int pageIndex;
	
	private Object mutexRequest = new Object();
	private Client queryRequest = null;
	
	public DataPageImpl(int pageIndex,CommandOpenDataSource.Page page, DataSourceImpl ds) {
		this.pageIndex = pageIndex;
		this.page = page;
		dataSource = ds;
	}

	@Override
	public int getSize() {
		return page.getSize();
	}

	@Override
	public List<Record> getRecords() {
		if(records==null) {
			List<List<Object>> data = page.getData();
			records = new ArrayList<>(data.size());
			for (List<Object> values : data) {
				RecordImpl r = new RecordImpl(this,values);
				records.add(r);
			}
		}
		
		return records;
	}

	@Override
	public boolean isEof() throws Exception {
		return page.getEof();
	}

	@Override
	public boolean isAlive() {
		return page.getAlive();
	}

	public DataSourceImpl getDataSource() {
		return dataSource;
	}

	public void requestPageData(final Callback<CommandGetRecords.Response, Void> after,
			final Callback<Exception,Void> errorHandler) {
		synchronized (mutexRequest) {
			request = true;
			queryRequest = Cache.getPage(dataSource.getHost(),dataSource.getSessionId(),
					dataSource.getCursor().getCursorId(),pageIndex,
					new Callback<CommandGetRecords.Response, Void>(){
						@Override
						public Void call(CommandGetRecords.Response r) {
							records = null;
							page = r.getPages().get(0);
							if(after!=null) after.call(r);
							setRequest(false);
							dataSource.RequestOfPage(null);
							return null;
						}
			},new Callback<Exception, Void>() {
				@Override
				public Void call(Exception ex) {
					setRequest(false);
					if(errorHandler!=null) errorHandler.call(ex);
					dataSource.RequestOfPage(null);
					return null;
				}
			});
			dataSource.RequestOfPage(queryRequest);
		}
	}

	public boolean isRequest() {
		synchronized (mutexRequest) {
			return request;
		}
	}

	public void setRequest(boolean request) {
		synchronized (mutexRequest) {
			this.request = request;
			if(!request) queryRequest = null;
		}
	}
	
	public void waitAlive() throws InterruptedException {
		synchronized (mutexRequest) {
			if(!isAlive() && queryRequest!=null) {
				queryRequest.waitFinish();
			}
		}
	}

	public void fetch() {
		requestPageData(null,null);
	}



}
