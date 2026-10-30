package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenizeText {
    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        List<String> analyzed = analyze("I like a play computer games and go a to the gym. What about you", analyzer);
        for (String a: analyzed){
            System.out.println(a);
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
