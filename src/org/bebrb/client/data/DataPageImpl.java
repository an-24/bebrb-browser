package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Callback;

import org.bebrb.client.Cache;
import org.bebrb.client.Host;
import org.bebrb.data.DataPage;
import org.bebrb.data.Record;
import org.bebrb.server.net.CommandGetRecords;
import org.bebrb.server.net.CommandGetRecords.Response;
import org.bebrb.server.net.CommandOpenDataSource;

public class DataPageImpl implements DataPage {
	private CommandOpenDataSource.Page page;
	private List<Record> records;
	private DataSourceImpl dataSource;
	private boolean request;
	private int pageIndex;
	private Callback<Exception, Void> errorHandler;
	
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

	public void requestPageData(final Callback<CommandGetRecords.Response, Void> after) {
		Cache.getPage(dataSource.getHost(),dataSource.getSessionId(),
				dataSource.getCursor().getCursorId(),pageIndex,
				new Callback<CommandGetRecords.Response, Void>(){
					@Override
					public Void call(CommandGetRecords.Response r) {
						records = null;
						page = r.getPages().get(0);
						if(after!=null) after.call(r);
						return null;
					}
		},errorHandler);
	}

	public boolean isRequest() {
		return request;
	}

	public Callback<Exception, Void> getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(Callback<Exception, Void> errorHandler) {
		this.errorHandler = errorHandler;
	}

}
