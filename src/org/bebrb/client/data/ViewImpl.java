package org.bebrb.client.data;

import org.bebrb.client.Host;
import org.bebrb.data.DataSource;
import org.bebrb.reference.View;
import org.bebrb.server.net.CommandGetAppContext;

public class ViewImpl implements View {
	private CommandGetAppContext.View view;
	private DataSource ds;
	private String sessisonId;
	private Host host;
	
	public static View createView(Host host,String sessisonId,CommandGetAppContext.View view) {
		return new ViewImpl(host,sessisonId,view);
	}
	
	protected ViewImpl(Host host,String sessisonId,CommandGetAppContext.View view) {
		this.view = view;
		this.sessisonId = sessisonId;
		this.host = host;
	}

	@Override
	public String getName() {
		return view.getName();
	}

	@Override
	public String getTitle() {
		return view.getTitle();
	}

	@Override
	public DataSource getDataSource() {
		if(ds==null)
			this.ds = (DataSource) DataSourceImpl.createDataSet(host,sessisonId,view);
		return ds;
	}

	@Override
	public void refresh() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getRoot() {
		return view.getRoot();
	}

	@Override
	public boolean isLazy() {
		return view.getLazy();
	}

}
