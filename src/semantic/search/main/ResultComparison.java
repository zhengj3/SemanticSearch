package semantic.search.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import semantic.search.searcher.EntitySearcher;
import semantic.search.searcher.SemiStructuredSearcher;

public class ResultComparison {
	
	public static void main(String [] args){
		String referenceFile = "Dataset/qrels.track2.txt";
		String queryFile = "Dataset/final-types-2011.txt";
		//String indexFile = "Dataset/combined6";
		String indexDir = "Dataset/entity_base_noPOstring";
		String lexical_base = "Dataset/lexical_base";
		SemiStructuredSearcher searcher = new SemiStructuredSearcher(indexDir,lexical_base);
		HashMap<String, ArrayList<String>> relevantResult = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> myResult = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> queryIndex = new HashMap<String, String>();
		 try{
			  FileInputStream fstream = new FileInputStream(referenceFile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   {
			 	String [] cols = strLine.split("\t",4);
//			 	System.out.println(strLine+" "+cols.length);
			 	if(Integer.parseInt(cols[3]) > 0){
			 		//System.out.println(cols[2]);
			 		if(relevantResult.keySet().contains(cols[0])){
			 			ArrayList<String> currentResult = relevantResult.get(cols[0]);
			 			currentResult.add(cols[2]);
			 			relevantResult.put(cols[0], currentResult);
			 		}else{
			 			ArrayList<String> currentResult = new ArrayList<String>();
			 			currentResult.add(cols[2]);
			 			relevantResult.put(cols[0], currentResult);			 			
			 		}
			 	}
			  }
			  		  
			  in.close();
		}catch (Exception e){//Catch exception if any
			  e.printStackTrace();
		}
		 
		 try{
			  FileInputStream fstream2 = new FileInputStream(queryFile);
			  DataInputStream in2 = new DataInputStream(fstream2);
			  BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			  String strLine;
			  int counter = 1;
//			  EntitySearcher searcher = new EntitySearcher(indexFile);
			  while ((strLine = br2.readLine()) != null)   {
				  String key = "q"+counter;
				  System.out.println("query "+key+" "+strLine);
				  ArrayList<String> thisQueryResult = searcher.search(strLine);
				  myResult.put(key, thisQueryResult);
				  queryIndex.put(key, strLine);
				  counter++;
			  }
			  		  
			  in2.close();
		}catch (Exception e){//Catch exception if any
			  e.printStackTrace();
		}
		 
		 int totalCount = 0;
		 int overallTotal = 0;
		 double MAP = 0;
		 for(String query:relevantResult.keySet()){
			 double matchCount = 0;
			 double thisQueryScore = 0;
			 ArrayList<String> myQueryResult = myResult.get(query);
			 ArrayList<String> refQueryResult = relevantResult.get(query);
			 if(myQueryResult == null || refQueryResult == null)
				 continue;
			 
			 int maxCount = relevantResult.size();
			 if(maxCount > 10)
				 maxCount = 10;
			 
			 overallTotal += maxCount;
			 
			 for(String result:myQueryResult){
				 //overallTotal++;
				 //result = result.replaceAll("<", "").replaceAll(">", "");
				 result = result.replaceAll(">", "").replaceAll("<", "");
				// System.out.print("my result: "+result);
				 if(refQueryResult.contains(result)){
					 matchCount++;
					 totalCount++;
					 //System.err.println(" in");
				 }else{
					 //System.out.println(" not in");
				 }
			 }
			 if(matchCount > 10)
				 matchCount = 10;
			 thisQueryScore = matchCount/maxCount;
			 MAP += thisQueryScore;
//			 System.out.println(query+" "+queryIndex.get(query)+" "+matchCount);
		 }
		 for(String query:relevantResult.keySet()){
			 int matchCount = 0;
			 
			 ArrayList<String> myQueryResult = myResult.get(query);
			 ArrayList<String> refQueryResult = relevantResult.get(query);
			 if(myQueryResult == null || refQueryResult == null)
				 continue;

			 
			 for(String result:refQueryResult){
				 //overallTotal++;
				 //result = result.replaceAll("<", "").replaceAll(">", "");
				 result = "<"+result+">";
				// System.out.println("ref result: "+result);
				 if(myQueryResult.contains(result)){
					 matchCount++;
					 System.out.println("ref result: "+result+" in");
				 }else{
					 System.out.println("ref result: "+result+" not in");
				 }
			 }
			 System.out.println(query+" "+queryIndex.get(query)+" "+matchCount+" out of "+refQueryResult.size());
		 }		 
		 System.out.println(totalCount+" out of "+overallTotal);
		 System.out.println("MAP: "+ (MAP/relevantResult.keySet().size()));
	}
}
