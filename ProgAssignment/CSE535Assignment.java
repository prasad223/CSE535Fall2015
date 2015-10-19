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
     //CheckOutputFile();
     processQueryFile();
   }catch(Exception e){
     e.printStackTrace();
   }
 }

private static void processQueryFile() throws Exception{
  if(!checkFileRead(queryTermFile)){
    throw new Exception("Cannot read " + queryTermFile);}
  BufferedReader queryTermReader = new BufferedReader(new FileReader(queryTermFile));
  String currentLine;
  String[] terms;
  LinkedList<Integer> result = new LinkedList<>();
  Map<String, Map<String, LinkedList<Integer>>> termPostings = new HashMap<>();
  Map<String, LinkedList<Integer>> tAATPostings = new HashMap<>();
  Map<String, LinkedList<Integer>> dAATPostings = new HashMap<>();
  while((currentLine = queryTermReader.readLine()) != null){
    terms = currentLine.split(" ");
    termPostings = getTermPostings(currentLine.split(" "));
    tAATPostings = getSpecificPostings(termPostings,POSTINGBYDOCIDINCREASE);
    dAATPostings = getSpecificPostings(termPostings,POSTINGBYTERMFREQDECREASE);
    termAtATimeQueryAnd(tAATPostings);
    termAtATimeQueryOr(tAATPostings);
    docAtATimeQueryAnd(dAATPostings);
    docAtATimeQueryOr(dAATPostings);
  }
}

private static Map<String, LinkedList<Integer>> getSpecificPostings(Map<String, Map<String, LinkedList<Integer>>> arg,String postingType ){
  Map<String, LinkedList<Integer>> result = new HashMap<>();
  for(Map.Entry<String, Map<String, LinkedList<Integer>>> entry : arg.entrySet()){
    result.put(entry.getKey(), entry.getValue().get(postingType));
  }return result;
}

private static void print(String msg){
  System.out.println(msg);}

private static void termAtATimeQueryAnd(Map<String, LinkedList<Integer>> termPostings){
  StringBuilder output = new StringBuilder();
  LinkedList<Integer> result = new LinkedList<>();
  int numOfCompares = 0;
  long start = System.currentTimeMillis();
  output.append("FUNCTION: termAtATimeQueryAnd ");
  output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
  if(termPostings == null || termPostings.size() == 0){
    }
  else if(termPostings.size() == 1){
    result = termPostings.get(termPostings.keySet().toArray()[0]);
  }
  else if(termPostings.size() >= 2){
    String[] terms = termPostings.keySet().toArray(new String[0]);
    result = termPostings.get(terms[0]);
    for(String term : Arrays.copyOfRange(terms,1,terms.length)){
      result = termAnd(result, termPostings.get(term));
      numOfCompares += result.removeLast();
    }}
  long diff = System.currentTimeMillis() - start;
  float timeUsed = diff/1000;
  output.append(Integer.toString(result.size())+" documents are found\n");
  output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
  output.append( String.format("%.02f", timeUsed)+" seconds are used\n");
  output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
  print(output.toString());
}

private static LinkedList<Integer> termAnd(LinkedList<Integer> arg1, LinkedList<Integer> arg2){
  LinkedList<Integer> result  =  new LinkedList<>();
  int numOfCompares = 0;
  ListIterator<Integer> arg1Iterator = arg1.listIterator();
  ListIterator<Integer> arg2Iterator = arg2.listIterator();
  int list1Item = -1, list2Item = -1;
  while(arg1Iterator.hasNext()){
    list1Item = arg1Iterator.next();
    while(arg2Iterator.hasNext()){
        list2Item = arg2Iterator.next();
        numOfCompares++;
        if(list1Item == list2Item){
          result.add(list1Item);
          if(arg1Iterator.hasNext()){   list1Item = arg1Iterator.next();}
        }}}    result.addLast(numOfCompares);
  return result;
}

private static boolean elementExists(TreeSet<Integer> arg, int element){
  boolean result = false;
  Iterator<Integer> iterator = arg.iterator();
  int num = -1;
  while(iterator.hasNext()){
    num = iterator.next();
    if(num == element){ result = true; break;}
    else if(num > element){ break;}
  } return result;
}
private static LinkedList<Integer> termOr(LinkedList<Integer> arg1, LinkedList<Integer> arg2){
  if(arg1.size() == 0){ arg2.add(0); return arg2;}
  TreeSet<Integer> result  =  new TreeSet<>(arg1);
  int numOfCompares = 0;
  ListIterator<Integer> arg1Iterator = arg1.listIterator();
  ListIterator<Integer> arg2Iterator = arg2.listIterator();
  int list1Item = -1, list2Item = -1;

  while(arg2Iterator.hasNext()){
    list2Item = arg2Iterator.next();
    numOfCompares++;
    if(!elementExists(result,list2Item)){
        result.add(list2Item);
    }
  }
  LinkedList<Integer> response = new LinkedList<Integer>(result);
  response.addLast(numOfCompares);
  return response;
}

