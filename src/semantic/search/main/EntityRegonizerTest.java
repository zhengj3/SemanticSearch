package semantic.search.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

import semantic.search.query.Annotation;
import semantic.search.query.AnnotatoinComparable;
import semantic.search.query.LongestEntityRecognizer;
import semantic.search.searcher.TermSearcher;

public class EntityRegonizerTest {

	public static void main(String [] args){
		
		//String lexical_base = "Dataset/lexical_base_with_analyzedLabel";
		String lexical_base = "Dataset/lexical_base";
		String entity_base = "Dataset/entity_base_noPOstring";
		LongestEntityRecognizer ER = new LongestEntityRecognizer(lexical_base,entity_base);
		//String text = "Gerald R. Ford was laid to rest on the grounds of his presidential museum Wednesday after eight days of mourning and remembrance that spanned the country, from the California desert to the nation¡¯s capital and back to Ford¡¯s boyhood home.The sunset burial capped the official mourning for the 38th president after a 17-hour viewing Tuesday night and Wednesday at the museum in his hometown.At a graveside service that included a 21-gun salute and a 21-aircraft flyover, Vice President Dick Cheney presented former first lady Betty Ford with the American flag that was draped over her husband¡¯s casket.Earlier, Ford was remembered as a man not afraid to laugh, make tough decisions or listen to the advice of his independent wife in eulogies delivered during a funeral at the church the couple attended for six decades. An honor guard carried the casket inside Grace Episcopal Church, where Ford¡¯s defense secretary, Donald Rumsfeld, and Ford¡¯s successor, Jimmy Carter, recalled his public service.";
		
		String text = args[0];//"canadian army battles in operation charnwood".toLowerCase();
//		TermSearcher searcher = new TermSearcher(lexical_base);
//		ArrayList<HashMap<String, Float>> resultList = new ArrayList<HashMap<String, Float>>();
//		while(true){
//	    BufferedReader in = null;	    
//	    try {
//	    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
//			text = in.readLine();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		Analyzer analyzer2 = new StopAnalyzer(Version.LUCENE_CURRENT);
//		QueryParser parser2 = new QueryParser("", analyzer2);
//		try {
//			//text = parser2.parse(text).toString();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//System.out.println(text);
//		long start = System.currentTimeMillis();
		ArrayList<Annotation> annotations = ER.annotate(text);
		
		//Comparator comparator = new AnnotatoinComparable<Annotation>();
		//Collections.sort(annotations, comparator);
		Hashtable<String, Annotation> result = new Hashtable<String, Annotation> ();
		for(Annotation annotation:annotations){
			String keyword = annotation.getKeyword();
//			System.out.println(annotation);
			if(result.keySet().contains((keyword))){
				if(result.get(keyword).getScore() < annotation.getScore()){
					result.put(keyword, annotation);
				}
			}else{	
				result.put(keyword, annotation);
			}
		}
		for(String key:result.keySet()){
			System.out.println(result.get(key));
		}
		long end = System.currentTimeMillis();
//		System.out.println((end - start)+" ms");
//	}
	}

}
