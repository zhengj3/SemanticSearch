package semantic.search.searcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import semantic.search.query.Annotation;
import semantic.search.query.LongestEntityRecognizer;
import semantic.search.query.LongestEntityRecognizerUnWeighted;


public class SemiStructuredSearcher implements MySearcher{

	static float StringQueryWeight = 1;
	static int num_hits = 50;
	Searcher searcher; 
	QueryParser parser;
	QueryParser filter_parser;
	TermSearcher annotator;
	LongestEntityRecognizer ER;
	Set<String> sameAsProperty;
	public SemiStructuredSearcher(String index, String lexical_base){
		try{
			String field = "contents";
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)), true); 
		    searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		    parser = new QueryParser(field, analyzer);
		    filter_parser = new QueryParser("", analyzer);
		    annotator = new TermSearcher(lexical_base);
		    ER = new LongestEntityRecognizer(lexical_base,index);
		    intializeSameAs();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public ArrayList<String> search(String query) {
		ArrayList<String> results = new ArrayList<String>();
		try{
			
			BooleanQuery  luceneQuery = new BooleanQuery ();
			Query contentQuery = parser.parse(QueryParser.escape(query));
			//query = filter_parser.parse(query).toString();
//			System.out.println(query);

			long startt =System.currentTimeMillis();
			ArrayList<Annotation> queryParts =ER.annotate(query);
			long endt =System.currentTimeMillis();
			
			System.out.println("annotation takes "+(endt - startt)+queryParts.size());
			
			for(Annotation queryPart:queryParts){
				Term term = new Term("object_original",queryPart.getAnnotation());
				PhraseQuery thisQuery = new PhraseQuery();
				thisQuery.add(term);
				thisQuery.setBoost(queryPart.getScore()*StringQueryWeight+StringQueryWeight);
				luceneQuery.add(thisQuery,BooleanClause.Occur.SHOULD);
				
				Term term2 = new Term("url",queryPart.getAnnotation());
				PhraseQuery urlQuery = new PhraseQuery();
				urlQuery.add(term2);
				//urlQuery.setBoost(100000000);
				luceneQuery.add(thisQuery,BooleanClause.Occur.SHOULD);				
			}
			contentQuery.setBoost(StringQueryWeight);

			luceneQuery.add(contentQuery,BooleanClause.Occur.SHOULD);
	//		System.out.println(luceneQuery.toString());
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
			searcher.search(luceneQuery, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			int start = 0;
			
			for (int i = start; i < hits.length; i++) {	
				String thisResult = "";
				Document doc = null;
				try {
					doc = searcher.doc(hits[i].doc);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String url = doc.get("url");
				thisResult += url;//+" "+hits[i].score;
				results.add(thisResult);
				String [] triples = doc.getValues("triple");
				for(String triple:triples){
					String newUrl = getSameAs(triple);
					if(newUrl != null){
						results.add(newUrl);
					}
				}
						
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}
	private String getSameAs(String triple){
		String [] po = triple.split(" ",2);
		if(po.length !=2)
			return null;
		if(sameAsProperty.contains(po[0])){
			return po[1];
		}
		return null;	
	}
	private void intializeSameAs(){
		sameAsProperty = new HashSet<String>();
		sameAsProperty.add("<http://xmlns.com/foaf/0.1/page>");
		sameAsProperty.add("<http://dbpedia.org/property/redirect>");
		sameAsProperty.add("<http://www.w3.org/2002/07/owl#sameAs>");
		sameAsProperty.add("<http://del.icio.us/laprice#sameas>");
		sameAsProperty.add("<http://dbpedia.org/property/page>");

		
	}
	private String luceneQueryString(String str){
		return str.replaceAll("\\:", "\\\\:");
	}
	

}
