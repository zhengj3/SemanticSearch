package semantic.search.main;

import java.util.Date;

import semantic.search.indexer.DictionaryIndexer;
import semantic.search.indexer.EntityIndexer;
import semantic.search.indexer.Indexer;

public class IndexingFiles {
	
	public static void main(String [] args){
		Indexer indexer = new DictionaryIndexer();
		String sourceDataDir = "Dataset/lexical_base.nt";
		String indexDir = "Dataset/lexical_base_test";
	    Date start = new Date();
		indexer.index(sourceDataDir, indexDir);
	    Date end = new Date();
	    System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	}

}
