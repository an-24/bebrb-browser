package org.bebrb.client.utils;

public interface DataFilter<T> {
	public boolean filter(T record);
}
