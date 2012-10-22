package ie.dcu.cngl.summarizer.main;

import ie.dcu.cngl.summarizer.Aggregator;
import ie.dcu.cngl.summarizer.Summarizer;
import ie.dcu.cngl.summarizer.Weighter;
import ie.dcu.cngl.tokenizer.Structurer;
import java.io.File;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.*;


public class Test {
	public static void main(String [] args) throws Exception {
		String text = FileUtils.readFileToString(new File("17339036.xml"), "UTF-8");
                String title = FileUtils.readFileToString(new File("17339036_title.xml"), "UTF-8");
                System.out.println(title);
		Structurer structurer = new Structurer();
                
		Weighter weighter = new Weighter();
		Aggregator aggregator = new Aggregator();               
		Summarizer summarizer = new Summarizer(structurer, weighter, aggregator);
                
                String [] phraseQuery = {"alien android"};
                
                summarizer.setQuery("alien android");
                summarizer.setPhraseQuery(phraseQuery);
                summarizer.setTitle(title);
              
		summarizer.setNumSentences(3);
		String summary = summarizer.summarize(text);
                System.out.println("****** Print summary after ******");
               	System.out.println(summary);
                
                   
	}
}
