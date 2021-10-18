import java.io.File;  // Import the File class
import java.io.FileWriter;
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Document;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;

import java.nio.file.Paths;

import org.apache.lucene.search.similarities.BM25Similarity;

import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

public class Querier {

    private static String INDEX_DIRECTORY = "../index";
    private Analyzer analyzer;
    private Directory directory;
    private IndexWriter iwriter;
    private FileWriter fileWriter;

    public Querier() throws IOException{
        // 1.)Analyzer that is used to process TextField
        this.analyzer = new EnglishAnalyzer();
        // 2.)Directory containing index
        this.directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        //file writer is kept open
        this.fileWriter = new FileWriter("../cran/results.txt");
    }

    public void Query(HashMap<String,String> queryMap) throws IOException, ParseException{
    
        int MAX_RESULTS = 1400;
        //1.) Create a directory reader
        DirectoryReader ireader = DirectoryReader.open(directory);
        //2.) Created and index searcher
        IndexSearcher isearcher = new IndexSearcher(ireader);
        isearcher.setSimilarity(new BM25Similarity());
        //3.) Construct Query
        MultiFieldQueryParser parser = new MultiFieldQueryParser( new String[] {/*"Index", "Author", "B",*/"Title", "Content"} ,analyzer);
        String queryString = queryMap.get("Query");
        

        // trim leading and trailing whitespace from the query
        queryString = queryString.trim();
        queryString = queryString.replace("?","\\?"); //escape '?' NOTE: Unsure what to do with '?' in qry could be REGEX or just a search string, escaping for now
        // if the user entered a querystring
        if (queryString.length() > 0)
        {   
            // parse the query with the parser
            Query query = parser.parse(queryString);
            // 4.)Get the set of results
            ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;
            for (int i = 0; i < hits.length; i++)
            {
                Document hitDoc = isearcher.doc(hits[i].doc);
                fileWriter.write(queryMap.get("Index")+ " " + "Q0" + " " + hitDoc.get("Index") + " " + "1" + " " + hits[i].score + " "+"STANDARD\n");
            }
        }

    }

    public void ParseQuery(String[] args) throws IOException, FileNotFoundException, ParseException{
        
        for (String arg : args)
        {
            File myObj = new File(arg);
            int index = 0;
            Scanner myReader = new Scanner(myObj);
            String content = "";

            char currentField = Character.MIN_VALUE;
            HashMap<Character,String> fieldMap = new HashMap<>();
            HashMap<String,String> query = new HashMap<>();

            //Map doc fields to long names: I, Q
            fieldMap.put('I', "Index");
            fieldMap.put('W', "Query");

            while (myReader.hasNextLine()) {
                
                String data = myReader.nextLine();
                if(data.charAt(0) == '.'){
                    //new field, eject content string
                    if(!content.isEmpty()){
                        //insert into HashMap
                        String f = fieldMap.get(currentField);
                        query.put(f,content);
                        if (currentField == 'W'){ Query(query);}
                        content = "";
                    }
                    currentField = data.charAt(1);

                    if( data.charAt(1) == 'I') {
                        index++;
                        content = Integer.toString(index);
                    } 
                }
                else{
                    if(content != ""){content += " ";}
                    content += data.trim();
                }
            }
            //final query
            String f = fieldMap.get(currentField);
            query.put(f,content);
            Query(query);
        System.out.print("Successfully read queries: "+ (index) + ("\n"));
        myReader.close();
        }
    }
    
    public void tearDown() throws IOException{
        directory.close();
        //commit and close filewriter
        fileWriter.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length <= 0)
            {
                System.out.println("Expected cran.qry as input");
                System.exit(1);
            }
        Querier q = new Querier();
        q.ParseQuery(args);
        q.tearDown();
    }
}