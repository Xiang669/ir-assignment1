package org.example.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.config.Config.DOCS_PATH;
import static org.example.config.Config.QUERIES_PATH;

public class FileUtil {

    public static List<Document> readDocuments() throws IOException {
        File file = new File(DOCS_PATH);
        List<Document> documents= new ArrayList<>();
        if(file.isFile()){
            //read file by char
            FileReader reader = new FileReader(file);
            BufferedReader bf = new BufferedReader(reader);
            String line;
            String docId = null;
            String title = null;
            String author = null;
            StringBuilder contentBuffer = new StringBuilder();
            char currentTag = ' ';

            while((line = bf.readLine())!=null){
                line = line.trim();
                if (line.startsWith(".I")){
                    if (currentTag!=' ' && docId!=null){
                        Document document = new Document();
                        document.add(new StringField("docId",docId, Field.Store.YES));
                        document.add(new TextField("title",title, Field.Store.YES));
                        document.add(new TextField("author",author, Field.Store.YES));
                        document.add(new TextField("content",contentBuffer.toString(), Field.Store.YES));
                        documents.add(document);
                    }
                    currentTag = 'I';
                    docId = line.substring(2).trim();
                    title = "";
                    author = "";
                    contentBuffer = new StringBuilder();
                }else if (line.startsWith(".T")){
                    currentTag='T';
                }else if (line.startsWith(".A")){
                    currentTag='A';
                }else if (line.startsWith(".W")){
                    currentTag='W';
                }
                else if (currentTag == 'T'){
                    title +=  line + " ";
                }
                else if (currentTag == 'A'){
                    author += line;
                }
                else if (currentTag == 'W'){
                    contentBuffer.append(line).append(" ");
                }
            }
            if (currentTag!=' ' && docId != null){
                Document document = new Document();
                document.add(new StringField("docId",docId, Field.Store.YES));
                document.add(new TextField("title",title, Field.Store.YES));
                document.add(new TextField("author",author, Field.Store.YES));
                document.add(new TextField("content",contentBuffer.toString(), Field.Store.YES));
                documents.add(document);
            }

        }
        return documents;
    }
    public static List<String> readQuerys() throws IOException {
        File file = new File(QUERIES_PATH);
        List<String> querys = new ArrayList<>();
        if (file.isFile()){
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder query = new StringBuilder();
            char currentTag = ' ';
            String line = "";
            //read all the querys in doc
            while ((line=bufferedReader.readLine())!=null){
                if (line.startsWith(".I")){
                    if(currentTag!=' '){
                        querys.add(query.toString().trim());
                        query = new StringBuilder();
                    }
                    currentTag = 'I';
                }
                else if (line.startsWith(".W")){
                    currentTag = 'W';
                }
                else if (currentTag == 'W'){
                    query.append(line).append(" ");
                }
            }
            if (currentTag=='W'){
                querys.add(query.toString().trim());
            }
        }
        return querys;
    }
}
