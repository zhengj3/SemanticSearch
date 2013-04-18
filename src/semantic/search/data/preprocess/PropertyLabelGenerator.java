package semantic.search.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class PropertyLabelGenerator {

	public static void main(String [] args){
//		System.out.println(toPhrase("topicIDTestThing"));
		try{
			  FileInputStream fstream = new FileInputStream("Dataset/objectList_sorted_unique.txt");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter("Dataset/property_labels.txt");
			  BufferedWriter out = new BufferedWriter(ofstream);

			  while ((strLine = br.readLine()) != null)   {
				  String label = getLabel(strLine);
				  if(label.length()>0){
					  out.write(strLine+" <http://www.w3.org/2000/01/rdf-schema#label> \""+label+"\" .\n");
				  }
				  
			  }
			  in.close();
			    }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
			  }
		
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
		return str.replaceAll("_", " ").replaceAll("-", " ").replaceAll("has", "").trim();
	}
}
