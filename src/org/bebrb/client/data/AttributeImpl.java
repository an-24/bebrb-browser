package org.bebrb.client.data;

import org.bebrb.data.Attribute;
import org.bebrb.data.BaseDataSet;
import org.bebrb.server.net.CommandGetAppContext;

public class AttributeImpl implements Attribute {

	private org.bebrb.server.net.CommandGetAppContext.Attribute originAttr;
	private BaseDataSet ds; 
	
	static public Attribute createAttribute(CommandGetAppContext.Attribute attr, BaseDataSet ds) {
		return new AttributeImpl(attr,ds);
	}

	
	protected AttributeImpl(CommandGetAppContext.Attribute attr, BaseDataSet ds) {
		this.originAttr = attr;
		this.ds = ds;
	}

	@Override
	public BaseDataSet getDataSource() {
		return ds;
	}

	@Override
	public String getName() {
		return originAttr.getName();
	}

	@Override
	public String getCaption() {
		return originAttr.getCaption();
	}

	@Override
	public Type getType() {
		return originAttr.getType();
	}

	@Override
	public boolean isKey() {
		return originAttr.getKey();
	}

	@Override
	public Attribute getForeignKey() {
		org.bebrb.server.net.CommandGetAppContext.Attribute fk = originAttr.getForeignKey();
		//FIXME определить источник данных
		return new AttributeImpl(fk,null);
	}

	@Override
	public boolean isVisible() {
		return originAttr.getVisible();
	}

	@Override
	public boolean isMandatory() {
		return originAttr.getMandatory();
	}

	@Override
	public int getMaxSizeChar() {
		return originAttr.getMaxSizeChar();
	}

}
