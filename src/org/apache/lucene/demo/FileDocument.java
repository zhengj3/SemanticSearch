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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.demo.html.HTMLParser;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;

/** A utility for making Lucene Documents from a File. */

public class FileDocument {
  /** Makes a document for a File.
    <p>
    The document has three fields:
    <ul>
    <li><code>path</code>--containing the pathname of the file, as a stored,
    untokenized field;
    <li><code>modified</code>--containing the last modified date of the file as
    a field as created by <a
    href="lucene.document.DateTools.html">DateTools</a>; and
    <li><code>contents</code>--containing the full contents of the file, as a
    Reader field;
    */
	public static Document indexDoc(String content,String triples, String url, String subjects,String objects, String domains, String ranges)throws java.io.FileNotFoundException {

		   Document doc = new Document();
		   String subject = "";
		   

		     
		   //doc.add(new Field("triples", contents.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		   doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("property", IndexFiles.reviseString(url), Field.Store.YES, Field.Index.ANALYZED));
		   doc.add(new Field("subjects", subjects, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("objects", objects, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("domains", domains, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("ranges", ranges, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("triples", triples, Field.Store.YES, Field.Index.NOT_ANALYZED));
		   doc.add(new Field("contents", content, Field.Store.YES, Field.Index.ANALYZED,TermVector.WITH_POSITIONS_OFFSETS));
		   
		//   System.out.println("subject: "+subject);
		//   System.out.println("contents: "+content);

		   // return the document
		   
		   return doc;	
	}
  public static Document indexDoc(String content, String triples)throws java.io.FileNotFoundException {
    		 

   Document doc = new Document();
   String subject = "";
   
   try {
       BufferedReader br = new BufferedReader(new StringReader(content));
       String line = br.readLine();     
       subject = line;

     } catch (Exception e) {
       System.err.println(e);
     }
     
   //doc.add(new Field("triples", contents.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
   doc.add(new Field("url", subject, Field.Store.YES, Field.Index.NOT_ANALYZED));
   doc.add(new Field("subject", IndexFiles.reviseString(subject), Field.Store.YES, Field.Index.ANALYZED));
   doc.add(new Field("triples", triples, Field.Store.YES, Field.Index.NOT_ANALYZED));
   doc.add(new Field("contents", content, Field.Store.YES, Field.Index.ANALYZED,TermVector.WITH_POSITIONS_OFFSETS));
   
//   System.out.println("subject: "+subject);
//   System.out.println("contents: "+content);

   // return the document
   
   return doc;	  
  }
  public static Document Document(File f)
       throws java.io.FileNotFoundException {
	 

    Document doc = new Document();


    doc.add(new Field("url", f.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));


    StringBuilder contents = new StringBuilder();
    
    
    try {
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line= br.readLine())!= null){
        	contents.append(line);
        	contents.append("\n");
        }
    	/*
        FileInputStream fstream = new FileInputStream(f);
        String content = "";
        FileInputStream fis = new FileInputStream(f);
        HTMLParser parser = new HTMLParser(fis);
        contents=readFromReader(parser.getReader());
        */
      } catch (Exception e) {
        System.err.println(e);
      }
      
    //doc.add(new Field("triples", contents.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    
    doc.add(new Field("contents", contents.toString(), Field.Store.YES, Field.Index.ANALYZED,TermVector.WITH_POSITIONS_OFFSETS));
    

    // return the document
    return doc;
  }
  public static String readFromReader(Reader reader) throws IOException
  {
	int data=reader.read();
	StringBuilder content= new StringBuilder();
	while(data != -1)
	    {
		char theChar=(char)data;
		content.append(theChar);
		data=reader.read();
	    }
	return content.toString();
  }
  private FileDocument() {}
}
    
