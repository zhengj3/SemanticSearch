package semantic.search.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;



import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;



public class ReadIndex {
	static String  INDEX_DIR = "Dataset/entity_base_noPOstring";
	public static void main(String [] args){
		try{
			
			IndexReader reader = IndexReader.open(FSDirectory.open(new File(INDEX_DIR)), true);
			while(true){
			    BufferedReader in = null;
			    String text = "";
			    try {
			    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					text = in.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    readIndexByTerm(reader,text);
			}

		}catch(Exception e){
			
			e.printStackTrace();
		}
	}
	public static void readIndexByTerm(IndexReader reader, String myterm) throws CorruptIndexException, IOException{
		String [] termpart = myterm.split(":",2);
		Term term = new Term(termpart[0], termpart[1]);
		TermDocs docs = reader.termDocs(term);
		while(docs.next()){	
			int docId = docs.doc();
			Document doc = reader.document(docId);
			String url = doc.get("url");
			String [] labels = doc.getValues("label");
			System.out.println("url: "+url);				
			for(String label:labels){
				System.out.println("	label: "+label);
			}
		    BufferedReader in = null;
		    String text = "";
		    try {
		    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
				text = in.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String [] triples = doc.getValues("triple");
			for(String triple:triples){
				System.out.println("	triple: "+triple);
			}
		}
	}
	public static void readIndex(IndexReader reader, String myterm) throws CorruptIndexException, IOException{
		int maxDoc = 100;
		TermEnum terms = reader.terms(); 

		while(terms.next()){
			Term term = terms.term();
			int numDocs = terms.docFreq();
			TermDocs docs = reader.termDocs(term);
//			System.out.println(term.toString()+" "+(term.toString().contains("object:")));
			if((!term.toString().contains(myterm) ))
				continue;
			
			System.out.println("term: "+term.toString()+" : "+numDocs);
			while(docs.next()){	
				int docId = docs.doc();
				Document doc = reader.document(docId);
				String url = doc.get("url");
				String [] labels = doc.getValues("label");
				System.out.println("url: "+url);				
				for(String label:labels)
					System.out.println("	triple: "+label);

			}
		}
	}

}
