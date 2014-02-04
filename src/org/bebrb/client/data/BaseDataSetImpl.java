package org.bebrb.client.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.bebrb.data.Attribute;
import org.bebrb.data.BaseDataSet;
import org.bebrb.server.net.CommandGetAppContext;

public abstract class BaseDataSetImpl implements BaseDataSet {
	private String id;
	private String name;
	private List<Attribute> attributes =  new ArrayList<>();
	private Attribute key;
	private CacheControl cc;
	private Date actualDate;

	protected BaseDataSetImpl(CommandGetAppContext.DataSource ds) {
		this.id = ds.getId();
		this.name = ds.getName();
		List<CommandGetAppContext.Attribute> attrs = ds.getAttributes();
		for (CommandGetAppContext.Attribute a : attrs) {
			attributes.add(AttributeImpl.createAttribute(a,this));
			if(a.getKey())
				key = attributes.get(attributes.size()-1);
		}
		this.cc = ds.getCacheControl();
		this.actualDate = ds.getActualDate();
	}

	protected BaseDataSetImpl(CommandGetAppContext.View ds) {
		this.id = ds.getReferenceBook().getMetaData().getName()+"."+ds.getName();
		this.name = ds.getTitle();
		List<CommandGetAppContext.Attribute> attrs = ds.getReferenceBook().getMetaData().getAttributes();
		for (CommandGetAppContext.Attribute a : attrs) {
			attributes.add(AttributeImpl.createAttribute(a,this));
			if(a.getKey())
				key = attributes.get(attributes.size()-1);
		}
		this.cc = ds.getReferenceBook().getMetaData().getCacheControl();
		this.actualDate = ds.getReferenceBook().getMetaData().getActualDate();
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public Attribute getKey() {
		return key;
	}

	@Override
	public CacheControl getCacheControl() {
		return cc;
	}

	@Override
	public Date getActualDate() {
		return actualDate;
	}

}
