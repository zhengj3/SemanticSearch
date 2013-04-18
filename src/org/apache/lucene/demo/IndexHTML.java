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
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
/** Indexer for HTML files. */
public class IndexHTML {
  private IndexHTML() {}

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
    private static void indexResourceDocs(HTMLDocument.Triple triple, IndexWriter thiswriter, File facetindex) throws Exception
    {

	//for(int i=0;i<triples.size();i++)
	//  {
		/*
		Document facet_doc=new Document();
		facet_doc.add(new Field("uid", doc.get("uid"), Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		facet_doc.add(new Field("contents",doc.get("contents"),Field.Store.YES,Field.Index.ANALYZED));
		
		for(int j=0;j<triples.size();j++){
		    
		    facet_doc.add(new Field("triples",triples.get(j).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		
		facet_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		
		facet_doc.add(new Field("fromdoc",doc.get("title"),Field.Store.YES, Field.Index.NO));
		
		thiswriter.addDocument(facet_doc);
		*/
		boolean differentDoc=true;
		boolean newDoc=true;
		Document facet_doc=new Document();

		//create facet doc
		//		facet_doc.add(new Field("uid", doc.get("uid"), Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		//facet_doc.add(new Field("contents",doc.get("contents"),Field.Store.YES,Field.Index.ANALYZED));

		//for(int j=0;j<triples.size();j++){
		    
		facet_doc.add(new Field("triples",triple.toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    //	}
		facet_doc.add(new Field("contents",triple.toString(),Field.Store.YES, Field.Index.ANALYZED));
		
		facet_doc.add(new Field("resource",triple.getSubject(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		//facet_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		//facet_doc.add(new Field("facetvalues",triples.get(i).getObject(),Field.Store.YES, Field.Index.ANALYZED));

		//facet_doc.add(new Field("fromdoc",doc.get("title"),Field.Store.YES, Field.Index.NO));

		
		IndexReader thisReader=thiswriter.getReader();
		//IndexReader thisReader = IndexReader.open(FSDirectory.open(facetindex), false);		  // open existing index
		TermDocs termdocs = thisReader.termDocs(new Term("resource",triple.getSubject()));

		while(termdocs!= null && termdocs.next()!=false)
		    {
			//check if this facet doc is from same HTML document(same property in the same document)
			//if so skip.
			//otherwise different document and is a new document.

			if(termdocs != null){
			    int docId = termdocs.doc();
			    if(differentDoc){
				
				Document temp_doc=thisReader.document(docId);	
			 				    
				String [] temp_triples=temp_doc.getValues("triples");
				String [] temp_contents=temp_doc.getValues("contents");

				//String [] temp_facetvalues=temp_doc.getValues("facetvalues");

				for(int j=0;j<temp_contents.length;j++){				    
					facet_doc.add(new Field("contents",temp_contents[j],Field.Store.YES,Field.Index.ANALYZED));
				}

				for(int j=0;j<temp_triples.length;j++){
				    facet_doc.add(new Field("triples",temp_triples[j],Field.Store.YES, Field.Index.NOT_ANALYZED));
				}
				/*  
				for(int j=0;j<temp_facetvalues.length;j++){
				    facet_doc.add(new Field("facetvalues",temp_facetvalues[j],Field.Store.YES, Field.Index.ANALYZED));
				}

				facet_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));
				
				for(int k=0;k<oldDocFromdoc.length;k++){
				    facet_doc.add(new Field("fromdoc",oldDocFromdoc[k],Field.Store.YES, Field.Index.NO));
				}
				*/
				//delete old facet doc
				thiswriter.deleteDocuments(new Term("resource",facet_doc.get("resource")));				    
			    }			   
			    
			}
		    }
		thisReader.close();
		

		if(newDoc)
		    {
			//index new facet doc
		    thiswriter.addDocument(facet_doc);
		    //thiswriter.commit();
		    }
		//  }
    }
    private static void indexFacetDocs(HTMLDocument.Triple triple, IndexWriter thiswriter, File facetindex) throws Exception
    {

	//for(int i=0;i<triples.size();i++)
	//  {
		/*
		Document facet_doc=new Document();
		facet_doc.add(new Field("uid", doc.get("uid"), Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		facet_doc.add(new Field("contents",doc.get("contents"),Field.Store.YES,Field.Index.ANALYZED));
		
		for(int j=0;j<triples.size();j++){
		    
		    facet_doc.add(new Field("triples",triples.get(j).toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		
		facet_doc.add(new Field("facetname",triples.get(i).getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		
		facet_doc.add(new Field("fromdoc",doc.get("title"),Field.Store.YES, Field.Index.NO));
		
		thiswriter.addDocument(facet_doc);
		*/
	
		boolean differentDoc=true;
		boolean newDoc=true;
		Document facet_doc=new Document();

		//create facet doc
		//		facet_doc.add(new Field("uid", doc.get("uid"), Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		//facet_doc.add(new Field("contents",doc.get("contents"),Field.Store.YES,Field.Index.ANALYZED));

		//for(int j=0;j<triples.size();j++){
		    
		facet_doc.add(new Field("triples",triple.toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    //	}
		facet_doc.add(new Field("contents",triple.toString(),Field.Store.YES, Field.Index.ANALYZED));

		facet_doc.add(new Field("subject",triple.getSubject(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		facet_doc.add(new Field("facetname",triple.getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		facet_doc.add(new Field("facetvalues",triple.getObject(),Field.Store.YES, Field.Index.ANALYZED));

		//facet_doc.add(new Field("fromdoc",doc.get("title"),Field.Store.YES, Field.Index.NO));

		
		IndexReader thisReader=thiswriter.getReader();
		//IndexReader thisReader = IndexReader.open(FSDirectory.open(facetindex), false);		  // open existing index
		TermDocs termdocs = thisReader.termDocs(new Term("facetname",triple.getPredicate()));

		while(termdocs!= null && termdocs.next()!=false)
		    {
			//check if this facet doc is from same HTML document(same property in the same document)
			//if so skip.
			//otherwise different document and is a new document.
			if(termdocs != null){
			    int docId = termdocs.doc();
			    if(differentDoc){
				
				Document temp_doc=thisReader.document(docId);	
			 				    
				String [] temp_triples=temp_doc.getValues("triples");
				String [] temp_contents=temp_doc.getValues("contents");

				String [] temp_facetvalues=temp_doc.getValues("facetvalues");
				
				for(int j=0;j<temp_contents.length;j++){				    
					facet_doc.add(new Field("contents",temp_contents[j],Field.Store.YES,Field.Index.ANALYZED));
				}
				
				for(int j=0;j<temp_triples.length;j++){
				    facet_doc.add(new Field("triples",temp_triples[j],Field.Store.YES, Field.Index.NOT_ANALYZED));
				}
				    
				for(int j=0;j<temp_facetvalues.length;j++){
				    facet_doc.add(new Field("facetvalues",temp_facetvalues[j],Field.Store.YES, Field.Index.ANALYZED));
				}

				facet_doc.add(new Field("facetname",triple.getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));
				/*
				for(int k=0;k<oldDocFromdoc.length;k++){
				    facet_doc.add(new Field("fromdoc",oldDocFromdoc[k],Field.Store.YES, Field.Index.NO));
				    }*/
				//delete old facet doc
				thiswriter.deleteDocuments(new Term("facetname",facet_doc.get("facetname")));				    
			    }			   
			    
			}
		    }
		thisReader.close();
		

		if(newDoc)
		    {
			//index new facet doc
		    thiswriter.addDocument(facet_doc);
		    //thiswriter.commit();
		    }
		//  }
    }
    private static void indexFacetValueDocs(HTMLDocument.Triple triple, IndexWriter thiswriter,File facetvalueindex) throws Exception
    {

	//	for(int i=0;i<triples.size();i++)
	//  {
		
		boolean differentDoc=true;
		boolean newDoc=true;
		Document facetvalue_doc=new Document();
		//facetvalue_doc.add(new Field("uid", doc.get("uid"), Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		facetvalue_doc.add(new Field("contents",triple.toString(),Field.Store.YES, Field.Index.ANALYZED));
		//for(int j=0;j<triples.size();j++){
		    
		facetvalue_doc.add(new Field("triples",triple.toString(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		    //}
		facetvalue_doc.add(new Field("subject",triple.getSubject(),Field.Store.YES, Field.Index.ANALYZED));
		
		facetvalue_doc.add(new Field("facetname",triple.getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));

		facetvalue_doc.add(new Field("facetvalue",triple.getObject(),Field.Store.YES, Field.Index.ANALYZED));
		
		//facetvalue_doc.add(new Field("fromdoc",doc.get("title"),Field.Store.YES, Field.Index.NO));
		
		facetvalue_doc.add(new Field("facetvalue_id",triple.getPredicate()+" "+triple.getObject(),Field.Store.YES, Field.Index.NOT_ANALYZED));


		IndexReader thisReader = thiswriter.getReader();//IndexReader.open(FSDirectory.open(facetvalueindex), false);		  // open existing index
		TermDocs termdocs = thisReader.termDocs(new Term("facetvalue_id",triple.getPredicate()+" "+triple.getObject()));
		
		while(termdocs!= null && termdocs.next()!=false)
		    {

			if(termdocs != null){
			    int docId = termdocs.doc();

			    if(differentDoc){
				
				Document temp_doc=thisReader.document(docId);				 				    
				String [] temp_triples = temp_doc.getValues("triples");
				
				String [] temp_contents=temp_doc.getValues("contents");

				for(int j=0;j<temp_contents.length;j++){				    
					facetvalue_doc.add(new Field("contents",temp_contents[j],Field.Store.YES,Field.Index.ANALYZED));
				}				
				
				for(int j=0;j<temp_triples.length;j++){
				    
				    facetvalue_doc.add(new Field("triples",temp_triples[j],Field.Store.YES, Field.Index.NOT_ANALYZED));
				}
				    
				facetvalue_doc.add(new Field("facetname",triple.getPredicate(),Field.Store.YES, Field.Index.NOT_ANALYZED));
				/*
				for(int k =0 ; k<oldDocFromdoc.length;k++){
				    facetvalue_doc.add(new Field("fromdoc",oldDocFromdoc[k],Field.Store.YES, Field.Index.NO));
				}
				*/
				facetvalue_doc.add(new Field("facetvalue",triple.getObject(),Field.Store.YES, Field.Index.NOT_ANALYZED));
				thiswriter.deleteDocuments(new Term("facetvalue_id",facetvalue_doc.get("facetvalue_id")));				    
			    }			   

			}
			
		    }
		thisReader.close();



		if(newDoc)
		    thiswriter.addDocument(facetvalue_doc);

		//  }
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
      for (int i = 0; i < files.length; i++)	  // recursively index them
	  indexDocs(new File(file, files[i]),index,facetindex,facetvalueindex);

    } else if (file.getPath().endsWith(".html") || // index .html files
      file.getPath().endsWith(".htm") || // index .htm files
	       file.getPath().endsWith(".txt") || file.getPath().endsWith(".nq")){ // index .txt files
      if (uidIter != null||facetuidIter!=null||facetvalueuidIter!=null) {

	  System.out.println("nq works");
        String uid = HTMLDocument.uid(file);	  // construct uid for doc

        while (uidIter!=null&&uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) < 0) {
          if (deleting) {			  // delete stale docs
            System.out.println("deleting " +
                HTMLDocument.uid2url(uidIter.term().text()));
            reader.deleteDocuments(uidIter.term());
          }
          uidIter.next();
        }
	
        while (facetuidIter!=null && facetuidIter.term() != null && facetuidIter.term().field() == "uid" &&
            facetuidIter.term().text().compareTo(uid) < 0) {
          if (deleting) {			  // delete stale docs
            System.out.println("deleting " +
                HTMLDocument.uid2url(facetuidIter.term().text()));
            facetreader.deleteDocuments(facetuidIter.term());
          }
          facetuidIter.next();
        }

        while (facetvalueuidIter!=null && facetvalueuidIter.term() != null && facetvalueuidIter.term().field() == "uid" &&
            facetvalueuidIter.term().text().compareTo(uid) < 0) {
          if (deleting) {			  // delete stale docs
            System.out.println("deleting " +
                HTMLDocument.uid2url(facetvalueuidIter.term().text()));
            facetvaluereader.deleteDocuments(facetvalueuidIter.term());
          }
          facetvalueuidIter.next();
        }
	
        if (uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) == 0) {
          uidIter.next();			  // keep matching docs
        } else if (!deleting) {			  // add new docs
          HTMLDocument.IndexDocuments indexDocs = HTMLDocument.Document(file);
	  Document doc = indexDocs.getFileDoc();
	  //ArrayList<Document> facetDocuments = indexDocs.getFacetDocs();
	  //ArrayList<Document> facetValueDocuments = indexDocs.getFacetValueDocs();
          System.out.println("adding1 " + doc.get("path"));

          writer.addDocument(doc);
	  /*
	  for(int i=0;i<facetDocuments.size();i++){
	      facetwriter.addDocument(facetDocuments.get(i));
	      System.out.println("adding facet "+facetDocuments.get(i).get("facetname"));
	  }

	  for(int i=0;i<facetValueDocuments.size();i++){
	      facetvaluewriter.addDocument(facetValueDocuments.get(i));
	      System.out.println("adding " + facetValueDocuments.get(i).get("facetvalue"));
	  }
	  */
        }
      } else {					  // creating a new index
	  //        Document doc = HTMLDocument.Document(file);

	  //instead of file, open the file, read each line and call index
	  //during the index, make sure to check if the resourse already in the index
	  String filePath = file.getAbsolutePath();
	  try{
	      FileInputStream fstream = new FileInputStream(filePath);
	      DataInputStream in = new DataInputStream(fstream);
	      BufferedReader br = new BufferedReader(new InputStreamReader(in));
	      String strLine;
	      int count = 0 ;
	      while ((strLine = br.readLine()) != null && count < 10000)   {
		  count ++;
		  //System.out.println("indexing: "+count);
		  String [] nquad = parse(strLine);
		  String s = nquad[0];
		  String p = nquad[1];
		  String o = nquad[2];
		  HTMLDocument.Triple triples = new HTMLDocument.Triple(s,p,o);
		  //indexResourceDocs(triples,writer,index);
		  //indexFacetDocs(triples,facetwriter,facetindex);
		  //indexFacetValueDocs(triples,facetvaluewriter,facetvalueindex);
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
}
