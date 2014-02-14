package org.bebrb.client.controls;

public interface ControlLink {
	public void linkActive(DataSourceLink dsl, boolean active);
	public void startRequestData();
	public void finishRequestData();
}
