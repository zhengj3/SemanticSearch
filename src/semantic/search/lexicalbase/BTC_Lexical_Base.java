package semantic.search.lexicalbase;

import org.apache.lucene.index.IndexReader;

import semantic.search.searcher.TermSearcher;

public class BTC_Lexical_Base {
	
	IndexReader reader;
	TermSearcher searcher;
	public BTC_Lexical_Base(String indexDir){
		TermSearcher searcher = new TermSearcher(indexDir);
	}
	
	public int lookUp(String term,String Context){
		
		return 0;
	}
}
