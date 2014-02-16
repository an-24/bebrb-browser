package org.bebrb.client.controls;

public interface ControlLink {
	public enum ActiveMode {Closing, Opening,Refreshing};
	public void linkActive(DataSourceLink dsl, ActiveMode active);
	public void startRequestData();
	public void finishRequestData();
}
