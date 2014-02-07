package org.bebrb.client.controls;

public interface DataSourceLink {
	public void registerControl(ControlLink control);
	public void unRegisterControl(ControlLink control);
	public int getRecordCount();

}
