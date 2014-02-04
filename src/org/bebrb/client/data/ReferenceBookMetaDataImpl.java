package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bebrb.data.Attribute;
import org.bebrb.reference.ReferenceBookMetaData;
import org.bebrb.server.net.CommandGetAppContext;

public class ReferenceBookMetaDataImpl implements ReferenceBookMetaData {
	private String id;
	private String title;
	private boolean history;
	private ReferenceType type;
	private CacheControl cc;
	private List<Attribute> attrs = new ArrayList<Attribute>();
	private Attribute keyAttribute = null;
	private Attribute parentKey = null;
	private boolean сanChoiseFolder = false;
	private Date actualCacheDate;
	
	public static ReferenceBookMetaData createMetaData(CommandGetAppContext.RMetaData meta) {
		return new ReferenceBookMetaDataImpl(meta);
	}
	
	protected ReferenceBookMetaDataImpl(CommandGetAppContext.RMetaData meta) {
		id = meta.getId();
		title = meta.getName();
		history = meta.getHistoryAvailable();
		type = meta.getReferenceType();
		cc = meta.getCacheControl();
		
		List<CommandGetAppContext.Attribute> alist = meta.getAttributes();
		for (CommandGetAppContext.Attribute a : alist) {
			attrs.add(AttributeImpl.createAttribute(a, this));
			if(a.getKey())
				keyAttribute = attrs.get(attrs.size()-1);
			if(meta.getParentKey()!=null && meta.getParentKey().equalsIgnoreCase(a.getName()))
				parentKey = attrs.get(attrs.size()-1);
		}
		сanChoiseFolder = meta.getCanChoiseFolder();
		actualCacheDate = meta.getActualDate();
	}
	

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getReferenceTitle() {
		return title;
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public ReferenceType getReferenceType() {
		return type;
	}

	@Override
	public Date getActualDate() {
		return actualCacheDate;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attrs;
	}

	@Override
	public Attribute getKey() {
		return keyAttribute;
	}

	@Override
	public Attribute getParentKey() {
		return parentKey;
	}

	@Override
	public boolean isCanChoiseFolder() {
		return сanChoiseFolder;
	}

	@Override
	public CacheControl getCacheControl() {
		return cc;
	}

	@Override
	public boolean isHistoryAvailable() {
		return history;
	}

}
