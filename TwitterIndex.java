//import java.awt.TextField;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
//import java.lang.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.*;


public class TwitterIndex {
	static String path_name=System.getenv("HOME") + "/Index_Tweets";
	static String path_name2=System.getenv("HOME") + "/Twitter_Archive";
	/* Archive Directory variable. */
	static File archive = new File (path_name);
	
	static Analyzer analyzer;
	static IndexWriterConfig config;
	static IndexWriter writer;
	static Directory indexDir;
	static String everything;
	
	public static void main(String[] args) throws ParseException
	{	
		String tweets = System.getenv("HOME") + "/Twitter_Archive";
		System.out.println(tweets);
		try
		{
			System.out.println("top");
			indexDocs (writer, Paths.get(tweets));
		}
		catch (IOException e)
		{}
		
	}
	
	static void indexDocs(final IndexWriter writer, Path path) throws IOException
	{
	  if (Files.isDirectory(path)) 
	  {
	    Files.walkFileTree(path, new SimpleFileVisitor<Path>()
	    {
	      @Override
	      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	      {
	          index(writer, file, attrs.lastModifiedTime().toMillis());
	
	        return FileVisitResult.CONTINUE;
	      }
	    });
	  } 
	  else
	  {
	    index(writer, path, Files.getLastModifiedTime(path).toMillis());
	  }
	}
	
	static void index(IndexWriter writer, Path file, long lastModified) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file.toString()));
		String line = null;
		
		try {
			
			while ((line = br.readLine()) != null)
			{
		        StringBuilder sb = new StringBuilder();
		
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        String everything = sb.toString();
		       
		        line = br.readLine();
		        System.out.println("Screen Name: " + getScreen_name(everything));
	            System.out.println("Name: " + getName(everything));
	            System.out.println("Location: " + getLocation(everything));
	            System.out.println("Hashtags: " + getHashtags(everything));
	            System.out.println("Text: " +getText(everything));
	            System.out.println("----------------");
			}
		}finally{
			br.close();
		}
	}
	
//==============================================================================================================
    // Parses a tweet JSON string and returns name
    public static String getScreen_name (String json){
       
        int  start = json.indexOf("\"screen_name\":\"");
        int end = (json.substring(start+16)).indexOf("\"");
       
        String screen_name = json.substring(start+15,end+start+16);
       
        return screen_name;
        
    }
   
    public static String getName (String json){
        int start = json.indexOf("profile_sidebar_bord");
        
        json = json.substring(start+20);
       
        start = json.indexOf("\"name\":\"");
        int end = (json.substring(start+10)).indexOf("\"");
       
        String name = json.substring(start+8,end+start+10);
       
        return name;
    }
   
    // Parses a tweet JSON string and returns text
    public static String getText (String json){
        int start = json.indexOf("\"text\":\"");
        if (start == -1 )
            return null;
        int end = (json.substring(start+9)).indexOf("\"");
        
        String text = json.substring(start+8,end+start+9);
       
        return text;
    }
   
    // Parses a tweet JSON string and returns Location
    public static String getLocation (String json){
        int start = json.indexOf("\"full_name\":\"");
        int end = (json.substring(start+14)).indexOf("\"");
        if (start == -1 )
            return null;
        String Location = json.substring(start+13,end+start+14);
       
        return Location;
    }
   
    public static String getHashtags (String json){
        int start = json.indexOf("\"hashtags\":[{\"text\":");
        if (start == -1 )
            return null;
        int end = (json.substring(start+21)).indexOf("\"user_mentions\"");
	       
	        String hashes= json.substring(start+20,end+start+18);
	       
	        return hashes;
	    }  	
	
}