package utils;

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
		String path = openOrCreatePath();
		load(new FileInputStream(path+fileName));
	}
	
	public void save() throws FileNotFoundException, IOException {
		String path = openOrCreatePath();
		store(new FileOutputStream(path+fileName),null);
	}

	private String openOrCreatePath() throws IOException {
		String path = System.getProperty("user.home")+File.separator+".bebrb"+
				File.separator+domain+File.separator;
		File f = new File(path);
		if(!f.exists())	f.mkdirs();
		f = new File(path+fileName);
		if(!f.exists())	f.createNewFile();
		
		return path;
	}
}
