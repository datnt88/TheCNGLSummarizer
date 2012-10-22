package ie.dcu.cngl.summarizer.main;

import ie.dcu.cngl.summarizer.Aggregator;
import ie.dcu.cngl.summarizer.Summarizer;
import ie.dcu.cngl.summarizer.Weighter;
import ie.dcu.cngl.tokenizer.Structurer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class InexRun {

    public static void main(String[] args) throws Exception {

        String[] index = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
            "29", "30", "31", "32", "33", "34", "35"};

        //Reading title for query for summarizer
        BufferedReader query_br;
        String line = "";
        ArrayList<String> queriesList = new ArrayList<String>();
        File queriesFile = new File("Inex2012_reference_Run/queries");
        query_br = new BufferedReader(new FileReader(queriesFile));
        while ((line = query_br.readLine()) != null) {
            queriesList.add(line);
        }


        //System.out.println(queriesList.size());

        //Reading file for phrase query
        BufferedReader phraseQuery_br;

        ArrayList<String> phraseQueriesList = new ArrayList<String>();
        phraseQuery_br = new BufferedReader(new FileReader(new File("Inex2012_reference_Run/phraseQueries")));
        while ((line = phraseQuery_br.readLine()) != null) {
            phraseQueriesList.add(line);
        }

        ////////////////////////////////////


        String doc_root = "Inex2012_reference_Run";
        String doc_folder = "";
        String filename = "";
        String filepath = "";

        String text = "";
        String title = "";
        String query = "";
        String phraseQuery = "";

        //4 sumarizer

        //4 print 2 file
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        BufferedWriter bw;
        File tmp;

        for (int topicIndex = 0; topicIndex < 35; topicIndex++) {
            query = queriesList.get(topicIndex);
            phraseQuery = phraseQueriesList.get(topicIndex);
            //System.out.println("--------" + phraseQuery);
            


            //System.out.println("Query of group " + topicIndex + " is: " + query);
            doc_folder = "Inex2012_reference_Run/20120" + index[topicIndex];
            System.out.println(doc_folder);

            try {
                File folder = new File(doc_folder);

                File[] listOfFiles = folder.listFiles();
                int numOfFiles = listOfFiles.length;

                for (int i = 0; i < numOfFiles; i++) {
                    filename = listOfFiles[i].getName();
                    //System.out.println("\nProcessing documnet: " + filename);
                    filepath = listOfFiles[i].getPath();

                    text = FileUtils.readFileToString(new File("INEX_bdy/" + filename), "UTF-8");
                    title = FileUtils.readFileToString(new File("INEX_title/" + filename), "UTF-8");


                    Structurer structurer = new Structurer();
                    Weighter weighter = new Weighter();
                    Aggregator aggregator = new Aggregator();


                    Summarizer summarizer = new Summarizer(structurer, weighter, aggregator);
                    summarizer.setTitle(title);
                    summarizer.setQuery(query);
                    summarizer.setPhraseQuery(phraseQuery.split(","));
                    


                    summarizer.setNumSentences(3);
                    String summary = summarizer.summarize(text);
                    //Print summary to file
                    //print2file#
                    if (summary.contains(">") | summary.contains("<")) {
                        System.out.println(filename);
                    }
                    try {
                        tmp = new File("results4/20120" + index[topicIndex] + "/" + filename + "_summary");
                        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp), "UTF8"));

                        //bw.write(StringEscapeUtils.unescapeXml(summary));
                        bw.write(summary);
                        //System.out.println("Wrote sumary of " + filename + " document to file");
                        bw.close();

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println(filename);
                    }
                }
                System.out.println("Done group " + (topicIndex + 1));
                System.out.println("---------------------------------------------------");



            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }// end try 1

        }
    }
}