private static void termAtATimeQueryOr(Map<String, LinkedList<Integer>> termPostings){
  StringBuilder output = new StringBuilder();
  LinkedList<Integer> result = new LinkedList<>();
  int numOfCompares = 0;
  long start = System.currentTimeMillis();
  output.append("FUNCTION: termAtATimeQueryOr ");
  output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
  if(termPostings == null || termPostings.size() == 0){
    }
  else if(termPostings.size() == 1){
    result = termPostings.get(termPostings.keySet().toArray()[0]);
  }
  else if(termPostings.size() >= 2){
    String[] terms = termPostings.keySet().toArray(new String[0]);
    result = termPostings.get(terms[0]);
    for(String term : Arrays.copyOfRange(terms,1,terms.length)){
      result = termOr(result, termPostings.get(term));
      numOfCompares += result.removeLast();
    }}
  long diff = System.currentTimeMillis() - start;
  float timeUsed = diff/1000;
  output.append(Integer.toString(result.size())+" documents are found\n");
  output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
  output.append( String.format("%.02f", timeUsed)+" seconds are used\n");
  output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
  print(output.toString());
}

private static LinkedList<Integer> docAtATimeQueryAnd(Map<String, LinkedList<Integer>> termPostings){
  StringBuilder output = new StringBuilder();
  LinkedList<Integer> result  =  new LinkedList<>();
  int numOfCompares = 0;
  long start = System.currentTimeMillis();
  output.append("FUNCTION: docAtATimeQueryAnd ");
  output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
  ArrayList<LinkedList<Integer>> iteratorList = new ArrayList<>();
  for(LinkedList<Integer> value : termPostings.values()){
    //print("val size : " + Integer.toString(value.size()));
    if(value.size() > 1){
      iteratorList.add(value);
    }
  }
  if(iteratorList.size() > 1){
  result = daatAnd(iteratorList); }
  else if(iteratorList.size() == 1 && iteratorList.size() == termPostings.size())
    result = iteratorList.get(0);
  long diff = System.currentTimeMillis() - start;
  float timeUsed = diff/1000;
  output.append(Integer.toString(result.size())+" documents are found\n");
  output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
  output.append( String.format("%.02f", timeUsed)+" seconds are used\n");
  output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
  print(output.toString());

    return result;
}

private static Integer sum(Integer[] arg){
  Integer sum = 0;
  for(Integer num: arg){
    sum += num;
  }
  return sum;
}

private static LinkedList<Integer> daatAnd(ArrayList<LinkedList<Integer>> iteratorList){
  LinkedList<Integer> result = new LinkedList<>();
  Integer[] nums = new Integer[iteratorList.size()];
  Integer[] indices =  new Integer[iteratorList.size()];
  int i = 0;
  Integer min;
  for(i = 0 ; i < iteratorList.size(); i++){
    if(iteratorList.get(i).size() > 0 ){
      nums[i] = iteratorList.get(i).get(0);
      indices[i] = 0;
    }}
    print("inti: " + Arrays.toString(nums));
  while(true){
      min = Collections.min(Arrays.asList(nums));
      if(min != Integer.MAX_VALUE && sum(nums) == (iteratorList.size()*min)){
      result.add(min);}
      for(i = 0 ; i < iteratorList.size(); i++){
        if(indices[i] < iteratorList.get(i).size()){
        if(iteratorList.get(i).get(indices[i]) == min){
          indices[i]++;
          if(indices[i] < iteratorList.get(i).size()){
          nums[i] = iteratorList.get(i).get(indices[i]);
        }  else{   indices[i] = nums[i] = Integer.MAX_VALUE; }
          }}

        }

      if(min == Integer.MAX_VALUE){
        break;
      }
    }
  return result;
}

