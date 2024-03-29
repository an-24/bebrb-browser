package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bebrb.client.utils.Resources;
import org.bebrb.data.Attribute;
import org.bebrb.data.DataPage;
import org.bebrb.data.DataSource;
import org.bebrb.data.Field;
import org.bebrb.data.Record;


public class RecordImpl implements Record {

	private Mode mode = Mode.Browse;
	private List<Object> data;
	private List<Field<?>> fields;
	private DataPageImpl page;
	private Field<?> key;
	private HashMap<String, Field<?>> fieldsIdxByName;
	private int fieldCount;

	public RecordImpl(DataPageImpl dp, List<Object> values) {
		this.page = dp;
		this.data = values;
		this.fieldCount = dp.getDataSource().getAttributes().size();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public List<Object> getValues() {
		return data;
	}

	@Override
	public List<Field<?>> getFields() {
		makeFields();
		return fields;
	}
	@Override
	public Field<?> getKey() {
		makeFields();
		return key;
	}

	@Override
	public Field<?> fieldByName(String fname) throws Exception {
		makeFields();
		Field<?> fld = fieldsIdxByName.get(fname);
		if(fld==null)
			throw new Exception(String.format(Resources.getBungles().getString("fieldNotFound"),fname));
		return fld;
	}

	@Override
	public Field<?> findField(String fname) {
		makeFields();
		return fieldsIdxByName.get(fname);
	}

	@Override
	public DataPage getDataPage() {
		return page;
	}

	@Override
	public DataSource getDataSource() {
		return page.getDataSource();
	}

	@Override
	public void commit() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	private void makeFields() {
		if(fields!=null) return;
		fields = new ArrayList<>(fieldCount);
		fieldsIdxByName = new HashMap<>(fieldCount); 
		List<Attribute> attrs = page.getDataSource().getAttributes();
		for (int i = 0, len = fieldCount; i < len; i++) {
			Attribute attr = attrs.get(i);
			Field<?> fld = new FieldImpl<>(attr, this, i);
			fields.add(fld);
			if(attr.isKey()) key = fld;
			fieldsIdxByName.put(attr.getName(), fld);
		}
	}

	public int getFieldCount() {
		return fieldCount;
	}

}
