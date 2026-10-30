package org.example.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.example.analyzer.AnalyzerFactory;
import org.example.similarity.SimilarityFactory;
import org.example.util.FileUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

import static org.example.config.Config.*;

public class SearchIndex {
    public static void search(String analyzerType,String similarityType) throws ParseException, IOException{

            List<String> querys = FileUtil.readQuerys();

            //write results to output_res
            String runTag = analyzerType+"_"+similarityType;
            File writeFile = new File(OUTPUT_RES+runTag+".res");
            FileWriter fileWriter = new FileWriter(writeFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            //create same analyzer as Create index
            Analyzer analyzer = AnalyzerFactory.getAnalyzer(analyzerType);

            //create queryParser
            QueryParser queryParser = new QueryParser("content",analyzer);
            //open Index directory
            Directory directory = FSDirectory.open(Paths.get(INDEX_BASE_DIR+"_"+analyzerType));
            //creat query content
            int qid = 0;
            for (String q:querys){
                qid++;
                Query query = null;
                try {
                    String escape = QueryParser.escape(q);
                    query = queryParser.parse(escape);
                } catch (ParseException e) {
                    System.out.println("qid="+qid+" raw queryStr=["+q+"]");
                    System.out.println("qid"+qid+"fail to analyze");
                    continue;
                }
                IndexReader indexReader = DirectoryReader.open(directory);
                //Default similarity is BM25(k1=1.2,b=0.75)
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                //set similarity to VSM
                indexSearcher.setSimilarity(SimilarityFactory.getSimilarity(similarityType));
                TopDocs topDocs = indexSearcher.search(query, TOP_K);
                ScoreDoc[] hits = topDocs.scoreDocs;
                //System.out.println("qid "+qid+" 检索返回文档数量："+hits.length); //新增打印
                for (int i = 0; i < hits.length; i++) {
                    ScoreDoc hit = hits[i];
                    String docId = indexSearcher.storedFields().document(hit.doc).get("docId");
                    int rank = i+1;
                    double score = hit.score;
                    // runTag points out the analyzer and similarity type;
                    //String outline = String.format("%d Q0 %s %d %.6f %s\n", qid, docId, rank, score, runTag);
                    String outline = String.format("%d Q0 %s %d %.6f %s\n", qid, docId, rank, score, runTag);
                    bufferedWriter.write(outline);
                }

            }
            System.out.println("Finished，overall"+qid+"results were written in："+OUTPUT_RES);
            System.out.println("Saved"+runTag+".res");
            bufferedWriter.flush();
            bufferedWriter.close();
        }
    }
