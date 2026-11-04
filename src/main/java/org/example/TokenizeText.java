package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenizeText {
    public static void main(String[] args) throws IOException {
        //analyzer without stop words removal and stemming
        Analyzer analyzer1 = new StandardAnalyzer();
        //analyzer without stemming
        Analyzer analyzer2 = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
        //analyzer without remove stop words
        Analyzer analyzer3 = new EnglishAnalyzer(CharArraySet.EMPTY_SET);
        //analyzer with stop words removal and stemming
        Analyzer analyzer4 = new EnglishAnalyzer();

        List<Analyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzer1);
        analyzers.add(analyzer2);
        analyzers.add(analyzer3);
        analyzers.add(analyzer4);


        String text = "I like playing computer games and going to the gym. What about you?" +
                " I have a lovely cat and there is a brand new ps5 in my bedroom";

        for (Analyzer analyzer:analyzers){
            System.out.println("============");
            List<String> analyzed = analyze(text, analyzer);
            StringBuilder stringBuilder = new StringBuilder();
            for (String term: analyzed){
                stringBuilder.append("\"").append(term).append("\" ");
            }
            System.out.println(stringBuilder.toString());
        }
    }
    public static List<String> analyze(String text, Analyzer analyzer) throws IOException{
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream("FIELD_NAME", text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()){
            result.add(attr.toString());
        }
        tokenStream.close();
        return result;
    }
}
