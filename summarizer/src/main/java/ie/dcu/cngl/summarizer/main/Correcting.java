/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.main;

import java.io.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

/**
 *
 * @author cngl
 */
public class Correcting {

    public static void main(String[] args) throws Exception {

        StringBuilder sb = new StringBuilder();
        String line = "";
        String nohtml = "";
        BufferedWriter bw;
        BufferedReader br;
        

        try {
            //Read file
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            //

            Document doc = docBuilder.parse(new File("inex-2012-sr-referencerun.xml"));

            NodeList nList;
            int numTopic;
            Element topicNode;

            nList = doc.getElementsByTagName("topic");
            numTopic = nList.getLength();

            System.out.println("The number of topics is: " + numTopic);
            for (int i = 0; i < numTopic; i++) {
                Element topic = (Element) nList.item(i);
                String topicID = topic.getAttribute("topic-id");
                System.out.println("Checking in folder: " + topicID);
                
                int check = 0;
                NodeList nSnippet = topic.getChildNodes();
                for(int j =0; j < nSnippet.getLength(); j++){
                    Node snippet =  nSnippet.item(j);
                    
                    if(snippet.getNodeType() != Node.TEXT_NODE){
                        
                        Element tmp = (Element) snippet;
                        String docID = tmp.getAttribute("doc-id");
                        
                        File file = new File("results3/" + topicID + "/" + docID + ".xml_summary");
                        if(file.exists() == false){
                            System.out.println(docID + " is not included in folder " + topicID);
                        }
                        //System.out.println(docID + " is include folder " + topicID + " |Check: " +file.exists());
                        check++;
                    }                   
                    
                }
                                
                System.out.println("Number of docs in this topic: " + check);
                System.out.println("---------------------------------");
                
            }












        } catch (Exception ex) {
            System.out.println("Reading file error");
        }
    }
}