private static LinkedList<Integer> docAtATimeQueryOr(Map<String, LinkedList<Integer>> termPostings){
  StringBuilder output = new StringBuilder();
  LinkedList<Integer> result  =  new LinkedList<>();
  int numOfCompares = 0;
  long start = System.currentTimeMillis();
  output.append("FUNCTION: docAtATimeQueryOr ");
  output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
  ArrayList<LinkedList<Integer>> iteratorList = new ArrayList<>();
  for(LinkedList<Integer> value : termPostings.values()){
    //print("val size : " + Integer.toString(value.size()));
    if(value.size() > 1){
      iteratorList.add(value);
    }
  }
  if(iteratorList.size() > 1){
  result = daatOr(iteratorList); }
  else if(iteratorList.size() == 1)
    result = iteratorList.get(0);

  long diff = System.currentTimeMillis() - start;
  float timeUsed = diff/1000;
  output.append(Integer.toString(result.size())+" documents are found\n");
  output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
  output.append( String.format("%.02f", timeUsed)+" seconds are used\n");
  output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
  print(output.toString());

    return result;
}


private static LinkedList<Integer> daatOr(ArrayList<LinkedList<Integer>> iteratorList){
  LinkedList<Integer> result = new LinkedList<>();
  Integer[] nums = new Integer[iteratorList.size()];
  Integer[] indices =  new Integer[iteratorList.size()];
  int i = 0;
  Integer min;
  for(i = 0 ; i < iteratorList.size(); i++){
    if(iteratorList.get(i).size() > 0 ){
      nums[i] = iteratorList.get(i).get(0);
      indices[i] = 0;
    }}

  while(true){
      min = Collections.min(Arrays.asList(nums));
      if(min != Integer.MAX_VALUE){
      result.add(min);}
      for(i = 0 ; i < iteratorList.size(); i++){
        if(indices[i] < iteratorList.get(i).size()){
        if(iteratorList.get(i).get(indices[i]) == min){
          indices[i]++;
          if(indices[i] < iteratorList.get(i).size()){
          nums[i] = iteratorList.get(i).get(indices[i]);
        }  else{   indices[i] = nums[i] = Integer.MAX_VALUE; }
          }}

        }

      if(min == Integer.MAX_VALUE){
        break;
      }
    }
  return result;
}

private static Map<String, Map<String, LinkedList<Integer>>> getTermPostings(String[] terms)throws Exception{
  Map<String, Map<String, LinkedList<Integer>>> termPostings = new HashMap<>();
   Map<String, LinkedList<Integer>> postings;
  for(String term: terms){
     postings = getPostings(term);
    if(postings != null && (postings instanceof HashMap)){
        termPostings.put(term, postings);
    }}
  return termPostings;
}

private static Map<String, LinkedList<Integer>> getPostings(String term){
  StringBuilder result = new StringBuilder();
  Map<String, LinkedList<Integer>> response = new HashMap<>();
  result.append("FUNCTION: getPostings " + term + "\n");
  if(postingsMap.containsKey(term)){
    response = postingsMap.get(term);
    String byDocId = Arrays.toString(response.get(POSTINGBYDOCIDINCREASE).toArray());
    String byTF = Arrays.toString(response.get(POSTINGBYTERMFREQDECREASE).toArray());
    result.append("Ordered by doc IDs: " + byDocId.replace("[","").replaceAll("]","").trim() + "\n");
    result.append("Ordered by TF: " + byTF.replace("[","").replaceAll("]","").trim() +"\n");
  }else{
      result.append("term not found\n");
      response = new HashMap<>();
      response.put(POSTINGBYDOCIDINCREASE, new LinkedList<Integer>());
      response.put(POSTINGBYTERMFREQDECREASE, new LinkedList<Integer>());
    }
  print(result.toString());
  return response;
}

private static void getTopKTerms(){
  List<Integer> termFrequencies = new ArrayList<>(new TreeSet<>(termFrequencyTermMap.keySet()));
  Collections.reverse(termFrequencies);
  Set<String> terms = null;
  int count = 0;
  boolean exitStatus = false;
  String result= "FUNCTION: getTopK " + Integer.toString(numberOfTopTerms) + "\n";
  StringBuilder output = new StringBuilder();
  for(Integer termFrequency : termFrequencies){
    terms = termFrequencyTermMap.get(termFrequency);
    if(terms != null){
      for(String term: terms){
        output.append(term + ", ");
        if(++count == numberOfTopTerms){
          exitStatus = true;
          break;
        }
      }
      if(exitStatus){
        break;
      }
    }
  }
  result = result + "Result: " + output.toString().trim().replaceAll(",$","");
  System.out.println(result);
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
     throw new Exception("Cannot Read " + postingsFile ); }
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
