package org.bebrb.client.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LocalStore {
	
	public static File openStore() {
		return openStore(null);
	}

	public static File openStore(String domain) {
		String path = System.getProperty("user.home")+File.separator+".bebrb"+
					File.separator+(domain!=null?domain+File.separator:"");
		File f = new File(path);
		if(!f.exists())	f.mkdirs();
		return f;
	}
	
	public static File openOrCreateFile(String fname) throws IOException {
		File f = new File(openStore(),fname);
		if(!f.exists())	f.createNewFile();
		return f;
	}
	
	public static String readTextFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer(); 
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
		};
		return sb.toString();
	}

	public static void writeTextFile(File file, String text) throws IOException {
		try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(file), "utf-8"))) {
			writer.write(text);
		};
	}
}
