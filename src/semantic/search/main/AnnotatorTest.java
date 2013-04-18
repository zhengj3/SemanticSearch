package semantic.search.main;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import semantic.search.query.QueryAnnotator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class AnnotatorTest {
	public static void main(String [] args){
		
		try{
			FileInputStream fstream = new FileInputStream("Dataset/dbpedia_3.8.owl");
			FileInputStream fstream2 = new FileInputStream("Dataset/infobox_property_definitions_en.nt");
			FileInputStream fstream3 = new FileInputStream("Dataset/labels.nt");
    		Model m = ModelFactory.createDefaultModel();
    		m.read(fstream,"");
    		m.read(fstream2,"","N-TRIPLE");
    		m.read(fstream3,"","N-TRIPLE");
    		Hashtable<String, ArrayList<String>> concepts = new Hashtable<String, ArrayList<String>>(); 
    		Hashtable<String, ArrayList<String>> properties = new Hashtable<String, ArrayList<String>>();
    		Hashtable<String, ArrayList<String>> resources = new Hashtable<String, ArrayList<String>>();
    		QueryAnnotator QA = new QueryAnnotator(m);
    		concepts = QA.getConceptTable();
    		properties = QA.getPropertyTable();
    		resources = QA.getResourceTable();
    		//for(String key:output.keySet()){
    		String [] keys = "United States".toLowerCase().split(" ");
    		String tryTerm = "";
    		String preTerm = "";
    		for(String key:resources.keySet()){
    			System.out.println(key);
    		}
    		int i = 0;
    		while(i < keys.length){
    			String key = keys[i];
    			if(key.charAt(key.length()-1)=='s')
    				key = key.substring(0, key.length()-1);
    			
				tryTerm += key.toLowerCase();
    			System.out.println("tryTerm: "+tryTerm);
    			if(resources.keySet().contains(tryTerm)){
    				System.out.println(" found "+resources.get(key));
    				preTerm = tryTerm;
    				tryTerm += " ";
    				i++;
    			}else{
    				if(preTerm.equals(""))
    					i++;
    				System.out.println("preTerm: "+preTerm);
    				preTerm = "";
    				tryTerm = "";
    			}
//    			if(concepts.keySet().contains(key)){
//	    			for(String url:concepts.get(key)){
//	    				System.out.print(url+" ");
//	    			}
//	    			System.out.println();
//    			}else if(properties.keySet().contains(key)){
//	    			for(String url:properties.get(key)){
//	    				System.out.print(url+" ");
//	    			}
//	    			System.out.println();  				
//    			}
    		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
