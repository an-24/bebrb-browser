package org.bebrb.client.data;

import java.util.AbstractList;
import java.util.List;

public class RecordWaiting extends RecordImpl {
	
	public RecordWaiting(DataPageImpl dp) {
		super(dp,null);
	}

	@Override
	public List<Object> getValues() {
		return new AbstractList<Object>() {
			@Override
		    public void add(int index, Object element) {
				// nothing
		    }
			@Override
			public Object get(int index) {
				return null;
			}
			@Override
			public int size() {
				return getFieldCount();
			}
		};
	}
}
