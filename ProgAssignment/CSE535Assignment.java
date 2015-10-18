/*

Author: Prasad Shivanna
Date: Oct 17 2015

Problem Statement: The java class will take a postings list, build an index out of it and perform few Boolean query processing

It takes the following input
1) term.idx  -- postings list
2) output.log -- the name of the file where the output is supposed to be stored
3) k -- the number of top K terms to be displayed
4) query_file.txt -- the file containing the list of query terms on which operations are to be performed

Functions
1) getPostings(queryTerm) -- get the list of document ids containing the query term
2) termAtATimeQueryAnd(queryTerm1,queryTerm2,....,queryTermN) --
3) getTopK - return top K terms
4) termAtATimeQueryOr
5) docAtATimeQueryAnd
6) docAtATimeQueryOr

Ouput:

getTopK -- returns top K terms
... to be updated

1) List of top K terms
2) Output for various queries as listed in above file

*/

/*
 TODO:
 1) check all cmd line args and create and read files
 2) Build list and other data structures required
 3) Start processing queries 1 by 1
 4) Optimise queires and reduce comparison

*/

import java.io.*;
import java.util.*;

public class CSE535Assignment{

 // List of variables and constants required
 private static final int NUMBEROFARGS = 4;
 private static final String TERMSPLITTER = "\\\\c";
 private static final String TERMFREQUENCYSPLITTER = "\\\\m";
 private static final String POSTINGBYDOCIDINCREASE = "PostingByDocIDIncrease";
 private static final String POSTINGBYTERMFREQDECREASE = "PostingByTermFreqDecrease";

 private static int numberOfTopTerms;
 private static  String postingsFile, outputFile, queryTermFile;
 private static Map<Integer, Set<String>> termFrequencyTermMap;
 private static Map<String, Map<String,LinkedList<Integer>>> postingsMap;

 public static void main(String[] args){

   try{
     processArgs(args);
     readInputAndBuildIndexFile();
     getTopKTerms();
     //processQueryFile();
   }catch(Exception e){
     e.printStackTrace();
   }
 }

private static void processQueryFile() throws Exception{
  if(!checkFileRead(queryTermFile)){
    throw new Exception("Cannot read " + queryTermFile);
  }
}

private static void getTopKTerms(){
  List<Integer> termFrequencies = new ArrayList<>(new TreeSet<>(termFrequencyTermMap.keySet()));
  Collections.reverse(termFrequencies);
  Set<String> terms = null;
  int count = 0;
  StringBuilder output = new StringBuilder();
  for(Integer termFrequency : termFrequencies){
    terms = termFrequencyTermMap.get(termFrequency);
    if(terms != null){
      for(String term: terms){
        output.append(term + ", ");
        if(++count == numberOfTopTerms){
          System.out.println("topK: " + output.toString().trim().replaceAll(",$",""));
          return;
        }}
    }
  }
}

// Function to process the arguments recieved
 private static void processArgs(String[] args) throws Exception{
  if(args.length != NUMBEROFARGS){
    throw new Exception("Invalid number of arguments");
  }
  postingsFile = args[0];
  numberOfTopTerms = Integer.parseInt(args[1]);
  outputFile = args[2];
  queryTermFile = args[3];
 }

private static String getTermFromLine(String line){
    return line.split(TERMSPLITTER)[0];}

private static int getPostSizeFromLine(String line){
  return Integer.parseInt(line.split(TERMSPLITTER)[1].split(TERMFREQUENCYSPLITTER)[0].trim());}

private static String[] getPostingListFromLine(String line){
  return (line.split(TERMFREQUENCYSPLITTER)[1].replace("[","").replace("]","").replace(" ","")).split(",");}

private static boolean checkFileRead(String fileName) throws Exception{
  File fileObj = new File(fileName);
    if((fileObj.isFile() && fileObj.canRead())){
    return true;
  }return false;}

 private static void readInputAndBuildIndexFile() throws Exception{
   String currentLine, term;
   int postingSize;
   String[] postingList;
   BufferedReader postingsReader = null;
   if(!checkFileRead(postingsFile)){
     throw new Exception("Cannot Read " + postingsFile );
   }
   termFrequencyTermMap = new HashMap<>();
   postingsMap = new HashMap<>();
   postingsReader = new BufferedReader(new FileReader(postingsFile));
   try{
     while((currentLine = postingsReader.readLine()) != null){
       term = getTermFromLine(currentLine);
       postingSize = getPostSizeFromLine(currentLine);
       postingList = getPostingListFromLine(currentLine);
       insertTotalTermFrequencyMap(term, postingSize);
       generatePostings(term, postingList);
    }
  }finally{
     if(postingsReader != null)
      postingsReader.close();
   }
 }

private static void insertTotalTermFrequencyMap(String term, int postingSize)throws Exception{
  if(termFrequencyTermMap.containsKey(postingSize)){
    Set<String> tempSet = termFrequencyTermMap.get(postingSize);
    tempSet.add(term);
    termFrequencyTermMap.put(postingSize, tempSet);
  }else{
    termFrequencyTermMap.put(postingSize,new TreeSet<>(Arrays.asList(term)));
  }
}

private static void generatePostings(String term, String[] postingList)throws Exception{
  Set<Integer> docIdsByIncrease = new TreeSet<>();
  Set<Integer> docIdsByTFDecrease = new TreeSet<>();
  Map<Integer, Set<Integer>> tempPostingTFMap = new HashMap<>();
  int docId, termFrequency;
  for(String termString : postingList){
    String[] docIdTF = termString.split("/");
    docId = Integer.parseInt(docIdTF[0]);
    termFrequency = Integer.parseInt(docIdTF[1]);
    docIdsByIncrease.add(docId);
    tempPostingTFMap = buildPostingTFMap(termFrequency, docId, tempPostingTFMap);
  }
  docIdsByTFDecrease = getDocIDsByTFDecrease(tempPostingTFMap);
    Map<String, LinkedList<Integer>> tempMap = new HashMap<>();
    tempMap.put(POSTINGBYDOCIDINCREASE, new LinkedList<>(docIdsByIncrease));
    tempMap.put(POSTINGBYTERMFREQDECREASE, new LinkedList<>(docIdsByTFDecrease));
    postingsMap.put(term, tempMap);
  }

private static Set<Integer> getDocIDsByTFDecrease(Map<Integer,Set<Integer>> tempPostingTFMap){
  Set<Integer> docIds = new HashSet<>();
  Set<Integer> tempSet = new TreeSet<>();
  List<Integer> termFrequencies = new ArrayList<>(tempPostingTFMap.keySet());
  Collections.sort(termFrequencies);
  for( int termFrequency: termFrequencies){
    tempSet = tempPostingTFMap.get(termFrequency);
    docIds.addAll(tempSet);
  }
  return docIds;
}

private static Map<Integer, Set<Integer>> buildPostingTFMap(int termFrequency, int docId, Map<Integer, Set<Integer>> tempPostingTFMap)throws Exception{
  Set<Integer> tempSet;
  if(!tempPostingTFMap.containsKey(termFrequency)){
    tempPostingTFMap.put(termFrequency, new TreeSet<Integer>(Arrays.asList(docId)));
  }else{
      tempSet = tempPostingTFMap.get(termFrequency);
      if(tempSet != null){
      tempSet.add(docId);
      tempPostingTFMap.put(termFrequency,tempSet);
    }
  }
  return tempPostingTFMap;
}}
