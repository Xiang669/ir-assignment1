package org.example.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerFactory {

    public static Analyzer getAnalyzer(String choice) {
        if (choice == null || choice.isBlank()) {
            return new StandardAnalyzer();
        }
        return switch (choice.trim().toLowerCase()) {
            //cran.all.1400 are all English so use Standard/English analyzer

            //Standard analyzer (no stemming and remove stop words)
            case "standard" -> new StandardAnalyzer();
            //stemming but do not remove stop words
            case "stemming" -> new EnglishAnalyzer(CharArraySet.EMPTY_SET);
            //remove stop words but do not stemming
            case "stopwords" -> new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
            //stemming and remove stop words
            case "english" -> new EnglishAnalyzer();
            default -> {
                System.out.println("[Warning] Unknown Analyzer '" + choice + "', use default StandardAnalyzer");
                yield new StandardAnalyzer();
            }
        };
    }
}
