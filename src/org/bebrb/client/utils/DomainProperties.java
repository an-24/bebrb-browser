package org.bebrb.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("serial")
public class DomainProperties extends Properties {
	
	private String fileName;
	private String domain;

	public DomainProperties(String domain) throws FileNotFoundException, IOException {
		this(domain,"domain.properties");
	}
	
	public DomainProperties(String domain,String fname) throws FileNotFoundException, IOException {
		super();
		this.domain = domain; 
		this.fileName = fname;
		open();
	}

	public void open() throws FileNotFoundException, IOException {
		File f = openOrCreatePath();
		load(new FileInputStream(f));
	}
	
	public void save() throws FileNotFoundException, IOException {
		File f = openOrCreatePath();
		store(new FileOutputStream(f),null);
	}

	private File openOrCreatePath() throws IOException {
		File f = LocalStore.openStore(domain);
		f = new File(f,fileName);
		if(!f.exists())	f.createNewFile();
		return f;
	}
}
