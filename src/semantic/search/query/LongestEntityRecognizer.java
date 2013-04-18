package semantic.search.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import semantic.search.searcher.EntitySearcher;
import semantic.search.searcher.TermSearcher;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LongestEntityRecognizer {

	TermSearcher searcher;
	Searcher eSearcher;
	IndexReader eIndexReader;
	Searcher lSearcher;
	HashSet<String> stopWords;
	public LongestEntityRecognizer(String indexDir, String entityIndex){
		this.searcher = new TermSearcher(indexDir);
		try{
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(entityIndex)), true); 
		    eSearcher = new IndexSearcher(reader);
			IndexReader lreader = IndexReader.open(FSDirectory.open(new File(indexDir)), true); 
		    lSearcher = new IndexSearcher(lreader);
			stopWords = new HashSet(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			stopWords.add("who");
			stopWords.add("what");
			stopWords.add("how");
			stopWords.add("where");
			//System.out.println(stopWords);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ArrayList<Annotation> annotate(String text){
		text = text.toLowerCase();
		ArrayList<String> entities = getLongestEntities(text);
		
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		ArrayList<Annotation> finalAnnotations = new ArrayList<Annotation>();
		for(String str:entities){
			
			annotations.addAll(searcher.mysearch(str));
//			System.out.println(str+" "+annotations.size());
		}
		
		for(Annotation annotation:annotations){
//			System.out.println(annotation);
			float score = validateEntities(annotation,annotations);
//			constructEntity(annotation,annotations);
//			System.out.println(annotation.getScore()+" "+(score*annotation.getScore()+annotation.getScore()));
			annotation.setScore(score*annotation.getScore()+annotation.getScore());

			if(annotation.getScore()>0){
				finalAnnotations.add(annotation);
			}
		}
//		System.out.println(finalAnnotations);
		return finalAnnotations;
	}

	public ArrayList<String> getLongestEntities(String text) {
		ArrayList<String> entityString = new ArrayList<String>();
		entityString = findAll(text);
		return entityString;
	}
	public ArrayList<String> getEntities(String text) {
		ArrayList<String> entityString = new ArrayList<String>();
		Analyzer analyzer2 = new StopAnalyzer(Version.LUCENE_CURRENT);
		QueryParser parser2 = new QueryParser("", analyzer2);
		try {
			
			text = parser2.parse(text).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			String [] terms = text.split(" ");		
			for(int i = 0 ; i < terms.length; i++){
				String currentSearchTerm = "";
				String tryTerm  = "";
				int preEntitiesSize = 0;
				int currentSearchSize = 0;
				int j;
				for(j = i ; j < terms.length; j++){
					terms[j] = terms[j].replaceAll("\\W+","");
					if(terms[j].equals(""))
						continue;
					tryTerm = tryTerm + terms[j];
					preEntitiesSize = searcher.lookup(tryTerm, "");
						
						if(preEntitiesSize == 0){
							break;
						}
					currentSearchSize = preEntitiesSize ;
					currentSearchTerm = tryTerm;
					tryTerm += " ";
				}
				if((!currentSearchTerm.equals(""))){
//					System.out.println(currentSearchTerm+" "+currentSearchSize);
					entityString.add(currentSearchTerm);
					i = j-1;
				}
			}
			return entityString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public ArrayList<String> constructEntity(Annotation entity1, ArrayList<Annotation> entities){
		ArrayList<String> entity_parts = new ArrayList<String>();
		entity_parts.add(entity1.getAnnotation());
		for(Annotation e:entities){
			if(e.getKeyword().equals(entity1.getKeyword()))
					continue;
			float coOccuredEntity = getNumCoOccured(entity1.getAnnotation(), e.getAnnotation());
			if(coOccuredEntity > 0)
				entity_parts.add(e.getAnnotation());
		}
//		System.out.println(entity_parts);
		return entity_parts;
	}
	public float validateEntities(Annotation entity1, ArrayList<Annotation> entities){
		//get list of related entities of entity1 with score
		//for each of entity2 in entities
		//get list of related entities of entity2 with score
		//compute semantic relatedness
		float score = 0;
//		int relatedEntity1 = getNumRelatedEntity(entity1.getAnnotation());
		for(Annotation e:entities){
			if(e.getKeyword().equals(entity1.getKeyword()))
					continue;
			
//			int relatedEntity2 = getNumRelatedEntity(e.getAnnotation());
			float coOccuredEntity = getNumCoOccured(entity1.getAnnotation(), e.getAnnotation());
			
//			System.err.println(entity1.getAnnotation()+" "+ e.getAnnotation()+" "+coOccuredEntity);
//			float numEntities = relatedEntity1+relatedEntity2-coOccuredEntity;
//			if(numEntities > 0)
//				score += (float)coOccuredEntity;//((float)coOccuredEntity/numEntities);
//			float coOccuredEntityScore = getWeightedRelatedScore(entity1.getAnnotation(), e.getAnnotation());
			float currentScore = (2*(coOccuredEntity * e.getScore())/(coOccuredEntity + e.getScore()));
			if(currentScore > score){
				score = currentScore;
			}
		}
		score = (float)Math.log((double)score/entities.size());
		if(score <= 0)
			return 0;
		return score;
	}
//	private double computeJacarrdRelatedness(ArrayList<String> entities1, ArrayList<String> entities2){
//		
//		for(String e1:entities1)
//		
//		
//	}
	private float getWeightedRelatedScore(String entity1, String entity2){
		float score1 = typedRelationAnalysis_so(entity1, entity2);
		float score2 = typedRelationAnalysis_so(entity2, entity1);
//		float score3 = typedRelationAnalysis_oo(entity1, entity2);
//		System.out.println("Score 1: "+score1+" Score 2: "+score2+" Score 3: "+score3);
		return score1+score2;//+score3;
	}
	private float getNumCoOccured(String entity1, String entity2){
		
		int num_hits = 1000000;
		Term term = new Term("object_original",entity1);
		PhraseQuery thisPart1 = new PhraseQuery();
		thisPart1.add(term);
		Term term2 = new Term("object_original",entity2);
		PhraseQuery thisPart2 = new PhraseQuery();
		thisPart2.add(term2);
		BooleanQuery  luceneQuery1 = new BooleanQuery ();
		luceneQuery1.add(thisPart1,BooleanClause.Occur.MUST);
		luceneQuery1.add(thisPart2,BooleanClause.Occur.MUST);
		
		Term term3 = new Term("url",entity1);
		PhraseQuery thisPart3 = new PhraseQuery();
		thisPart3.add(term3);
		Term term4 = new Term("object_original",entity2);
		PhraseQuery thisPart4 = new PhraseQuery();
		thisPart4.add(term4);
		BooleanQuery  luceneQuery2 = new BooleanQuery ();
		luceneQuery2.add(thisPart3,BooleanClause.Occur.MUST);
		luceneQuery2.add(thisPart4,BooleanClause.Occur.MUST);
	
		Term term5 = new Term("url",entity2);
		PhraseQuery thisPart5 = new PhraseQuery();
		thisPart5.add(term5);
		Term term6 = new Term("object_original",entity1);
		PhraseQuery thisPart6 = new PhraseQuery();
		thisPart6.add(term6);
		BooleanQuery  luceneQuery3 = new BooleanQuery ();
		luceneQuery3.add(thisPart5,BooleanClause.Occur.MUST);
		luceneQuery3.add(thisPart6,BooleanClause.Occur.MUST);		
		
		BooleanQuery  luceneQuery = new BooleanQuery ();
		luceneQuery.add(luceneQuery3,BooleanClause.Occur.SHOULD);
		luceneQuery.add(luceneQuery2,BooleanClause.Occur.SHOULD);
		luceneQuery.add(luceneQuery1,BooleanClause.Occur.SHOULD);
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
		try {
//			System.out.println(luceneQuery);
			eSearcher.search(luceneQuery, collector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
//		for(int i = 0 ; i < hits.length ; i++){
//			String thisResult = "";
//			Document doc = null;
//			try {
//				doc = eSearcher.doc(hits[i].doc);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			String url = doc.get("url");
//			thisResult += url+" "+hits[i].score;
//			System.out.println(thisResult);
//		}
		
		return (float)hits.length;
	}
	private float typedRelationAnalysis_so(String entity1, String entity2){
		int num_hits = 1000000;
		float weighted_score = 0;
		Term term3 = new Term("url",entity1);
		PhraseQuery thisPart3 = new PhraseQuery();
		thisPart3.add(term3);
		Term term4 = new Term("object_original",entity2);
		PhraseQuery thisPart4 = new PhraseQuery();
		thisPart4.add(term4);
		BooleanQuery  luceneQuery = new BooleanQuery ();
		luceneQuery.add(thisPart3,BooleanClause.Occur.MUST);
		luceneQuery.add(thisPart4,BooleanClause.Occur.MUST);
		TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
		ScoreDoc[] hits;
		try {
//			System.out.println(luceneQuery);
			eSearcher.search(luceneQuery, collector);
			hits = collector.topDocs().scoreDocs;	
			
			if(hits.length ==0)
				return 0;
			
			for(int i = 0 ; i < hits.length; i++){
				Document doc = eSearcher.doc(hits[i].doc);
				String [] triples = doc.getValues("triple");
				float thisScore = 0;
				int count = 0;
				for(String triple:triples){
					if(triple.contains(entity2)){
						String url = triple.replaceAll(entity2, "").trim();
						thisScore += getWeightByURL(url);
						count++;
					}
				}
				weighted_score += thisScore/count;
			}
			return weighted_score/hits.length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	private float getWeightByURL(String url){
		int num_hits = 1000000;
		float weighted_score = 0;
		Term term = new Term("url",url);
		PhraseQuery luceneQuery = new PhraseQuery();
		luceneQuery.add(term);
		TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
		ScoreDoc[] hits;
		try {
//			System.out.println(luceneQuery);
			lSearcher.search(luceneQuery, collector);
			hits = collector.topDocs().scoreDocs;	
			
			if(hits.length ==0)
				return 0;
			
			for(int i = 0 ; i < hits.length; i++){
				Document doc = lSearcher.doc(hits[i].doc);
				weighted_score += doc.getBoost();			
			}
			return weighted_score/hits.length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;	
	}
	private float typedRelationAnalysis_oo(String entity1, String entity2){
		//e1 is subject
		int num_hits = 1000000;
		float weighted_score = 0;
		Term term = new Term("object_original",entity1);
		PhraseQuery thisPart1 = new PhraseQuery();
		thisPart1.add(term);
		Term term2 = new Term("object_original",entity2);
		PhraseQuery thisPart2 = new PhraseQuery();
		thisPart2.add(term2);
		BooleanQuery  luceneQuery = new BooleanQuery ();
		luceneQuery.add(thisPart1,BooleanClause.Occur.MUST);
		luceneQuery.add(thisPart2,BooleanClause.Occur.MUST);
		TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
		ScoreDoc[] hits;
		try {
//			System.out.println(luceneQuery);
			eSearcher.search(luceneQuery, collector);
			hits = collector.topDocs().scoreDocs;	
			
			if(hits.length ==0)
				return 0;
			
			for(int i = 0 ; i < hits.length; i++){
				Document doc = eSearcher.doc(hits[i].doc);
				float score_1 = typedRelationAnalysis_so(doc.get("url"),entity1);
				float score_2 = typedRelationAnalysis_so(doc.get("url"),entity2);
				weighted_score += ((score_1+score_2)/4);			
			}
			return weighted_score/hits.length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
		
	}
	private int getNumRelatedEntity(String entity){	
		try {
			int num_hits = 1000000;
			Term term = new Term("object_original",entity);
			PhraseQuery luceneQuery = new PhraseQuery();
			luceneQuery.add(term);
			TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits, false);
			eSearcher.search(luceneQuery, collector);
			return collector.topDocs().scoreDocs.length;
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	private String LuceneEscape(String str){
		str = str.replaceAll("http://", "");
		str = str.replaceAll("\\[", " ");
		str = str.replaceAll("\\]"," ");
		str = str.replaceAll("\\+"," ");
		str = str.replaceAll("-"," ");
		str = str.replaceAll("&&"," ");
		str = str.replaceAll("\\|\\|"," ");
		str = str.replaceAll("!"," ");
		str = str.replaceAll("\\("," ");
		str = str.replaceAll("\\)"," ");
		str = str.replaceAll("\\{"," ");
		str = str.replaceAll("\\}"," ");
		str = str.replaceAll("\\^"," ");
		str = str.replaceAll("\""," ");
		str = str.replaceAll("~"," ");
		str = str.replaceAll("\\*"," ");
		str = str.replaceAll("\\?"," ");
		str = str.replaceAll("\\\\"," ");
		str = str.replaceAll("\\.", " ");
		str = str.replaceAll("/"," ");
		str = str.replaceAll("#"," ");
		str = str.replaceAll(":"," ");
		return str;
	}

	public String concat(String[] slist, int start, int end) {
		StringBuilder ret = new StringBuilder();
		for (int i = start; i < end; ++i) {
			ret.append(slist[i]);
			if (i != end - 1)
				ret.append(" ");
		}
		return ret.toString();
	}

	public String findMaxPrefix(String[] parts) {

		for (int j = 0; j < parts.length; ++j)
			for (int i = parts.length; i > j; --i) {
				String sub = concat(parts, j, i);
				//System.out.println("try: "+sub);
				if (searcher.lookup(sub, "") >= 1)
					return sub;
			}
		return null;
	}

	public ArrayList<String> findAll(String str) {
		//System.out.println(str);
		if (str == null || str.equals(""))
			return new ArrayList<String>();
		
		
		String[] temp_parts = str.split(" ");
		int index = 0;
		while(stopWords.contains(temp_parts[0])){
//			System.out.println(temp_parts[0]);
			index = temp_parts[0].length()+1;			
			str = str.substring(index);
			temp_parts = str.split(" ");
		}
		String [] parts = str.split(" ");
		ArrayList<String> ret = new ArrayList<String>();
		String s = findMaxPrefix(parts);
		if (s == null)
			return ret;
		
		ret.add(s);
		
		index = str.indexOf(s) + s.length() + 1;
		if (index >= str.length())
			return ret;
		
		String nextString = str.substring(index);

		List<String> ls = findAll(nextString);
		ret.addAll(ls);
		return ret;
	}
}
