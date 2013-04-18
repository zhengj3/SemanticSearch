package org.apache.lucene.demo;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import org.apache.lucene.document.*;
import org.apache.lucene.demo.html.HTMLParser;
import java.util.ArrayList;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.*;
/** A utility for making Lucene Documents for HTML documents. */

public class HTMLDocument {
    static char dirSep = System.getProperty("file.separator").charAt(0);
    
    

  public static String uid(File f) {
    // Append path and date into a string in such a way that lexicographic
    // sorting gives the same results as a walk of the file hierarchy.  Thus
    // null (\u0000) is used both to separate directory components and to
    // separate the path from the date.
    return f.getPath().replace(dirSep, '\u0000') +
      "\u0000" +
      DateTools.timeToString(f.lastModified(), DateTools.Resolution.SECOND);
  }

  public static String uid2url(String uid) {
    String url = uid.replace('\u0000', '/');	  // replace nulls with slashes
    return url.substring(0, url.lastIndexOf('/')); // remove date from end
  }
    /*
    public static ArrayList<Document> facetDoc(File f) throws IOException, InterruptedException{

    }
    */
  public static IndexDocuments Document(File f)
       throws IOException, InterruptedException  {
    // make a new, empty document
    Document doc = new Document();
    ArrayList<Triple> facet_docs=new ArrayList<Triple>();


    // Add the url as a field named "path".  Use a field that is 
    // indexed (i.e. searchable), but don't tokenize the field into words.
    doc.add(new Field("path", f.getPath().replace(dirSep, '/'), Field.Store.YES,
        Field.Index.NOT_ANALYZED));

    // Add the last modified date of the file a field named "modified".  
    // Use a field that is indexed (i.e. searchable), but don't tokenize
    // the field into words.
    doc.add(new Field("modified",
        DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
        Field.Store.YES, Field.Index.NOT_ANALYZED));

    // Add the uid as a field, so that index can be incrementally maintained.
    // This field is not stored with document, it is indexed, but it is not
    // tokenized prior to indexing.
    doc.add(new Field("uid", uid(f), Field.Store.NO, Field.Index.NOT_ANALYZED));

    FileInputStream fis = new FileInputStream(f);
    HTMLParser parser = new HTMLParser(fis);
    String contents=readFromReader(parser.getReader());
    fis.close();
    // Add the tag-stripped contents as a Reader-valued Text field so it will
    // get tokenized and indexed.
    doc.add(new Field("contents",contents,Field.Store.NO,Field.Index.ANALYZED));
    // Add the summary as a field that is stored and returned with
    // hit documents for display.
    doc.add(new Field("summary", parser.getSummary(), Field.Store.YES, Field.Index.NO));

    // Add the title as a field that it can be searched and that is stored.
    doc.add(new Field("title", parser.getTitle(), Field.Store.YES, Field.Index.ANALYZED));


    parse_rdfa rdfa_parser=new parse_rdfa();
    rdfa_parser.insertFilter("<http://www.w3.org/1999/xhtml/vocab#stylesheet>");
    rdfa_parser.parseFile(f.getPath().replace(dirSep, '/'));
    ArrayList<Triple> triples=new ArrayList<Triple>();
    ArrayList<String> propertyValues=new ArrayList<String>();
    triples=rdfa_parser.getTriples();
    propertyValues=rdfa_parser.getPropertyValues();

    /*
    FileInputStream fis2 = new FileInputStream(f);
    HTMLParser parser2 = new HTMLParser(fis2);

    fis2.close();
    */
    //    IndexReader facetindexreader = IndexReader.open(FSDirectory.open(new File("/opt/lucene/facetindex")),false);

    //    IndexReader facetvalueindexreader = IndexReader.open(FSDirectory.open(new File("/opt/lucene/facetvalueindex")),false);

    for(int i=0;i<triples.size();i++)
	{
	    doc.add(new Field("triples", triples.get(i).toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
	    facet_docs.add(triples.get(i));
	    //try to index facets w.r.t keyword, triples, causing memory problem
	    /*
	    Document facet_doc=new Document();
	  
	    facet_doc.add(new Field("uid", uid(f), Field.Store.NO, Field.Index.NOT_ANALYZED));

	    facet_doc.add(new Field("contents",contents,Field.Store.NO,Field.Index.ANALYZED));

	    for(int j=0;j<triples.size();j++){
		facet_doc.add(new Field("triples",triples.get(j).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
	    }

	    facet_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

	    facet_doc.add(new Field("fromdoc",parser.getTitle(),Field.Store.YES, Field.Index.NO));

	    //facet_doc.add(new Field("contents","anything",Field.Store.NO,Field.Index.NOT_ANALYZED));

	    facet_docs.add(facet_doc);
	    */
	    //fis2.close();

	    /*
	    FileInputStream fis3 = new FileInputStream(f);
	    HTMLParser parser3 = new HTMLParser(fis3);


	    //try to index facet value w.r.t keyword, triples, facet
	    Document facet_value_doc=new Document();
	    facet_value_doc.add(new Field("uid", uid(f), Field.Store.NO, Field.Index.NOT_ANALYZED));
	    facet_value_doc.add(new Field("contents",parser3.getReader()));
	    for(int j=0;j<triples.size();j++){
		facet_value_doc.add(new Field("triples",triples.get(i).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
	    }
	    facet_value_doc.add(new Field("facet",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));
	    facet_value_doc.add(new Field("facetvalue",triples.get(i).getObject(),Field.Store.YES, Field.Index.NO));
	    facet_doc.add(new Field("fromdoc",parser.getTitle(),Field.Store.YES, Field.Index.NO));
	    facet_value_docs.add(facet_value_doc);
	    fis3.close();
	    */
	}


    



    /*
    for(int i=0;i<propertyValues.size();i++)
	{
	    doc.add(new Field("property_value", i+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
	}
    */

    IndexDocuments docs=new IndexDocuments(doc, facet_docs);
    return docs;

    // return the document
    //return doc;
  }
    public static String readFromReader(Reader reader) throws IOException
    {
	int data=reader.read();
	String content="";
	while(data != -1)
	    {
		char theChar=(char)data;
		content+=theChar;
		data=reader.read();
	    }
	return content;
    }

  private HTMLDocument() {}


    public static class parse_rdfa{


	private ArrayList<Triple> Triples;
	private ArrayList<String> PropertyValues;
	private ArrayList<String> PropertyFilters;
	private ArrayList<String> Properties;

	public parse_rdfa()
	{
	    Triples=new ArrayList<Triple>();
	    PropertyValues=new ArrayList<String>();
	    PropertyFilters=new ArrayList<String>();
	}
	public void insertFilter(String filter){
	    PropertyFilters.add(filter);
	}
	public ArrayList<Triple> getTriples()
	{
	    return Triples;
	}
	public void parseFile(String file)
	{
	    try {
		Runtime rt = Runtime.getRuntime();
		//Process pr = rt.exec("cmd /c dir");
		Process pr = rt.exec("php /var/www/research/search/rdfaParser/myScript/arc2_rdfa_parser.php "+file);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line=null;

		while((line=input.readLine()) != null) {
		    String delims = "[ ]+";
		    String[] triple = line.split(delims,3);
		    //String[] triple = line.split(delims,4);
		    boolean filter=false;
		    String property;
		    if(triple.length==3)
			{
			    property=triple[1];
			    for(int i=0;i<PropertyFilters.size();i++)
				{
				    if(property.compareTo(PropertyFilters.get(i))==0)
					{
					    filter=true;
					    break;
					}
				}

			    if(!filter){
				insertTriple(triple[0],triple[1],triple[2]);
				insertPropertyValue(triple[1],triple[2]);
			    }

			}
		}

	    } catch(Exception e) {
		System.out.println(e.toString());
		e.printStackTrace();
	    }

	}
	public ArrayList<String> getPropertyValues()
	{
	    return PropertyValues;
	}
	private void insertPropertyValue(String pre,String obj)
	{
	    String PropertyValue=pre+" "+obj;
	    PropertyValues.add(PropertyValue);
	}
	private void insertTriple(String sub,String pre,String obj)
	{
	    Triples.add(new Triple(sub,pre,obj));
	}
    }

    public static class Triple{
	private String subject;
	private String predicate;
	private String object;

	public Triple(String sub, String pre, String obj){
	    subject=sub;
	    predicate=pre;
	    object=obj;
	}
	public String toString()
	{
	    return subject+" "+predicate+" "+object;
	}
	public String getSubject()
	{
	    return subject;
	}
	public String getPredicate()
	{
	    return predicate;
	}
	public String getObject()
	{
	    return object;
	}
    }
    public static class IndexDocuments{
	private Document fileDocument;
	private ArrayList<Triple> facetDocuments;

	public IndexDocuments(Document doc, ArrayList<Triple> facetDocs){
	    fileDocument=doc;
	    facetDocuments=facetDocs;
	    //facetValueDocuments=facetValueDocs;
	}
	public Document getFileDoc()
	{
	    return fileDocument;
	}
	public ArrayList<Triple> getTriples()
	{
	    return facetDocuments;
	}
	/*
	public ArrayList<Document> getFacetValueDocs()
	{
	    return facetValueDocuments;
	}
	*/
    }
}