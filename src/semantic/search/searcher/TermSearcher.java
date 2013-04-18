package semantic.search.searcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import semantic.search.query.Annotation;

public class TermSearcher implements MySearcher{
	
	IndexReader reader;
	Searcher searcher;
	
	public TermSearcher(String indexDir){
		 try {
			 reader = IndexReader.open(FSDirectory.open(new File(indexDir)), true);
			 searcher = new IndexSearcher(reader);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public int lookup(String query, String context){
		ArrayList<String> results = new ArrayList<String>();    
		TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
		PhraseQuery luceneQuery = null;
		try {
			Term term = new Term("label",query);
			luceneQuery = new PhraseQuery();//parser.parse(query);
			luceneQuery.add(term);
			searcher.search(luceneQuery, collector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		return numTotalHits;
	}
	public ArrayList<Annotation> mysearch(String query) {
		ArrayList<Annotation> results = new ArrayList<Annotation>();    
		TopScoreDocCollector collector = TopScoreDocCollector.create(10, false);
		BooleanQuery luceneQuery = new BooleanQuery();
		try {
			Term term = new Term("label",query);
			TermQuery TermQuery = new TermQuery(term);//parser.parse(query);
			TermQuery.setBoost(100);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			QueryParser parser = new QueryParser("analyzedLabel", analyzer);
			Query parsedQuery = parser.parse(query);
//			System.out.println(query);
			luceneQuery.add(TermQuery, BooleanClause.Occur.SHOULD);
			luceneQuery.add(parsedQuery, BooleanClause.Occur.SHOULD);
			
			searcher.search(luceneQuery, collector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		
		int start = 0;
		int end = Math.min(numTotalHits, 10);
		
		for (int i = start; i < end; i++) {	
			String thisResult = "";
			Document doc = null;
			try {
				doc = searcher.doc(hits[i].doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String url = doc.get("url");
			thisResult += url;//+" "+hits[i].score;		
//			String [] labels = doc.getValues("label");		
//			for(String label:labels){
//				thisResult += label+"\n";	
//			}
			Annotation annotation = new Annotation(query, url, hits[i].score);
			results.add(annotation);		
		}
		return results;
	}
	@Override
	public ArrayList<String> search(String query) {
		ArrayList<String> results = new ArrayList<String>();
		String [] queryParts = query.split(":");
		System.out.println(queryParts[0]+" "+queryParts[1]);
		Term term = new Term(queryParts[0],queryParts[1]);
		try{
			if(reader==null)
				System.out.println("null");
			TermDocs docs = reader.termDocs(term);
			int numDocs = reader.docFreq(term);
			if(numDocs == 0)
				return results;
			System.out.println(numDocs);
			while(docs.next()){
				String thisResult = "";
				int docId = docs.doc();
				Document doc = reader.document(docId);
				String url = doc.get("url");
				String [] labels = doc.getValues("label");
				thisResult += url+"\n";				
				for(String label:labels){
					thisResult += label+"\n";	
				}
				results.add(thisResult);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}

}
