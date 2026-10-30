package org.example;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.example.index.CreateIndex;
import org.example.search.SearchIndex;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        String[] analyzers = {"standard","stemming","stopwords","english"};
        String[] similarities = {"bm25", "classic","lmdirichlet","dfr"};
        for(String analyzer:analyzers){
            CreateIndex.creat(analyzer);
        }
        System.out.println("All 4 type of Index created");

        for(String analyzer:analyzers){
            for (String similarity:similarities){
                SearchIndex.search(analyzer,similarity);
            }
        }
        System.out.println("All 16 experiments finished.");


    }
}