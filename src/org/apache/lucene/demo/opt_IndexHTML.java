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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.FilterIndexReader.FilterTermDocs;



import java.io.File;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
/** Indexer for HTML files. */
public class opt_IndexHTML {
  private opt_IndexHTML() {}

  private static boolean deleting = false;	  // true during deletion pass
  private static IndexReader reader;		  // existing index
  private static IndexReader facetreader;		  // existing index
  private static IndexReader facetvaluereader;		  // existing index
  private static IndexWriter writer;		  // new index being built
  private static IndexWriter facetwriter;		  // new index being built
  private static IndexWriter facetvaluewriter;		  // new index being built
  private static TermEnum uidIter;		  // document id iterator
  private static TermEnum facetuidIter;		  // document id iterator
  private static TermEnum facetvalueuidIter;		  // document id iterator

  /** Indexer for HTML files.*/
  public static void main(String[] argv) {
    try {
      File index = new File("index");
      File facetindex=new File("facetindex");
      File facetvalueindex=new File("facetvalueindex");
      boolean create = false;
      File root = null;

      String usage = "IndexHTML 2 [-create] [-index <indexdir>] <root_directory>";

      if (argv.length == 0) {
        System.err.println("Usage: " + usage);
        return;
      }

      for (int i = 0; i < argv.length; i++) {
        if (argv[i].equals("-index")) {		  // parse -index option
          index = new File(argv[++i]+"/index");
          facetindex = new File(argv[i]+"/facetindex");
          facetvalueindex = new File(argv[i]+"/facetvalueindex");
        } else if (argv[i].equals("-create")) {	  // parse -create option
          create = true;
        } else if (i != argv.length-1) {
          System.err.println("Usage: " + usage);
          return;
        } else
          root = new File(argv[i]);
      }
      
      if(root == null) {
        System.err.println("Specify directory to index");
        System.err.println("Usage: " + usage);
        return;
      }

      Date start = new Date();

      if (!create) {				  // delete stale docs
        deleting = true;

        indexDocs(root, index,facetindex,facetvalueindex, create);
      }
      writer = new IndexWriter(FSDirectory.open(index), new StandardAnalyzer(Version.LUCENE_CURRENT), create, 
                               new IndexWriter.MaxFieldLength(1000000));
      facetwriter = new IndexWriter(FSDirectory.open(facetindex), new StandardAnalyzer(Version.LUCENE_CURRENT), create, 
                               new IndexWriter.MaxFieldLength(1000000));
      facetvaluewriter = new IndexWriter(FSDirectory.open(facetvalueindex), new StandardAnalyzer(Version.LUCENE_CURRENT), create, 
                               new IndexWriter.MaxFieldLength(1000000));

      reader = IndexReader.open(FSDirectory.open(index), false);		  // open existing index     
      facetreader = IndexReader.open(FSDirectory.open(facetindex), false);		  // open existing index
      facetvaluereader = IndexReader.open(FSDirectory.open(facetvalueindex), false);		  // open existing index


      indexDocs(root, index, facetindex, facetvalueindex, create);		  // add new docs
      reader.close();
      facetreader.close();
      facetvaluereader.close();
      System.out.println("Optimizing index...");
      writer.optimize();
      writer.close();
      facetwriter.optimize();
      facetwriter.close();
      facetvaluewriter.optimize();
      facetvaluewriter.close();

      Date end = new Date();

      System.out.print(end.getTime() - start.getTime());
      System.out.println(" total milliseconds");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* Walk directory hierarchy in uid order, while keeping uid iterator from
  /* existing index in sync.  Mismatches indicate one of: (a) old documents to
  /* be deleted; (b) unchanged documents, to be left alone; or (c) new
  /* documents, to be indexed.
   */

    private static void indexDocs(File file, File index,File facetindex, File facetvalueindex, boolean create)
       throws Exception {
    if (!create) {				  // incrementally update


      uidIter = reader.terms(new Term("uid", "")); // init uid iterator
      facetuidIter = facetreader.terms(new Term("uid", "")); // init uid iterator
      facetvalueuidIter = facetvaluereader.terms(new Term("uid", "")); // init uid iterator

      indexDocs(file,index,facetindex,facetvalueindex);

      if (deleting) {				  // delete rest of stale docs
        while (uidIter.term() != null && uidIter.term().field() == "uid") {
          System.out.println("deleting " +
              HTMLDocument.uid2url(uidIter.term().text()));
          reader.deleteDocuments(uidIter.term());
          uidIter.next();
        }

        while (facetuidIter.term() != null && facetuidIter.term().field() == "uid") {
          System.out.println("deleting " +
              HTMLDocument.uid2url(facetuidIter.term().text()));
          facetreader.deleteDocuments(facetuidIter.term());
          facetuidIter.next();
        }

        while (facetvalueuidIter.term() != null && facetvalueuidIter.term().field() == "uid") {
          System.out.println("deleting " +
              HTMLDocument.uid2url(facetvalueuidIter.term().text()));
          facetvaluereader.deleteDocuments(facetvalueuidIter.term());
          facetvalueuidIter.next();
        }
        deleting = false;
      }

      uidIter.close();				  // close uid iterator
      facetuidIter.close();				  // close uid iterator
      facetvalueuidIter.close();				  // close uid iterator
      
    } else					  // don't have exisiting
	indexDocs(file,index,facetindex,facetvalueindex);
  
    }
    private static void indexResourceDocs(IndexWriter thiswriter, ArrayList<HTMLDocument.Triple> triples, String content)throws Exception
    {
    	if(triples.size()==0)
    		return;
    	
    	String subject = triples.get(0).getSubject();  	
    	Document resource_doc=new Document();
    	System.out.println("resource "+content);
    	resource_doc.add(new Field("contents",content,Field.Store.YES, Field.Index.ANALYZED));
    	resource_doc.add(new Field("resource",subject,Field.Store.YES, Field.Index.NOT_ANALYZED));    	
    	for(int i = 0 ; i < triples.size(); i ++){
    		System.out.println("triples "+triples.get(i).toString());
    		resource_doc.add(new Field("triples",triples.get(i).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
    		
    	}
    	thiswriter.addDocument(resource_doc);
    }
    private static void indexFacetDocs(IndexWriter thiswriter,  ArrayList<HTMLDocument.Triple> triples, String facetname, String content) throws Exception
    {
	    Iterator<HTMLDocument.Triple> itr = triples.iterator();
	    Document facet_doc=new Document();
	    System.out.println("facet "+content);
    	facet_doc.add(new Field("contents",content,Field.Store.YES, Field.Index.ANALYZED));	
    	facet_doc.add(new Field("facetname",facetname,Field.Store.YES, Field.Index.NOT_ANALYZED));
	    while(itr.hasNext()){
	    	HTMLDocument.Triple triple = itr.next();
	    	System.out.println("triples "+triple.toString());
	    	facet_doc.add(new Field("triples",triple.toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
	    	if(triple.getPredicate().equals(facetname))
	    		facet_doc.add(new Field("facetvalues",triple.getObject(),Field.Store.YES, Field.Index.ANALYZED));	   
	    }
	    thiswriter.addDocument(facet_doc);
    }
    private static void indexFacetValueDocs(IndexWriter thiswriter, ArrayList<HTMLDocument.Triple> triples, String content) throws Exception
    {
    	for(int i = 0 ; i < triples.size(); i ++){
    		 Document facetvalue_doc=new Document();
    		 System.out.println("facetvalue "+content);
    		 facetvalue_doc.add(new Field("contents",content,Field.Store.YES, Field.Index.ANALYZED));
    		 facetvalue_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));
    		 facetvalue_doc.add(new Field("facetvalue_id",triples.get(i).getPredicate()+" "+triples.get(i).getObject(),Field.Store.YES, Field.Index.NOT_ANALYZED));
    		 for(int j = 0 ; j < triples.size(); j++){
    			 System.out.println("triples "+triples.get(i).toString());
    			 facetvalue_doc.add(new Field("triples",triples.get(i).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
    		 }
    	}
    }

    private static String [] parse(String nquad){

	String [] sub_and_rest = nquad.split(" ", 2);

	String subject = sub_and_rest[0];

	String rest = sub_and_rest[1];

	String [] pro_and_rest = rest.split(" ", 2);

	String property = pro_and_rest[0];

	rest = pro_and_rest[1].substring(0,pro_and_rest[1].length()-2).trim();

	int lastspace = rest.lastIndexOf(' ');

	String object = rest.substring(0, lastspace);

	String [] nquadTriple = {subject, property, object};

	return nquadTriple;
    }
    private static void indexDocs(File file,File index,File facetindex,File facetvalueindex) throws Exception {
    if (file.isDirectory()) {			  // if a directory
      String[] files = file.list();		  // list its files
      Arrays.sort(files);			  // sort the files
      for (int i = 0; i < files.length && i < 100; i++)	  // recursively index them
	  indexDocs(new File(file, files[i]),index,facetindex,facetvalueindex);
      } else {					  // creating a new index
	  //        Document doc = HTMLDocument.Document(file);

	  //instead of file, open the file, read each line and call index
	  //during the index, make sure to check if the resourse already in the index
	  String filePath = file.getAbsolutePath();
	  try{
		  boolean isfacetfile = true;
		  String facetname = "";
	      FileInputStream fstream = new FileInputStream(filePath);
	      DataInputStream in = new DataInputStream(fstream);
	      BufferedReader br = new BufferedReader(new InputStreamReader(in));
	      String strLine;
	      int count = 0 ;
	      ArrayList<HTMLDocument.Triple> triples = new ArrayList<HTMLDocument.Triple>();
	      StringBuilder sb = new StringBuilder();
	      if((strLine = br.readLine())!=null){
	    	  strLine = strLine.trim();
	    	  if(strLine.contains(" ")){
	    		  isfacetfile = false;
	    	  }
	    	  else{
	    		  facetname = strLine;
	    	  }
	      }
		  if(isfacetfile){
			  System.out.println("indexing as facet "+facetname);
		      while ((strLine = br.readLine()) != null)   {
				 
				  String [] nquad = parse(strLine);
				  String s = nquad[0];
				  String p = nquad[1];
				  String o = nquad[2];
				  sb.append(s);
				  sb.append(" ");
				  sb.append(p);
				  sb.append(" ");
				  sb.append(o);
				  sb.append(" ");
				  HTMLDocument.Triple triple = new HTMLDocument.Triple(s,p,o);
				  if(triples.contains(triple)!=true)
					  triples.add(triple);
		      }
			  indexFacetDocs(facetwriter,triples,facetname,sb.toString());
		  }
		  else{
			  System.out.println("indexing as resource "+ strLine);
		      do{
				  
				  String [] nquad = parse(strLine);
				  String s = nquad[0];
				  String p = nquad[1];
				  String o = nquad[2];
				  sb.append(s);
				  sb.append(" ");
				  sb.append(p);
				  sb.append(" ");
				  sb.append(o);
				  sb.append(" ");
				  HTMLDocument.Triple triple = new HTMLDocument.Triple(s,p,o);
				  triples.add(triple);
		      } while ((strLine = br.readLine()) != null);
		      
			  indexResourceDocs(writer,triples,sb.toString());
			  indexFacetValueDocs(facetvaluewriter,triples,sb.toString());		  
		  }
		  
	      in.close();
	  }catch (Exception e){//Catch exception if any
	      System.err.println("Error: " + e.getMessage());
	  }


          //HTMLDocument.IndexDocuments indexDocs = HTMLDocument.Document(file);
	  //Document doc = indexDocs.getFileDoc();
	  //ArrayList<HTMLDocument.Triple>  triples = indexDocs.getTriples();
          //System.out.println("adding2 " + doc.get("path"));	  
	  //writer.addDocument(doc);
	  //indexFacetDocs(triples,doc,facetwriter,facetindex);
	  //indexFacetValueDocs(triples,doc,facetvaluewriter,facetvalueindex);
	  
	  //System.out.println("facets: "+facetDocuments.size());
	  //facetwriter.addDocument(doc);
	  /*	  
	  for(int i=0;i<facetDocuments.size();i++){ 
	      System.out.println(facetDocuments.get(i).get("contents"));
	  }
	  /*
	  for(int i=0;i<facetDocuments.size();i++){ 
	      Document facetdoc=facetDocuments.get(i);

	      facetwriter.addDocument(facetdoc);
	      break;
	      //System.out.println(facetDocuments.get(i).get("facetname"));
	  }
	  /*

	  for(int i=0;i<facetValueDocuments.size();i++){
	      facetvaluewriter.addDocument(facetValueDocuments.get(i));
	      }*/
	  //System.out.println("adding " + doc.get("path"));
	  //	  writer.addDocument(doc);		  // add docs unconditionally
   



    }
  }
}
