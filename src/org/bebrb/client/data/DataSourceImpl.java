package org.bebrb.client.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bebrb.data.Attribute;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.OnValidate;
import org.bebrb.data.Record;

public class DataSourceImpl implements DataSource {

	public DataSourceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Attribute> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CacheControl getCacheControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getActualDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLazy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCanAdd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCanDelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCanEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<DataPage> open(Map<String, Object> params) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxSizeDataPage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Record findRecord(Object value, boolean onServer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record findRecord(Object value) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Record> findRecord(Map<Attribute, Object> values,
			boolean onServer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Record> findRecord(Map<Attribute, Object> values)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public boolean isPublished() {
		return true; //always public
	}

}
