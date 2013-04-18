package semantic.search.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import semantic.search.searcher.EntitySearcher;
import semantic.search.searcher.SemiStructuredSearcher;
import semantic.search.searcher.TermSearcher;

public class EntitySearching {
	public static void main(String [] args){
		
		String indexDir = "Dataset/entity_base_noPOstring";
		String lexical_base = "Dataset/lexical_base";
		SemiStructuredSearcher searcher = new SemiStructuredSearcher(indexDir,lexical_base);
	    
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
			ArrayList<String> resultUrls = searcher.search(query);
		    Date end = new Date();
		    
		    System.out.println("found "+resultUrls.size()+" documents in "+ (end.getTime() - start.getTime()) + " total milliseconds");
		    for(String result:resultUrls){
		    	System.out.println(result);
		    }
	    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
}
