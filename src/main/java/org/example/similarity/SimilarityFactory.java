package org.example.similarity;

import org.apache.lucene.search.similarities.*;

public class SimilarityFactory {

    public static Similarity getSimilarity(String choice) {
        if (choice == null || choice.isBlank()) {
            return new BM25Similarity();
        }
        return switch (choice.trim().toLowerCase()) {
            case "bm25" -> new BM25Similarity(1.2f, 0.75f);
            case "classic" -> new ClassicSimilarity();
            case "lmdirichlet" -> new LMDirichletSimilarity(2000f);
            case "dfr" -> new DFRSimilarity(
                    new BasicModelG(),
                    new AfterEffectL(),
                    new NormalizationH2()
            );
            default -> {
                System.out.println("[Warning] Unknown Similarity '" + choice + "', use default BM25Similarity");
                yield new BM25Similarity();
            }
        };
    }
}

