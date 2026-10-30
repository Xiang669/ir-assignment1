package org.example.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.example.analyzer.AnalyzerFactory;
import org.example.similarity.SimilarityFactory;
import org.example.util.FileUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

import static org.example.config.Config.INDEX_BASE_DIR;

public class CreateIndex {
    public static void creat(String analyzerType) throws IOException {
        //read document
        List<Document> documents = FileUtil.readDocuments();
        //create an analyzer
        Analyzer analyzer = AnalyzerFactory.getAnalyzer(analyzerType);
        System.out.println("analyzer:" + analyzerType);
        //store an index in disk
        Directory directory =  FSDirectory.open(Paths.get(INDEX_BASE_DIR+"_"+analyzerType));
        //config the analyzer to index writer
        //default similarity is BM25(k1=1.2,b=0.75)
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //set similarity to classic which is based on vector space model approaching.
        //creat index writer
        IndexWriter indexWriter = new IndexWriter(directory, config);


        for (Document d:documents){
            indexWriter.addDocument(d);
        }
        indexWriter.commit();
        indexWriter.close();
        System.out.println("write Index with analyzer: "+analyzerType+ " analyzer success ");


    }
}
