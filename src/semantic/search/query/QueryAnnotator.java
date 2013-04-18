package semantic.search.query;

import java.util.ArrayList;
import java.util.Hashtable;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryAnnotator {
	
	Model model;
	Hashtable<String, ArrayList<String>> conceptTable;
	Hashtable<String, ArrayList<String>> propertyTable;
	Hashtable<String, ArrayList<String>> resourceTable;
	
	public QueryAnnotator(Model m){	
		model = m;
		conceptTable = new Hashtable<String, ArrayList<String>>();
		propertyTable = new Hashtable<String, ArrayList<String>>();
		resourceTable = new Hashtable<String, ArrayList<String>>();
		buildHashIndexes();
	}

	public void buildHashIndexes(){
		buildConceptTable();
		buildPropertyTable();
		buildResourceTable();
	}
	public Hashtable<String, ArrayList<String>> getConceptTable(){
		return conceptTable;
	}
	public Hashtable<String, ArrayList<String>> getPropertyTable(){
		return propertyTable;
	}
	public Hashtable<String, ArrayList<String>> getResourceTable(){
		return resourceTable;
	}
	public void buildResourceTable(){
		try{
			String queryString = "SELECT DISTINCT ?type ?label WHERE {" +
					"{" +
					"?type <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
					"}" +
					"}";
			resourceTable = getQueryResult(model, queryString);			
			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	public void buildConceptTable(){
		try{
			String queryString = "SELECT DISTINCT ?type ?label WHERE {" +
					"{" +
					"?type <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>. " +
					"?type <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
					"}" +
					"}";
			conceptTable = getQueryResult(model, queryString);			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void buildPropertyTable(){
		try{
			String queryString = "SELECT DISTINCT ?type ?label WHERE {" +
					"{" +
					"?type <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>. " +
					"?type <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
					"}" +
					"UNION" +
					"{" +
					"?type <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>. " +
					"?type <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
					"}" +
					"UNION" +
					"{" +
					"?type <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>. " +
					"?type <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
					"}" +
					"}";
			propertyTable = getQueryResult(model, queryString);			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public Hashtable<String, ArrayList<String>> getQueryResult(Model model, String queryString)
	{
		Hashtable<String, ArrayList<String>> table = new Hashtable<String, ArrayList<String>>();
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		try {
			ResultSet queryResults = qe.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String typeURL = qs.get("?type").toString();
				String labelString = qs.get("?label").toString().replaceAll("@.*", "").toLowerCase();
				
				String [] labels = labelString.split(" ");
		//		String label = "";
				for(String label:labels){
		//			label += tlabel;
					if(table.keySet().contains(label)){
						ArrayList<String> typeList = table.get(label);
						if(!typeList.contains(typeURL))
							table.get(label).add(typeURL);			
					}else{
						ArrayList<String> typeList = new ArrayList<String> ();
						typeList.add(typeURL);
						table.put(label, typeList);
					}
					label += " ";
				}
			}
			
			qe.close();

		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return table;
	}
}
