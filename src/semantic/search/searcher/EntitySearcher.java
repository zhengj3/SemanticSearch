package semantic.search.searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import semantic.search.query.QueryAnnotator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class EntitySearcher implements MySearcher{

	Hashtable<String, ArrayList<String>> concepts;
	Hashtable<String, ArrayList<String>> properties;
	Searcher searcher;
	int hitsPerPage;
	boolean doTermSearch;
	QueryParser parser;
	public EntitySearcher(String index){
		
	    String field = "contents";
	    String queries = null;
	    doTermSearch = false;
	    hitsPerPage = 10;
	    try{
		FileInputStream fstream = new FileInputStream("Dataset/dbpedia_3.8.owl");
		FileInputStream fstream2 = new FileInputStream("Dataset/infobox_property_definitions_en.nt");
		Model m = ModelFactory.createDefaultModel();
		m.read(fstream,"");
		m.read(fstream2,"","N-TRIPLE");
		concepts = new Hashtable<String, ArrayList<String>>(); 
		properties = new Hashtable<String, ArrayList<String>>(); 
		QueryAnnotator QA = new QueryAnnotator(m);
		concepts = QA.getConceptTable();
		properties = QA.getPropertyTable();	
	    IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)), true); 
	    searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
	    parser = new QueryParser(field, analyzer);
	    }catch(Exception e){
	    	
	    }
	}
	@Override
	public ArrayList<String> search(String query) {
		ArrayList<String> result = new ArrayList<String>();
		try{
			Query luceneQuery = parser.parse(query);
			result = doPagingSearch(searcher, luceneQuery, hitsPerPage, doTermSearch, result);
		}catch(Exception e){
			
		}
		return result;
	}
	public ArrayList<String> doPagingSearch(Searcher searcher, Query query, 
              int hitsPerPage, boolean doTermSearch, ArrayList<String> result) throws IOException {
			// Collect enough docs to show 5 pages
		  //ArrayList<String> objects = new ArrayList<String>();
			TopScoreDocCollector collector = TopScoreDocCollector.create(
			5 * hitsPerPage, false);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			int numTotalHits = collector.getTotalHits();

			
			int start = 0;
			int end = Math.min(numTotalHits, hitsPerPage);

			
			for (int i = start; i < end; i++) {			
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("url");
				Term term =null;
				if(doTermSearch){
					term = new Term("object_original",path);
			    	doTermSearch(term, searcher, query, hitsPerPage,  false,result);
				}else{
					result.add(path);
					if (path != null) {
						System.out.println((i+1) + ". " + path+" "+hits[i].score);
//						String title = doc.get("url");
//						if (doTermSearch == false) {
//							System.out.println("   url: " + doc.get("url"));
//					    	  String [] triples = doc.getValues("triple");
//					    	  for(String triple:triples){
//	//				    		  if(triple.contains("type")||triple.contains("abstract"))
//					    		  System.out.println(triple);
//					    	  }
//					    	 
//						}
					} else {
						System.out.println((i+1) + ". " + "No path for this document");
					}	
				}
			}
			

//			
//			if(doTermSearch){
//				String [] keys = query.toString().replaceAll("contents:","").split(" ");
//	    		for(String key:keys){
//					Term term = null;
//	    			if(key.charAt(key.length()-1)=='s')
//	    				key = key.substring(0, key.length()-1);
//	    			System.out.println(key+":");
//	    			if(concepts.keySet().contains(key)){
//		    			for(String url:concepts.get(key)){
////		    				System.out.print(url+" ");
//		    				term = new Term("object_original","<"+url+">");
//					    	doTermSearch(term, searcher, query, hitsPerPage, false);
//		    			}
//		    			System.out.println();
//	    			}else if(properties.keySet().contains(key)){
//		    			for(String url:properties.get(key)){
//		    				System.out.print(url+" ");
//		    				term = new Term("property_original","<"+url+">");
//					    	doTermSearch(term, searcher, query, hitsPerPage, false);
//		    			}
//		    			System.out.println();  
//	    			}
//			
//	    		}
//			}
	    		


			
			
			return result;
	  }
	  public void doTermSearch(Term term,Searcher searcher, Query query, 
              int hitsPerPage,  boolean interactive,ArrayList<String> result) throws IOException{
    	  Query termQuery = new WildcardQuery(term);
//    	  Query [] newQueries = {termQuery};
//    	  Query mynewQuery = query.combine(newQueries);
//    	  System.out.println("new query: "+query.toString());
	      doPagingSearch( searcher, termQuery, hitsPerPage, false,result);
//	      System.in.read();
	      //Term term = new Term("object_original","<http://dbpedia.org/class/yago/PeopleWhoHaveWalkedOnTheMoon>");

	  }
}
