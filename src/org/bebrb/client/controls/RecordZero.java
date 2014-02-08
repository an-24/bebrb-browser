package org.bebrb.client.controls;

import java.util.List;

import org.bebrb.client.data.DataPageImpl;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.Field;
import org.bebrb.data.Record;

public class RecordZero implements Record {

	public RecordZero(DataPageImpl dp) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Field<?>> getFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> fieldByName(String fname) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> findField(String fname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataPage getDataPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commit() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
