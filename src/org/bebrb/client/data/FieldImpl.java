package org.bebrb.client.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.bebrb.data.Attribute;
import org.bebrb.data.Field;
import org.bebrb.data.Record;

public class FieldImpl<T> implements Field<T> {
	private T value;
	private Attribute attr;
	private Record record;
	private int index;

	@SuppressWarnings("unchecked")
	public FieldImpl(Attribute attr, Record record, int index) {
		this.attr = attr;
		this.record = record;
		this.index = index;
		value = (T) record.getValues().get(index);
	}
	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void getValue(ObjectOutputStream out) throws IOException {
		out.writeObject(value);
	}

	@Override
	public void setValue(T value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(ObjectInputStream input) throws IOException, ClassNotFoundException {
		value = (T) input.readObject();
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Attribute getAttribute() {
		return attr;
	}

	@Override
	public Record getRecord() {
		return record;
	}

}
