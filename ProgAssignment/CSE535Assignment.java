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
3) getTopK - return top K terms
2) termAtATimeQueryAnd(queryTerm1,queryTerm2,....,queryTermN) -- Rest follows the same format
4) termAtATimeQueryOr
5) docAtATimeQueryAnd
6) docAtATimeQueryOr

 TODO:
 1) check all cmd line args and create and read files
 2) Build list and other data structures required
 3) Start processing queries 1 by 1
 4) Optimise queires and reduce comparison
*/

import java.io.*;
import java.util.*;

public class CSE535Assignment{

    //Constants used to split terms and identify term,posting size and posting list
    private static final int NUMBEROFARGS = 4;
    private static final String TERMSPLITTER = "\\\\c";
    private static final String TERMFREQUENCYSPLITTER = "\\\\m";
    // Constants to identify the posting type in the posting lists
    private static final String POSTINGBYDOCIDINCREASE = "PostingByDocIDIncrease";
    private static final String POSTINGBYTERMFREQDECREASE = "PostingByTermFreqDecrease";

    // File writer object to write to output file specified in CMD line args
    private static BufferedWriter logFileWriter;
    // Map containing the terms with their respective frequencies used to obtain topk terms
    private static TreeMap<Integer, Set<String>> termFrequencyTermMap;
    // Global map contianing the posting list
    // Each Term points to a map,which contains { postingType : <list of doc ids arranged by the posting strategy specified > }
    private static Map<String, Map<String,LinkedList<Integer>>> postingsMap;

    // Entry point to the code
    public static void main(String[] args){
        try{
            readInputAndBuildIndexFile(args[0]);
            createOutputFile(args[1]);
            getTopKTerms(Integer.parseInt(args[2]));
            processQueryFile(args[3]);
            if(logFileWriter != null){
              logFileWriter.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }    }

    // Creates the output File and assigns the BufferedWriter object and throws Exception if unable to create file writer object
    private static void createOutputFile(String outputFile)throws Exception{
      try{
        File logFile = new File(outputFile);
        logFileWriter = new BufferedWriter(new FileWriter(logFile));
      }catch(Exception e){
      throw new Exception("Unable to write to " + outputFile);
      }
    }

    /*
      Takes query File as Input and performs all the required computations and writes it to the file
      List of operations performed
      1) Getting postings individually for each term
      2) term postings contains the mini map, each term pointing to a map of posting type
      3) get specific postings required by the taat and daat operations specifically
      4) Perform termAtATimeQueryAnd operation on the returned by the getSpecificPostings()
      5) Perform termAtATimeQueryOr operation on the returned by the getSpecificPostings()
      6) Perform docAtATimeQueryAnd operation on the returned by the getSpecificPostings()
      7) Perform docAtATimeQueryOr operation on the returned by the getSpecificPostings()
    */
    private static void processQueryFile(String queryTermFile) throws Exception{
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
            tAATPostings = getSpecificPostings(termPostings,POSTINGBYTERMFREQDECREASE);
            dAATPostings = getSpecificPostings(termPostings,POSTINGBYDOCIDINCREASE);
            termAtATimeQueryAnd(tAATPostings);
            termAtATimeQueryOr(tAATPostings);
            docAtATimeQueryAnd(dAATPostings);
            docAtATimeQueryOr(dAATPostings);
        }
    }

    /*
      Returns the specific type of postings required by the caller function
    */
    private static Map<String, LinkedList<Integer>> getSpecificPostings(Map<String, Map<String, LinkedList<Integer>>> arg,String postingType ){
        Map<String, LinkedList<Integer>> result = new HashMap<>();
        for(Map.Entry<String, Map<String, LinkedList<Integer>>> entry : arg.entrySet()){
            result.put(entry.getKey(), entry.getValue().get(postingType));
        }return result;
    }

    //Writes the given string to the output File specified in command line arguments
    private static void writeToOutputFile(String msg){
      try{
        if(logFileWriter != null){
        logFileWriter.write(msg);
      }else{
        System.out.println(msg);
        }
      }catch(Exception e){
      System.out.println("Unable to write to output file");
      }
    }

