
//import java.awt.TextField;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
//import java.lang.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.document.Field;



import java.util.Scanner;

public class Das_Search {

	/**
	 * @param args
	 */

	//======================= Directory where indexed files are going to be located ==========================
	//static String path_name = System.getenv("HOME") + "/Index_Tweets";
	
	static String path_name=System.getenv("HOME") + "\\Index_Tweets";
	/* Archive Directory variable. */
	static File archive = new File (path_name);
	
	static Analyzer analyzer;
	static IndexWriterConfig config;
	static IndexWriter writer;
	static Directory indexDir;
	static String everything;
	//==========================================================================================================
	
	
	//==============================================================================================================
	//Create Indexing Directory and Index
	//==============================================================================================================
	public static void Create_Index()
	{
		try {
			System.out.println("Home: "+System.getenv("HOME").toString());
			System.out.println ("Indexing to " + path_name + ".\n");
			indexDir = FSDirectory.open(Paths.get(path_name));
			
			analyzer = new StandardAnalyzer();
			config = new IndexWriterConfig(analyzer);
			
			// Create a new index in the directory, removing any
			// previously indexed documents:
			config.setOpenMode(OpenMode.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	//==============================================================================================================
	//==============================================================================================================

	public static void main(String[] args)
	{	
		archive.mkdir();
		
		Create_Index();

		String tweets = System.getenv("HOME") + "/Twitter_Archive";
		try
		{
			writer = new IndexWriter(indexDir, config);
			indexDocs (writer, Paths.get(tweets));
			
			//Search function
			writer.close();
			
			Searches ();
		}
		catch (IOException e)
		{}
	}
	
	//==============================================================================================================
	 /**
	 * Indexes the given file using the given writer, or if a directory is given,
	 * recurses over files and directories found under the given directory.
	 * 
	 * NOTE: This method indexes one document per input file.  This is slow.  For good
	 * throughput, put multiple documents into your input file(s).  An example of this is
	 * in the benchmark module, which can create "line doc" files, one document per line,
	**/
	//==============================================================================================================
	 static void indexDocs(final IndexWriter writer, Path path) throws IOException
	 {
	   if (Files.isDirectory(path)) 
	   {
	     Files.walkFileTree(path, new SimpleFileVisitor<Path>()
	     {
	       @Override
	       public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	       {
	           indexDoc(writer, file, attrs.lastModifiedTime().toMillis());

	         return FileVisitResult.CONTINUE;
	       }
	     });
	   } 
	   else
	   {
	     indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	   }
	 }
	//==============================================================================================================
	//==============================================================================================================
	 
	//==============================================================================================================
	  /** Indexes a single document */
	//==============================================================================================================
	  static void indexDoc(IndexWriter writer, Path file, long lastModified) 
	 {
	    try
	    { 
	    	System.out.println("adding " + file); 
	    	Document doc = new Document();
	        BufferedReader br = new BufferedReader(new FileReader(file.toString()));
	        ////////////////////// WHILE FILE HAS NOT BEEN FINISHED READING /////////////////////////////////////////
	        String line= null;

	        try 
	        {
			        while((line = br.readLine())!=null)
			        {
			            StringBuilder sb = new StringBuilder();
			          //  String line = null;
			            String empty_line = null; 
			         //   try {
			         //       line = br.readLine();
	
			        //    } catch (IOException e) {
			        //        e.printStackTrace();
			        //    }
		
			            sb.append(line);
			            sb.append(System.lineSeparator());
			          //  line = br.readLine();
			            //Trash lines we used to separate tweets////////////////
		                empty_line = br.readLine();
		                empty_line = br.readLine();
		                empty_line = br.readLine();
		                empty_line = br.readLine();
		                ///////////////////////////////////
			            everything = sb.toString();
			            
  
				      doc.add(new StringField("screen_name", getScreen_name(everything), Field.Store.YES));
				  //    System.out.println(getScreen_name(everything));

				      doc.add(new StringField("JSON", everything, Field.Store.YES));
				      doc.add(new StringField("name", getName(everything), Field.Store.YES));
			        doc.add(new StringField("location", getLocation(everything), Field.Store.YES));
				      if (getHashtags(everything) != null)
				    	  doc.add(new StringField("hastags", getHashtags(everything), Field.Store.YES));
				      doc.add(new StringField("text", getText(everything), Field.Store.YES)); 
			        }
				    //// END WHILE FILE HAS NOT BEEN FINISHED READING ////////////////////////////////////////////////////////
		    } 
	        finally
	        {
	            br.close();
	        }   	
	        
			 if (writer.getConfig().getOpenMode() == OpenMode.CREATE_OR_APPEND) 
			 {
			    // New index, so we just add the document (no old document can be there):
			    System.out.println("adding " + file);
			    writer.addDocument(doc);
			 } 
			 else 
			 {
			    // Existing index (an old copy of this document may have been indexed) so 
			    // we use updateDocument instead to replace the old one matching the exact 
			   // path, if present:
			    System.out.println("updating " + file);
			    writer.updateDocument(new Term("path", file.toString()), doc);
			 }
		     
		}
	    catch (IOException e)
	    {
		   e.printStackTrace();
	    }
	}
	//==============================================================================================================
	//==============================================================================================================

	  
	//==============================================================================================================
	 /** Searching */
	//==============================================================================================================
	  static void Searches ()
	  {
		  //String index = indexDir.toString();
		  String index = "Index_Tweets";
		 // int stop = index.indexOf("C:");
		 // int space = index.indexOf(" ");
		 // index = index.substring(stop+3,space);
		 Scanner in = new Scanner(System.in);
		 System.out.println("Enter Query:\n");
		 String query =in.nextLine();
		 
		  QueryParser queryparser = new QueryParser(query, analyzer);
		  Query q;
		  try 
		  {
			q = queryparser.parse(query);
			System.out.println("Searching for " + q.toString(query));
			int hitsPerPage = 10;
			try
			{
			//	System.out.println("Index: "+index);
			//	System.out.println("Paths: " + Paths.get(index));
			//	System.out.println("Here");
				
			  IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		//	  System.out.println("Here1");
			  IndexSearcher searcher = new IndexSearcher (reader);
		//	  System.out.println("Here2");
			  TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		//	  System.out.println("Here3");
			  searcher.search(q, 10 );
		//	  System.out.println("Here4");
			  ScoreDoc[] hits = collector.topDocs().scoreDocs;
			  
			  System.out.println( hits.length + ": Hits Found");
			  
			  System.out.println("Found " + hits.length + " hits.");
			  for ( int i = 0; i < hits.length; i++)
			  {
				  int docId = hits[i].doc;
				  Document docu = searcher.doc(docId);
				  System.out.println((i+1)+ ". " + docu.get("screen_name") + "\t" +docu.get("text"));
			  }
			  reader.close();
			}
			catch(IOException e)
			{}
		  }
		  catch (ParseException e1) 
		  {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		  }
	  }
//==============================================================================================================
//==============================================================================================================
	  
	  
//==============================================================================================================
	  /**
	   * Parse the tweet file sto only index the parts we want
	   * Screen Name
	   * name
	   * text
	   * location
	   * hashtags
	   */
//==============================================================================================================
		    // Parses a tweet JSON string and returns name
		    public static String getScreen_name (String json){
		        int start = json.indexOf("profile_sidebar_fill");
		        json = json.substring(start+20);
		       
		        start = json.indexOf("\"screen_name\":\"");
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
		        int end = (json.substring(start+9)).indexOf("\"");
		       
		        String text = json.substring(start+8,end+start+9);
		       
		        return text;
		    }
		   
		    // Parses a tweet JSON string and returns Location
		    public static String getLocation (String json){
		        int start = json.indexOf("\"full_name\":\"");
		        int end = (json.substring(start+14)).indexOf("\"");
		       
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