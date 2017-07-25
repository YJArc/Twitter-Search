
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
import org.apache.lucene.document.StoredField;
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
import java.io.FileOutputStream;
import java.io.PrintStream;



import java.util.Date;
import java.util.Scanner;
public class Twitter_Index {

	/**
	 * @param args
	 */

	//======================= Directory where indexed files are going to be located ==========================
	static String path_name=System.getenv("HOME") + "/Index_Tweets";
	static File archive = new File (path_name);
	
	static PrintStream console = System.out;
	static File names = new File(System.getenv("HOME") +"/USERNAMES.txt");

	static Analyzer analyzer;
	static IndexWriterConfig config;
	static IndexWriter writer;
	static Directory indexDir;
	static String everything;
	static Scanner in = new Scanner(System.in);
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
			
			while(true)
			{
			Searches ();
			System.out.println("Again y/n:");
			System.out.print("-> ");
			String input = in.next();
			if ( input == "n")
			{
				break;
			}
			}
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
	        BufferedReader br = new BufferedReader(new FileReader(file.toString()));
	        String line= null;
	        try 
	        {
			        while((line = br.readLine())!=null)
			        {
			        	Document doc = new Document();
			            StringBuilder sb = new StringBuilder();
			            String empty_line = null; 

			            sb.append(line);
			            sb.append(System.lineSeparator());
			            /////////Trash lines we used to separate tweets////////////////
		                empty_line = br.readLine();
		                
		                ///////////////////////////////////
			            everything = sb.toString();
			            
  
				      doc.add(new StringField("screen_name", getScreen_name(everything).toLowerCase(), Field.Store.YES));
				      doc.add(new TextField("JSON", everything.toLowerCase(), Field.Store.YES));
				      doc.add(new TextField("name", getName(everything).toLowerCase(), Field.Store.YES));
			          doc.add(new TextField("location", getLocation(everything).toLowerCase(), Field.Store.YES));
			          
				      if (getHashtags(everything) != null)
				    	  doc.add(new TextField("hastags", getHashtags(everything), Field.Store.YES));
				      
				      doc.add(new TextField("text", getText(everything).toLowerCase(), Field.Store.YES)); 
				      
				  	  if(!names.exists()){
						names.createNewFile();
				  	  }
				     FileOutputStream fos = new FileOutputStream(names,true);
					 PrintStream ps = new PrintStream(fos);
				      System.setOut(ps);
				      System.out.println(getScreen_name(everything));
				      System.out.println(getText(everything));
				      System.out.println("=====================================================\n");
				      System.setOut(console);
					 if (writer.getConfig().getOpenMode() == OpenMode.CREATE_OR_APPEND) 
					 {
					    // New index, so we just add the document (no old document can be there):
				//	    System.out.println("adding " + file);
					    writer.addDocument(doc);
					 } 
					 else 
					 {
					    // Existing index (an old copy of this document may have been indexed) so 
					    // we use updateDocument instead to replace the old one matching the exact 
					   // path, if present:
					    writer.updateDocument(new Term("path", file.toString()), doc);
					 }
				     
			        }
		    } 
	        finally
	        {
	            br.close();
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
		
		  String index = path_name;
		 System.out.println("Enter Query:\n");
		 String query =in.next();
		 
		  QueryParser queryparser = new QueryParser(query, analyzer);
		  Query q;
		  try 
		  {
			q = queryparser.parse(query);
			System.out.println("Searching for " + q.toString(query));
			int hitsPerPage = 10;
			try
			{
				
			  IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			 // System.out.println(reader.document(1));
			  IndexSearcher searcher = new IndexSearcher (reader);
			  TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
			  searcher.search(q, 10 );
			  
			  TopDocs topDocs = searcher.search(q,10);
			  //ScoreDoc[] hits = collector.topDocs().scoreDocs;
			  ScoreDoc[] hits = topDocs.scoreDocs;
			  
			  System.out.println( hits.length + ": Hits Found");
			  
			  System.out.println("Found " + hits.length + " hits.");
			  for ( int i = 0; i < hits.length; i++)
			  {
				  int docId = hits[i].doc;
				  Document docu = searcher.doc(docId);
				//  System.out.println(reader.document(docId));
				  System.out.println((i+1)+ ". " + docu.get("screen_name") + "\t" +docu.get("text"));
			  }
			  reader.close();
			}
			catch(IOException e)
			{}
		  }
		  catch (ParseException e1) 
		  {
			e1.printStackTrace();
		  } 	  
	  }
//==============================================================================================================
//==============================================================================================================
	  
	  
//==============================================================================================================
	  /**
	   * Parse the tweet file to only index the parts we want
	   * Screen Name
	   * name
	   * text
	   * location
	   * hashtags
	   */
//==============================================================================================================
		    // Parses a tweet JSON string and returns name
		    public static String getScreen_name (String json){
		        //int start = json.indexOf("profile_sidebar_fill");
		        //json = json.substring(start+20);
		       
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