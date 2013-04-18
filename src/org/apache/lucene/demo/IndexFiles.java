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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/** Index all text files under a directory. */
public class IndexFiles {
  
  private IndexFiles() {}

  static final File INDEX_DIR = new File("property_index");
  
  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.demo.IndexFiles <root_directory>";
    if (args.length == 0) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    if (INDEX_DIR.exists()) {
      System.out.println("Cannot save index to '" +INDEX_DIR+ "' directory, please delete it first");
      System.exit(1);
    }
    
    final File docDir = new File(args[0]);
    if (!docDir.exists() || !docDir.canRead()) {
      System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
      System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
      indexDocs(writer, docDir);
      System.out.println("Optimizing...");
      writer.optimize();
      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }
  static void indexDocs(IndexWriter writer, String content, String triples,String url, String subjects,String objects, String domains, String ranges)
		    throws IOException {

		        try {
		          writer.addDocument(FileDocument.indexDoc(content,triples,url,subjects,objects,domains,ranges));
		        }
		        // at least on windows, some temporary files raise this exception with an "access denied" message
		        // checking if the file can be read doesn't help
		        catch (FileNotFoundException fnfe) {
		          ;
		        }
		  }
  static String reviseString(String str){
	  int startIndex;
	  int endIndex = str.length();
	  
	  startIndex = str.lastIndexOf("#");
	  if(startIndex < 0){
		  startIndex = str.lastIndexOf("/");
	  }
	  if(startIndex < 0){
		  startIndex = 0;
	  }	  
//	  System.out.println(startIndex+" "+endIndex);
	  String newStr = str.substring(startIndex+1, endIndex).replaceAll("_", " ").replaceAll("<", "").replaceAll(">", "");
	  String finalStr = "";
	  for(char c:newStr.toCharArray()){
		  if(Character.isUpperCase(c)){
			  finalStr += " ";
			  finalStr += Character.toLowerCase(c);
		  }else{
			  finalStr += c;
		  }
	  }
	  return finalStr;
	  //return str.substring(startIndex+1, endIndex).replaceAll("_", " ").replaceAll("<", "").replaceAll(">", "");
  }
  static void indexDocs(IndexWriter writer, File file){

		  }
//  static void indexDocs(IndexWriter writer, File file)
//    throws IOException {
//    // do not try to index files that cannot be read
//    if (file.canRead()) {
//      if (file.isDirectory()) {
//        String[] files = file.list();
//        // an IO error could occur
//        if (files != null) {
//          for (int i = 0; i < files.length; i++) {
//            indexDocs(writer, new File(file, files[i]));
//          }
//        }
//      } else {
//        //System.out.println("adding " + file);
//        try {
//        	//read from file and call indexDocs
//            FileInputStream fstream = new FileInputStream(file);
//            DataInputStream in = new DataInputStream(fstream);
//            BufferedReader br = new BufferedReader(new InputStreamReader(in));
//            String line;
//            StringBuilder contents = new StringBuilder();
//            String preSubject = "";
//            StringBuilder triples = new StringBuilder();
//            if((line= br.readLine())!= null){
//            	String [] triple = line.split(" ",3);
//				if(triple.length >= 3){
//					preSubject = triple[0];
//					contents.append(line);
//					contents.append("\n");
//					triples.append(line);
//					triples.append("\n");
//				}
//            }
//            int processed_count = 1;
//            while ((line= br.readLine())!= null ){
//            	
//            	String [] triple = line.split(" ",3);
//				if(triple.length < 3)
//					continue;
//				  
//				String subject = triple[0];
//				  
//				if(!subject.equals(preSubject)){ 
////					System.out.println("index file: "+preSubject);
//					indexDocs(writer,contents.toString(),triples.toString());
//					contents = new StringBuilder();
//					preSubject = subject;
//					contents.append(triple[0]);
//					contents.append("\n");
//					String revised_subject = reviseString(triple[0]);
//					contents.append(revised_subject);
//					contents.append(" ");
//					triples = new StringBuilder();
//				}
//            	
//				triples.append(line);
//				triples.append("\n");
//				String revised_property = reviseString(triple[1]);
//				contents.append(revised_property);
//				contents.append(" ");
//				if(!triple[2].startsWith("\"")){
//					String revised_object = reviseString(triple[2]);
//					contents.append(revised_object);
//				}else{
//					  int startIndex;
//					  int endIndex;
//					  
//					  startIndex = triple[2].indexOf("\"");
//					  if(startIndex < 0){
//						  startIndex = 0;
//					  }	
//					  
//					  endIndex = triple[2].lastIndexOf("\"");
//					  if(endIndex < 0){
//						  endIndex = triple[2].length();
//					  }
//					String revised_object = triple[2].substring(startIndex+1,endIndex);
//					contents.append(revised_object);
//				}
//				contents.append(" ");   
//				processed_count ++;
//				
//				if(processed_count % 5000 ==0){				
//					System.out.println("processed: "+processed_count);
//				}
//            }
//          //writer.addDocument(FileDocument.Document(file));
//        }
//        // at least on windows, some temporary files raise this exception with an "access denied" message
//        // checking if the file can be read doesn't help
//        catch (FileNotFoundException fnfe) {
//          ;
//        }
//      }
//    }
//  }
  
}