    /*
    Performs the termAtATimeQueryAnd operation on the given posting lists
      It handles cases where
      1) posting list is null, and
      2) if one the the terms is not found , then it prematurely decides And operation cannot be performed
      3) If everything is well, then iteratively performs termAtATimeQueryAnd operation by taking 2 posting lists at a time
      4) It performs optimization on the posting lists to implement the optional bonus part
      5) Prepares rest of the data required by the output
    */
    private static void termAtATimeQueryAnd(Map<String, LinkedList<Integer>> termPostings){
        StringBuilder output = new StringBuilder();
        LinkedList<Integer> result = new LinkedList<>();
        int numOfCompares = 0;
        output.append("FUNCTION: termAtATimeQueryAnd ");
        output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
        long start = System.currentTimeMillis();

        if(termPostings == null && termPostings.size() == 1){
            result = termPostings.get(termPostings.keySet().toArray()[0]);
        }
        else if(termPostings.size() >= 2){
            String[] terms = termPostings.keySet().toArray(new String[0]);
            result = termPostings.get(terms[0]);
            for(String term : Arrays.copyOfRange(terms,1,terms.length)){
                result = termAnd(result, termPostings.get(term));
                numOfCompares += result.removeLast();
            }
        }
        int optimisedCompares = getOptimisedRunTAATAnd(new ArrayList<LinkedList<Integer>>(termPostings.values()));
        double diff= (System.currentTimeMillis() - start)/1000;
        output.append(Integer.toString(result.size())+" documents are found\n");
        output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
        output.append(Integer.toString(optimisedCompares)+" comparisions are made with optimization()\n");
        output.append(Double.toString(diff)+" seconds are used\n");
        output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
        writeToOutputFile(output.toString());
    }

    //Rearranges the posting lists to perform query optimization
    private static ArrayList<LinkedList<Integer>> optimisePostings(ArrayList<LinkedList<Integer>> postings){
        TreeMap<Integer, LinkedList<Integer>> optPosts = new TreeMap<>();
        for(LinkedList<Integer> posts : postings){
            optPosts.put(posts.size(), posts);
        }
        return new ArrayList<LinkedList<Integer>>(optPosts.values());
    }

    //Runs the optimised postings list and returns the number of comparisions
    private static int getOptimisedRunTAATAnd(ArrayList<LinkedList<Integer>> postings){
        postings = optimisePostings(postings);
        int numOfCompares = 0;
        LinkedList<Integer> result = postings.get(0);
        for(int i=1 , n = postings.size();i< n; i++){
            result = termAnd(result, postings.get(i));
            numOfCompares += result.removeLast();
        }
        return numOfCompares;
    }

    /*
      Performs And operation on given two posting lists
      Returns the AND intersection of the two lists
      Runs in O(M*N) time as no optimization can be performed, where M and N are sizes of arguments (1 and 2)
      but tries to save some comparisions by breaking out of the loop once a match is obtained
    */
    private static LinkedList<Integer> termAnd(LinkedList<Integer> arg1, LinkedList<Integer> arg2){
        LinkedList<Integer> result  =  new LinkedList<>();
        int numOfCompares = 0, list1Item;
        for(int i =0, n = arg1.size() ; i < n; i++){
            list1Item = arg1.get(i).intValue();
            for(int j= 0, m = arg2.size(); j< m; j++){
                numOfCompares++;
                if(arg1.get(i).intValue() == arg2.get(j).intValue()){
                    result.add(arg1.get(i));
                    break;
                }
            }
        }
        Collections.sort(result);
        result.addLast(numOfCompares);
        return result;
    }

    /*
    Checks whether a given element is already present in the given Collection
    returns a boolean response
    */
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

    /*
    Performs Or operation on the given two posting lists
    Performs premature optimization by finding if any one of the lists are null
    number of comparisons are appended at the end of the list and extracted by the caller to keep an accurate count
    */
    private static LinkedList<Integer> termOr(LinkedList<Integer> arg1, LinkedList<Integer> arg2){
        if(arg1.size() == 0){ arg2.add(0); return arg2;}
        if(arg2.size() == 0){ arg1.add(0); return arg1;}
        TreeSet<Integer> result  =  new TreeSet<>(arg1);
        int numOfCompares = 0;
        ListIterator<Integer> arg1Iterator = arg1.listIterator();
        ListIterator<Integer> arg2Iterator = arg2.listIterator();
        int list1Item = -1, list2Item = -1;

        while(arg2Iterator.hasNext()){
            list2Item = arg2Iterator.next().intValue();
            numOfCompares += result.size();
            if(!elementExists(result,list2Item)){
                result.add(list2Item);
            }
        }
        LinkedList<Integer> response = new LinkedList<Integer>(result);
        response.addLast(numOfCompares);
        return response;
    }

