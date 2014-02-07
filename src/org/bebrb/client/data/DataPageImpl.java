package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.List;

import org.bebrb.data.DataPage;
import org.bebrb.data.Record;
import org.bebrb.server.net.CommandOpenDataSource;

public class DataPageImpl implements DataPage {
	private CommandOpenDataSource.Page page;
	private List<Record> records;
	private DataSourceImpl dataSource;
	
	public DataPageImpl(CommandOpenDataSource.Page page, DataSourceImpl ds) {
		this.page = page;
		dataSource = ds;
	}

	@Override
	public int getSize() {
		return page.getSize();
	}

	@Override
	public List<Record> getRecords() throws Exception {
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

}
