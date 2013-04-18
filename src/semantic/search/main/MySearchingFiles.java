package semantic.search.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import semantic.search.indexer.DictionaryIndexer;
import semantic.search.indexer.Indexer;
import semantic.search.query.Annotation;
import semantic.search.searcher.EntitySearcher;
import semantic.search.searcher.MySearcher;
import semantic.search.searcher.TermSearcher;

public class MySearchingFiles {
	
	public static void main(String [] args){
		
		String indexDir = "Dataset/lexical_base";
		TermSearcher searcher = new TermSearcher(indexDir);
	    
	    try{
	    String queries = null;
	    BufferedReader in = null;
	    in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
	    
	    while (true) {
			if (queries == null)                        // prompt the user
			    System.out.println("Enter query: ");
	
			String query = in.readLine();
	
			if (query == null || query.length() == -1)
			    break;
	
			query = query.trim();
			if (query.length() == 0)
			    break;
			
			Date start = new Date();
			ArrayList<Annotation> resultUrls = searcher.mysearch(query);
		    Date end = new Date();
		    
		    System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		    for(Annotation result:resultUrls){
		    	System.out.println(result.getAnnotation());
		    }
	    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}

}
