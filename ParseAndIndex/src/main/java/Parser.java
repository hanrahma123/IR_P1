import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
// import org.apache.lucene.analysis.standard.StandardAnalyzer;
// import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
// import org.apache.lucene.analysis.core.StopAnalyzer;
// import org.apache.lucene.analysis.core.SimpleAnalyzer;
// import org.apache.lucene.analysis.core.WhitespaceAnalyzer;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
// import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Document;

// import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;

// import org.apache.lucene.search.Query;
// import org.apache.lucene.search.ScoreDoc;
// import org.apache.lucene.search.TermQuery;
// import org.apache.lucene.search.IndexSearcher;
// import org.apache.lucene.search.DocIdSetIterator;

import java.nio.file.Paths;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import org.apache.lucene.search.similarities.BM25Similarity;
// import org.apache.lucene.search.similarities.TFIDFSimilarity;
// import org.apache.lucene.search.similarities.ClassicSimilarity;
// import org.apache.lucene.search.similarities.MultiSimilarity;
// import org.apache.lucene.search.similarities.BooleanSimilarity;


public class Parser {

    private static String INDEX_DIRECTORY = "../index";
    private Analyzer analyzer;
    private Directory directory;
    private IndexWriter iwriter;

    public Parser() throws IOException{
        // 1.)Analyzer that is used to process TextField
        this.analyzer = new EnglishAnalyzer();
        // 2.)Directory containing index
        this.directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        // 3.)IW-config
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(new BM25Similarity());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // 4.)Index Writer
        iwriter = new IndexWriter(directory, config);
    }

    public void InsertDoc(HashMap<String,String> docMap) throws IOException{

        // Create a new document
        Document doc = new Document();
        for( String key : docMap.keySet()){
            doc.add(new TextField(key, docMap.get(key), Field.Store.YES));
        }
        // add document to index writer
        iwriter.addDocument(doc);
    }

    
    public void ParseContent(String[] args) throws FileNotFoundException, IOException{
        
        for (String arg : args){
            int index = 0;
            File myObj = new File(arg);
            Scanner myReader = new Scanner(myObj);
            String content = "";
            char currentField = Character.MIN_VALUE;
            HashMap<Character,String> fieldMap = new HashMap<>();
            //Map to store per document fields
            HashMap<String,String> doc = new HashMap<>();

            //Map doc fields to long names: I,T,A,B,W
            fieldMap.put('I', "Index");
            fieldMap.put('T', "Title");
            fieldMap.put('A', "Author");
            fieldMap.put('B', "B");
            fieldMap.put('W', "Content");

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.charAt(0) == '.'){
                    //new field, eject content string
                    if(!content.isEmpty()){
                        //insert into HashMap
                        String f = fieldMap.get(currentField);
                        doc.put(f,content);
                        content = "";
                    }
                    currentField = data.charAt(1);
                    if( data.charAt(1) == 'I') {
                        index++;
                        content = Integer.toString(index);
                        InsertDoc(doc);
                    }
                }
                else{
                    if(content != ""){content += " ";}
                    content += data.trim();
                }
            }
            //final document
            String f = fieldMap.get(currentField);
            doc.put(f,content);

        System.out.print("Successfully wrote documents to index: "+ (index) + ("\n"));
        myReader.close();
        }
    }

    public void tearDown() throws IOException{ 
        //commit index and close
        iwriter.close();
        directory.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length <= 0)
            {
                System.out.println("Expected cran.all.1400 as input");
                System.exit(1);            
            }
        
        Parser p = new Parser();
        p.ParseContent(args);
        p.tearDown();
    }
}