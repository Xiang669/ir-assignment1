package org.example.config;

public class Config {
    // collect data path
    public static final String DOCS_PATH = "./src/main/resources/cran.all.1400";
    public static final String QUERIES_PATH = "./src/main/resources/cran.qry";
    public static final String QRELS_PATH = "./src/main/resources/QRelsCorrectedforTRECeval";
    public static String OUTPUT_RES = "./src/main/resources/output/";

    // Index output path
    public static final String INDEX_BASE_DIR = "./src/main/resources/Index";


    // Record TOP rank 50 documents
    public static final int TOP_K = 50;
}
