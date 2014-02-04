package org.bebrb.client.data;

import java.util.Map;

import org.bebrb.reference.ReferenceBook;
import org.bebrb.reference.ReferenceBookMetaData;
import org.bebrb.reference.View;
import org.bebrb.server.net.CommandGetAppContext;

public class ReferenceImpl implements ReferenceBook {
	private boolean canEdit;
	private boolean canAdd;
	private boolean canDelete;

	protected ReferenceImpl(CommandGetAppContext.Reference refBook) {
		//refBook.getMetaData().
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public ReferenceBookMetaData getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, View> getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getDefaultView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCanAdd() {
		return canAdd;
	}

	@Override
	public boolean isCanDelete() {
		return canDelete;
	}

	@Override
	public boolean isCanEdit() {
		return canEdit;
	}



}
