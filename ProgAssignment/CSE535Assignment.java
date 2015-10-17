

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
import java.utils.*;

public class CSE535Assignment{
 

 public static void main(String[] args){
 
 System.out.println("len: " + args.length());
 
 }

}