    /*
    Performs the termAtATimeQueryOr operation on the given posting lists
      It handles cases where
      1) posting list is null, and
      2) if one the the terms is not found , then it performs OR on remaining lists
      3) If everything is well, then iteratively performs termAtATimeQueryAnd operation by taking 2 posting lists at a time
      4) It performs optimization on the posting lists to implement the optional bonus part
      5) Prepares rest of the data required by the output
    */
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
        double diff= (System.currentTimeMillis() - start)/1000;
        output.append(Integer.toString(result.size())+" documents are found\n");
        output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
        output.append(Double.toString(diff)+" seconds are used\n");
        output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
        writeToOutputFile(output.toString());
    }

    /*
    Performs the docAtATimeQueryAnd operation on the given posting lists
      It handles cases where
      1) posting list is null, and
      2) if one the the terms is not found , then it prematurely decides And operation cannot be performed
      3) If everything is well, then concurrently performs And opertion by following the below mentioned algorithm
        Algorithm: docAtATimeQueryAnd ( list(postingLists of size N))
            Array nums of size N: to store the current element being tracked
            Array indices of size N: to the store the indices of the current element being tracked

            While( All the lists are exhausted)
            {
              min = getMin(nums);
              result.add(min);
              update the indices and nums of the lists who have min
              if the index > list.size()
                set it to Integer.MAX_VALUE

              //Exit conditon
              if(min  = Integer.MAX_VALUE)
                This indicates all the lists have been exhausted
                hence the function terminates
              }

        */
    private static LinkedList<Integer> docAtATimeQueryAnd(Map<String, LinkedList<Integer>> termPostings){
        StringBuilder output = new StringBuilder();
        LinkedList<Integer> result  =  new LinkedList<>();
        int numOfCompares = 0;
        long start = System.currentTimeMillis();
        output.append("FUNCTION: docAtATimeQueryAnd ");
        output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
        ArrayList<LinkedList<Integer>> iteratorList = new ArrayList<>();
        for(LinkedList<Integer> value : termPostings.values()){
            if(value.size() > 1){
                iteratorList.add(value);
            }
        }
        if(iteratorList.size() < termPostings.size()){
          numOfCompares = 0;
        }
        else if(iteratorList.size() == 1){
          result = iteratorList.get(0);
        }
        else if(iteratorList.size() > 1){
            result = daatAnd(iteratorList);
            numOfCompares = result.removeLast();
        }
        double diff= (System.currentTimeMillis() - start)/1000;
        output.append(Integer.toString(result.size())+" documents are found\n");
        output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
        output.append(Double.toString(diff)+" seconds are used\n");
        output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
        writeToOutputFile(output.toString());
        return result;
    }

    //performs sum operation on the given number of Integers
    private static int sum(Integer[] arg){
        int sum = 0;
        for(Integer num: arg){
            sum += num.intValue();
        }
        return sum;
    }

    /*
    Algorithm: daatAnd ( list(postingLists of size N))
        Array nums of size N: to store the current element being tracked
        Array indices of size N: to the store the indices of the current element being tracked

        While( All the lists are exhausted)
        {
          min = getMin(nums);
          result.add(min);
          update the indices and nums of the lists who have min
          if the index > list.size() //Exit conditon
            set it to Integer.MAX_VALUE // If anyone of the list is exhausted , then no AND operation can be performed further
          if(max(nums)  = Integer.MAX_VALUE) // exit function
            This indicates one of the lists have been exhausted
            hence the function terminates
          }
          */
      private static LinkedList<Integer> daatAnd(ArrayList<LinkedList<Integer>> iteratorList){
        LinkedList<Integer> result = new LinkedList<>();
        Integer[] nums = new Integer[iteratorList.size()];
        Integer[] indices =  new Integer[iteratorList.size()];
        int i, min , numOfCompares = 0;
        for(i = 0 ; i < iteratorList.size(); i++){
            if(iteratorList.get(i).size() > 0 ){
                nums[i] = iteratorList.get(i).get(0);
                indices[i] = 0;
            }}
        while(true){
            numOfCompares += iteratorList.size();
            min = Collections.min(Arrays.asList(nums));
            if(min != Integer.MAX_VALUE && sum(nums) == (iteratorList.size()*min)){
                result.add(min);}
            for(i = 0 ; i < iteratorList.size(); i++){
                if(indices[i] < iteratorList.get(i).size()){
                    if(iteratorList.get(i).get(indices[i]).intValue() == min){
                        indices[i]++;
                        if(indices[i] < iteratorList.get(i).size()){
                            nums[i] = iteratorList.get(i).get(indices[i]);
                        }  else{   indices[i] = nums[i] = Integer.MAX_VALUE; break;}
                    }
                }
            }

            if(Collections.max(Arrays.asList(indices)).intValue() == Integer.MAX_VALUE){
                break;
            }
        }
        result.addLast(numOfCompares);
        return result;
    }

    /*
    Performs the docAtATimeQueryOr operation on the given posting lists
      It handles cases where
      1) posting list is null, and
      2) if one the the terms is not found , then it prematurely removes such lists
      3) If everything is well, then concurrently performs OR opertion by following the below algorithm mentioned in daatOr function
        */
    private static LinkedList<Integer> docAtATimeQueryOr(Map<String, LinkedList<Integer>> termPostings)throws Exception{
        StringBuilder output = new StringBuilder();
        LinkedList<Integer> result  =  new LinkedList<>();
        int numOfCompares = 0;
        long start = System.currentTimeMillis();
        output.append("FUNCTION: docAtATimeQueryOr ");
        output.append(Arrays.toString(termPostings.keySet().toArray(new String[0])).replace("[","").replace("]","").trim() + "\n");
        ArrayList<LinkedList<Integer>> iteratorList = new ArrayList<>();
        for(LinkedList<Integer> value : termPostings.values()){
            if(value.size() > 1){
                iteratorList.add(value);
            }
        }
        if(iteratorList.size() > 1){
            result = daatOr(iteratorList);
            numOfCompares = result.removeLast();
        }
        else if(iteratorList.size() == 1){
            result = iteratorList.get(0);
        }
        double diff= (System.currentTimeMillis() - start)/1000;
        output.append(Integer.toString(result.size())+" documents are found\n");
        output.append(Integer.toString(numOfCompares)+" comparisions are made\n");
        output.append(Double.toString(diff)+" seconds are used\n");
        output.append("Result: " + Arrays.toString(result.toArray()).replace("[","").replace("]","") + "\n");
        writeToOutputFile(output.toString());
        return result;
    }

    /*
    Algorithm: daatOr ( list(postingLists of size N))
        Array nums of size N: to store the current element being tracked
        Array indices of size N: to the store the indices of the current element being tracked

        While( All the lists are exhausted)
        {
          min = getMin(nums);
          if( min*N == sum(nums)) // This works only if doc Ids is numbers ,
                                  // it exploits the property of numbers by taking the min and matching it with the number of occurences
          result.add(min);
          update the indices and nums of the lists who have min
          if the index > list.size()
            set it to Integer.MAX_VALUE
          //Exit conditon
          if(min  = Integer.MAX_VALUE)
            This indicates all the lists have been exhausted
            hence the function terminates
          }
          */
    private static LinkedList<Integer> daatOr(ArrayList<LinkedList<Integer>> iteratorList) throws Exception{
        LinkedList<Integer> result = new LinkedList<>();
        Integer[] nums = new Integer[iteratorList.size()];
        Integer[] indices =  new Integer[iteratorList.size()];
        int i = 0, min, numOfCompares=0;
        for(i = 0 ; i < iteratorList.size(); i++){
            if(iteratorList.get(i).size() > 0 ){
                nums[i] = iteratorList.get(i).get(0);
                indices[i] = 0;
            }}
        while(true){
            numOfCompares += iteratorList.size();
            min = Collections.min(Arrays.asList(nums));
            if(min != Integer.MAX_VALUE){     result.add(min);}
            for(i = 0; i < iteratorList.size(); i++){
                if(nums[i].intValue() == min){
                    ++indices[i];
                    if(indices[i].intValue() >= iteratorList.get(i).size()){
                        indices[i] = Integer.MAX_VALUE;
                        nums[i] = Integer.MAX_VALUE;
                    }
                    else{
                        nums[i] = iteratorList.get(i).get(indices[i]);
                    }
                }
            }
            if(Collections.min(Arrays.asList(indices)).intValue() == Integer.MAX_VALUE){
                break;
            }
        }
        result.addLast(numOfCompares);
        return result;
    }

    // Returns postings for the given list of terms, for non-existent term , returns empty list
    // internally calls getPostings()
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

    // Returns postings for a given term , null for non-existent term
    // called by getTermPostings
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
        writeToOutputFile(result.toString());
        return response;
    }

    // returns the top K terms as specified , if number of terms is less than top terms required, exits when list is exhausted
    private static void getTopKTerms(int numberOfTopTerms){
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
        result = result + "\n";
        writeToOutputFile(result);
    }

    // Checks whether the given filename is readable
    private static boolean checkFileRead(String fileName) throws Exception{
        File fileObj = new File(fileName);
        if((fileObj.isFile() && fileObj.canRead())){
            return true;
        }return false;}

    // Takes the input file, performs few safety checks and builds the posting lists
    private static void readInputAndBuildIndexFile(String postingsFile) throws Exception{
        String currentLine, term;
        int postingSize;
        String[] postingList;
        BufferedReader postingsReader = null;
        if(!checkFileRead(postingsFile)){
            throw new Exception("Cannot Read " + postingsFile ); }
        termFrequencyTermMap = new TreeMap<>();
        postingsMap = new HashMap<>();
        postingsReader = new BufferedReader(new FileReader(postingsFile));
        try{
            while((currentLine = postingsReader.readLine()) != null){
                term = currentLine.split(TERMSPLITTER)[0];
                postingSize = Integer.parseInt(currentLine.split(TERMSPLITTER)[1].split(TERMFREQUENCYSPLITTER)[0].trim());
                postingList = (currentLine.split(TERMFREQUENCYSPLITTER)[1].replace("[","").replace("]","").replace(" ","")).split(",");
                insertTotalTermFrequencyMap(term, postingSize);
                generatePostings(term, postingList);
            }
        }finally{
            if(postingsReader != null)
                postingsReader.close();
        }
    }

    // Inserts term with its total number of occurences
    private static void insertTotalTermFrequencyMap(String term, int postingSize)throws Exception{
        if(termFrequencyTermMap.containsKey(postingSize)){
            termFrequencyTermMap.get(postingSize).add(term);
        }else{
            termFrequencyTermMap.put(postingSize,new TreeSet<String>(Arrays.asList(term)));
        }}

    // generates posting lists for different strategies as specified in the input
    private static void generatePostings(String term, String[] postingList)throws Exception{
        TreeSet<Integer> docIdsByIncrease = new TreeSet<>();
        HashSet<Integer> docIdsByTFDecrease = new HashSet<>();
        HashMap<Integer, TreeSet<Integer>> tempPostingTFMap = new HashMap<>();
        int docId, termFrequency;
        for(String termString : postingList){
            String[] docIdTF = termString.split("/");
            docId = Integer.parseInt(docIdTF[0]);
            termFrequency = Integer.parseInt(docIdTF[1]);
            docIdsByIncrease.add(docId);
            tempPostingTFMap = buildPostingTFMap(termFrequency, docId, tempPostingTFMap);
        }
        docIdsByTFDecrease = getDocIDsByTFDecrease(tempPostingTFMap);
        HashMap<String, LinkedList<Integer>> tempMap = new HashMap<>();
        tempMap.put(POSTINGBYDOCIDINCREASE, new LinkedList<Integer>(docIdsByIncrease));
        tempMap.put(POSTINGBYTERMFREQDECREASE, new LinkedList<Integer>(docIdsByTFDecrease));
        postingsMap.put(term, tempMap);
    }

    //Returns docids ordered by decreasing term frequencies order
    private static HashSet<Integer> getDocIDsByTFDecrease(HashMap<Integer, TreeSet<Integer>> tempPostingTFMap){
        HashSet<Integer> docIds = new HashSet<>();
        List<Integer> termFrequencies = new ArrayList<>(tempPostingTFMap.keySet());
        Collections.sort(termFrequencies);
        Collections.reverse(termFrequencies);
        for( int termFrequency: termFrequencies){
            docIds.addAll(tempPostingTFMap.get(termFrequency));
        }return docIds;
    }

    // Build a map of terms with their respective term frequency and doc ids list
    private static HashMap<Integer, TreeSet<Integer>> buildPostingTFMap(int termFrequency, int docId, HashMap<Integer, TreeSet<Integer>> tempPostingTFMap)throws Exception{
        if(!tempPostingTFMap.containsKey(termFrequency)){
            tempPostingTFMap.put(termFrequency, new TreeSet<Integer>(Arrays.asList(docId)));
        }else{
            tempPostingTFMap.get(termFrequency).add(docId);}
        return tempPostingTFMap;
    }
}
