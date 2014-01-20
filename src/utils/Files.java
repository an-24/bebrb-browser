package utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Files {
	
	public static String getProfilePath() {
		return System.getProperty("user.home")+File.separator+".bebrb";
	}
	
	public static File openOrCreateFile(String fname) throws IOException {
		String path = getProfilePath()+File.separator;
		File f = new File(path);
		if(!f.exists())	f.mkdirs();
		f = new File(path+fname);
		if(!f.exists())	f.createNewFile();
		return f;
	}
	
	public static String readFile(File f) throws IOException
	{
	   String content;
       try(FileReader reader = new FileReader(f)){
    	   char[] chars = new char[(int) f.length()];
    	   reader.read(chars);
    	   content = new String(chars);
       };
	   return content;
	}	

}
