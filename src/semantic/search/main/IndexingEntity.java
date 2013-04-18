package semantic.search.main;

import java.util.Date;

import semantic.search.indexer.EntityIndexer;
import semantic.search.indexer.Indexer;

public class IndexingEntity {
	public static void main(String [] args){
		Indexer indexer = new EntityIndexer();
		String sourceDataDir = "btc_processed/btc_all/btc_all.nt";
//		String sourceDataDir = "Dataset/btc/btc-2009-chunk-002.nt";
		String indexDir = "Dataset/entity_base_noPOstring";
	    Date start = new Date();
		indexer.index(sourceDataDir, indexDir);
	    Date end = new Date();
	    System.out.println(end.getTime() - start.getTime() + " total milliseconds");
	}
}
