package semantic.search.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class DictionaryIndexer implements Indexer{
	
	BufferedReader objectList;
	String currentLine;
	public DictionaryIndexer(){
		try{
	        FileInputStream fstream = new FileInputStream("Dataset/urlFrequency_sorted.txt");
	        DataInputStream in = new DataInputStream(fstream);
	        objectList = new BufferedReader(new InputStreamReader(in));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void index(String sourceDataDir, String indexDir) {
		try{

		      IndexWriter writer = new IndexWriter(FSDirectory.open(new File(indexDir)), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
		      System.out.println("Indexing to directory '" +indexDir+ "'...");
		      indexDocs(writer, new File(sourceDataDir));
		      System.out.println("Optimizing...");
		      writer.optimize();
		      writer.close();

		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	private void indexDocs(IndexWriter writer, File file){
	    if (file.canRead()) {
	      if (file.isDirectory()) {
	        String[] files = file.list();
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	        	System.out.print(i+" ");
	            indexDocs(writer, new File(file, files[i]));
	          }
	        }
	      } else {
		        System.out.println("adding " + file);
		        try{
		            FileInputStream fstream = new FileInputStream(file);
		            DataInputStream in = new DataInputStream(fstream);
		            BufferedReader br = new BufferedReader(new InputStreamReader(in));
		            String line;
		            Set<String> labels = new HashSet<String>();
		            String subject = "";
		            HashMap<String, Object> data = new HashMap<String, Object>();
		            
		            int count = 0;
		            while ((line= br.readLine())!= null ){			            	
		            	String [] spo = line.split(" ",3);
		            	
		            	if(spo.length < 3){
		            		continue;
		            	}
		            	
		            	if(!(spo[0].startsWith("<http")||spo[0].startsWith("_:")))
		            		continue;
		            	
		            	if(!(spo[1].startsWith("<http")||spo[1].startsWith("_:")))
		            		continue;	
		            	
		            	System.out.println(count+" line");
		            	count ++;
		            	if(!subject.equals(spo[0])){
		            		if(!subject.equals("")){
		            			data.put("url", subject);
		            			data.put("label", labels);
		            			System.out.println("adding "+subject);
		            			Document doc = getDoc(data);
		            			writer.addDocument(doc);
		            			if(count == 100000)
		            				break;
		            		}
		            		subject = spo[0];
		            		labels = new HashSet<String>();
		            	}
		            	addingLabels(labels,reviseString(spo[2]));
		            	//labels.add(reviseString(spo[2]));
		            }
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
	      }
	    }
	}
	public void close() throws Exception{
		objectList.close();
	}
	private Document getDoc(HashMap<String, Object> data){
		   Document doc = new Document();
		   doc.add(new Field("url", (String)data.get("url"), Field.Store.YES, Field.Index.NOT_ANALYZED));
		   HashSet<String> labels = (HashSet<String>)data.get("label");
		   for(String label:labels){
			   System.out.println(label);
			   doc.add(new Field("label",label, Field.Store.YES, Field.Index.NOT_ANALYZED));
			   doc.add(new Field("analyzedLabel",label, Field.Store.NO, Field.Index.ANALYZED));
		   }
		   float weight = getWeight((String)data.get("url"));
		   doc.setBoost(weight);
		   return doc;
	}
	private float getWeight(String url){
		int counts = 1;
		try{
			if(currentLine!=null && currentLine.equals(url)){
				counts++;
			}
			while((currentLine = objectList.readLine())!=null){
				int result = currentLine.compareTo(url);
				if(result == 0){
					counts++;
				}else if(result > 0){
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		float myWeight = (float)(1 + Math.log(counts));
		System.out.println(url+" weight: "+myWeight);
		return myWeight;		
	}
	private String reviseString(String str){
			return str.toLowerCase().replaceAll("^^<http://www.w3.org/2001/XMLSchema#.*","").replaceAll("@.*","").replaceAll("[^a-z0-9\\s]+"," ").replaceAll("\\s+"," ").trim();
	  }
	private void addingLabels(Set<String> hashSet, String label){
		StringBuilder labelBuilder = new StringBuilder();
		for(String label_part:label.split(" ")){
			labelBuilder.append(label_part);
			hashSet.add(labelBuilder.toString());
			labelBuilder.append(" ");
		}
		
	}
}
