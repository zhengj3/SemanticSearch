package semantic.search.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class EntityIndexer implements Indexer{

	@Override
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
		            StringBuilder contents = new StringBuilder();
		            StringBuilder triples = new StringBuilder();
		            StringBuilder labels = new StringBuilder();
		            String subject = "";
		            HashMap<String, String> data = new HashMap<String, String>();
		            Set<String> properties = new HashSet<String>();
		            Set<String> objects = new HashSet<String>();
		            
		            int count = 0;
		            while ((line= br.readLine())!= null ){	
		            	String [] spo = line.split(" ",3);
		            	
		            	if(!subject.equals(spo[0])){
		            		if(!subject.equals("")){
		            			data.put("contents", contents.toString());
		            			data.put("url", subject);
//		            			data.put("property", properties.toString());
//		            			data.put("object", objects.toString());
		            			data.put("triple", triples.toString());
		            			count ++;
		            			System.out.println(count+" adding "+subject);
		            			Document doc = getDoc(data);
		            			writer.addDocument(doc);
//		            			if(count == 50000)
//		            				break;
		            		}
		            		subject = spo[0];
		            		contents = new StringBuilder();	
		            		labels = new StringBuilder();
		            		triples =  new StringBuilder();
		            		labels.append(getLabel(subject));
		            		//properties = new HashSet<String>();
		            		objects = new HashSet<String>();
		            	}
		            	//contents.append(getLabel(subject)+" ");
		            	//properties.add(getLabel(spo[1]));	            	
		            	triples.append(spo[1]);
		            	triples.append(" ");
		            	triples.append(spo[2]);
		            	triples.append(" ");
		            	if(!spo[2].startsWith("<http") && !spo[2].startsWith("_:")){
		            		contents.append(spo[2]);
		            		contents.append(" ");
//		            		objects.add(spo[2]);
		            	}else{
//		            		String mylabel = getLabel(spo[2]);
//		            		System.out.println(mylabel);
//		            		objects.add(getLabel(spo[2]));
		            	}
//		            	contents.append(IndexFiles.reviseString(line));
//		            	contents.append("\n");
//		            	if(url.equals("")){
//		            		triples.append((line+" "+currentProperty+" "+line).replaceAll("[\n]+", " ")+" .\n");
//		            	}else{
//		            		if(currentProperty.equals(KEAConfiguration.DOMAIN_PROPERTY) || currentProperty.equals(KEAConfiguration.RANGE_PROPERTY))
//		            			line = line.split(" ")[0];
//		            		triples.append((url+" "+currentProperty+" "+line).replaceAll("[\n]+", " ")+" .\n");
//		            	}
		            }
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
	      }
	    }
	}
	private Document getDoc(HashMap<String, String> data){
		   Document doc = new Document();
		   doc.add(new Field("url", data.get("url"), Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("contents", data.get("contents"), Field.Store.NO, Field.Index.ANALYZED));
//		   doc.add(new Field("property", data.get("property"), Field.Store.NO, Field.Index.ANALYZED));
//		   System.out.println(data.get("object"));
//		   doc.add(new Field("object", data.get("object"), Field.Store.NO, Field.Index.ANALYZED));
//		   System.out.println(data.get("triple")+" "+data.get("url"));
		   String [] triples = data.get("triple").split(" . ");
		   for(String triple:triples){			   
			   String [] po = triple.split(" ",2);
			   if(po.length == 2){
			   doc.add(new Field("object_original", po[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
			   doc.add(new Field("object_original", po[1], Field.Store.YES, Field.Index.NOT_ANALYZED));
			   }
			   doc.add(new Field("triple",triple, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   }
		   
		   return doc;
	}
	public static String getLabel(String str){
		String label_str = str.replace("<", "").replace(">", "").replaceAll(".*:","").replaceAll("/.*(/|#)", "");	
		label_str = toPhrase(label_str);
		return replaceChars(label_str);
	}
	public static String toPhrase(String str) {
		String ret = "";
		for (int i = 0; i != str.length(); ++i) {		
		    char c = str.charAt(i);
		    
		    if (c >= 'A' && c <= 'Z'){
		    	if(i < str.length()-1 && i > 0){
		    		char d = str.charAt(i+1);
		    		if (!(d >= 'A' && d <= 'Z'))
		    			ret+='_';
		    		else {
		    			d = str.charAt(i-1);
			    		if ((d >= 'a' && d <= 'z'))
			    			ret+='_';	
		    		}
		    	}	
		    	ret += (char)(c-'A'+'a');
		    }else{
		    	ret += c;
		    }
		    

		}
		return ret;
	}
	public static String replaceChars(String str){
		return str.replaceAll("_", " ").replaceAll("-", " ").replaceAll(",", "").replaceAll("\\.", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
	}
}
