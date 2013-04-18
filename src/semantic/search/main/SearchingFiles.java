package semantic.search.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import semantic.search.query.QueryAnnotator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SearchingFiles {
	
	
	static Hashtable<String, ArrayList<String>> concepts;
	static Hashtable<String, ArrayList<String>> properties;
	
	 public static void main(String[] args) throws Exception {

		    String index = "Dataset/entity_base_noPOstring";
		    String field = "";
		    String queries = null;
		    int hitsPerPage = 10;
		    boolean raw = false;
	
//			FileInputStream fstream = new FileInputStream("Dataset/dbpedia_3.8.owl");
//			FileInputStream fstream2 = new FileInputStream("Dataset/infobox_property_definitions_en.nt");
//    		Model m = ModelFactory.createDefaultModel();
//    		m.read(fstream,"");
//    		m.read(fstream2,"","N-TRIPLE");
//    		concepts = new Hashtable<String, ArrayList<String>>(); 
//    		properties = new Hashtable<String, ArrayList<String>>(); 
//    		QueryAnnotator QA = new QueryAnnotator(m);
//    		concepts = QA.getConceptTable();
//    		properties = QA.getPropertyTable();
		    
		    
		    IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)), true); 
		    Searcher searcher = new IndexSearcher(reader);
		    Analyzer wanalyzer = new WhitespaceAnalyzer();
		    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

		    BufferedReader in = null;
		    in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		    QueryParser parser = new QueryParser(field, analyzer);
		     // QueryParser parser = new QueryParser(field, analyzer);
		    while (true) {
		      if (queries == null)                        // prompt the user
		        System.out.println("Enter query: ");

		      String line = in.readLine();

		      if (line == null || line.length() == -1)
		        break;

		      line = line.trim();
		      if (line.length() == 0)
		        break;
//		      Term term = new Term("contents","alan");
//		      Term term = new Term("triple","<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/class/yago/PeopleWhoHaveWalkedOnTheMoon>");
		      Term term = new Term("object_original","<http://dbpedia.org/class/yago/PeopleWhoHaveWalkedOnTheMoon>");
////		      Term term = new Term("triple","<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/rss/1.0/item>");
		      Query query = null;

		      boolean doTermSearch = true;
		      if(!line.equals("contents")){
				    QueryParser mparser = new QueryParser(field, wanalyzer);		    	  
				    query = mparser.parse(line);
				    doTermSearch = false;
		      }else{
		    	  query = parser.parse(line);
		      }
//		      Collection<String> fields = reader.getFieldNames(IndexReader.FieldOption.ALL);
//		      for(String f:fields){
//		    	  System.out.println(f);
//		      }
////		      query = new WildcardQuery(term);
//		      query = parser.parse(line);
		      System.out.println("Searching for: " + query.toString(field));
		      Date start = new Date();
		      
		      doPagingSearch(in, searcher, query, hitsPerPage, raw, false, doTermSearch);
		      Date end = new Date();
		      System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
		  }
		    reader.close();
		  }
	  public static ArrayList<String> doPagingSearch(BufferedReader in, Searcher searcher, Query query, 
              int hitsPerPage, boolean raw, boolean interactive, boolean doTermSearch) throws IOException {
			// Collect enough docs to show 5 pages
		  ArrayList<String> objects = new ArrayList<String>();
			TopScoreDocCollector collector = TopScoreDocCollector.create(
			5 * hitsPerPage, false);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			int numTotalHits = collector.getTotalHits();
			System.out.println(query.toString());
			System.out.println(numTotalHits + " total matching documents");
			
			int start = 0;
			int end = Math.min(numTotalHits, hitsPerPage);
			
			while (true) {
			if (end > hits.length) {
			System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
			System.out.println("Collect more (y/n) ?");
			String line = in.readLine();
			if (line.length() == 0 || line.charAt(0) == 'n') {
			break;
			}
			
			collector = TopScoreDocCollector.create(numTotalHits, false);
			searcher.search(query, collector);
			hits = collector.topDocs().scoreDocs;
			}
			
			end = Math.min(hits.length, start + hitsPerPage);
			
			for (int i = start; i < end; i++) {			
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("url");
				Term term =null;
				if(doTermSearch){
					term = new Term("object_original",path);
			    	doTermSearch(term,in, searcher, query, hitsPerPage, raw, false);
				}else{
					objects.add(path);
					if (path != null) {
						System.out.println((i+1) + ". " + path+" "+hits[i].score);
//						String title = doc.get("url");
						if (doTermSearch == false) {
//							System.out.println("   url: " + doc.get("url"));
					    	  String [] triples = doc.getValues("triple");
					    	  for(String triple:triples){
//	//				    		  if(triple.contains("type")||triple.contains("abstract"))
					    		  System.out.println(triple);
					    	  }
					    	 
						}
					} else {
						System.out.println((i+1) + ". " + "No path for this document");
					}	
				}
			}
			
			break;
	
			}
			
			if(doTermSearch){
				String [] keys = query.toString().replaceAll("contents:","").split(" ");
	    		for(String key:keys){
					Term term = null;
	    			if(key.charAt(key.length()-1)=='s')
	    				key = key.substring(0, key.length()-1);
	    			System.out.println(key+":");
	    			if(concepts.keySet().contains(key)){
		    			for(String url:concepts.get(key)){
//		    				System.out.print(url+" ");
		    				term = new Term("object_original","<"+url+">");
					    	doTermSearch(term,in, searcher, query, hitsPerPage, raw, false);
		    			}
		    			System.out.println();
	    			}else if(properties.keySet().contains(key)){
		    			for(String url:properties.get(key)){
		    				System.out.print(url+" ");
		    				term = new Term("property_original","<"+url+">");
					    	doTermSearch(term,in, searcher, query, hitsPerPage, raw, false);
		    			}
		    			System.out.println();  
	    			}
			

	    		}


			}
			
			return objects;
	  }
	  public static void doTermSearch(Term term,BufferedReader in, Searcher searcher, Query query, 
              int hitsPerPage, boolean raw, boolean interactive) throws IOException{
    	  Query termQuery = new WildcardQuery(term);
    	  Query [] newQueries = {termQuery};
    	  Query mynewQuery = query.combine(newQueries);
    	  System.out.println("new query: "+query.toString());
	      doPagingSearch(in, searcher, termQuery, hitsPerPage, raw, termQuery == null, false);
//	      System.in.read();
	      //Term term = new Term("object_original","<http://dbpedia.org/class/yago/PeopleWhoHaveWalkedOnTheMoon>");

	  }
	  
}
