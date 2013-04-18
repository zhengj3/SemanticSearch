package semantic.search.main;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class parseRelevantResult {
	
	public static void main(String [] args){
		 try{
			  FileInputStream fstream = new FileInputStream("Dataset/qrels.track1.txt");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   {
			 	String [] cols = strLine.split("\t",4);
//			 	System.out.println(strLine+" "+cols.length);
			 	if(Integer.parseInt(cols[3]) > 0){
			 		System.out.println(cols[0]+"\t"+cols[2]);
			 	}
			  }
			  in.close();
			    }catch (Exception e){//Catch exception if any
			  e.printStackTrace();
			  }
	}

}
