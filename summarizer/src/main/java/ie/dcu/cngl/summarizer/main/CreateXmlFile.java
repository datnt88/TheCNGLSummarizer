/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileReader;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.apache.commons.lang.StringEscapeUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Dat Tien Nguyen
 */
public class CreateXmlFile {

    public static void main(String argv[]) {

        String[] index = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
            "29", "30", "31", "32", "33", "34", "35"};

        String pathOfFodel = "";

        try {


            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            //

            Document doc = docBuilder.parse(new File("inex-2012-sr-referencerun.xml"));
            //doc.setXmlStandalone(false);
            NodeList nList;
            int numNodes;
            Element snipNode;

            //Adding run information
            nList = doc.getElementsByTagName("inex-snippet-submission");
            Element inex = (Element) nList.item(0);
            inex.setAttribute("participant-id", "247");
            inex.setAttribute("run-id", "TheCNGL_DCU_SnippetTrack_2012_SRun03");


            //Adding description
            nList = doc.getElementsByTagName("description");
            Element descr = (Element) nList.item(0);
            descr.replaceChild(doc.createTextNode("Bias factor"
                    + "to score sentences containing query terms more highly."
                    + "sentence score = (number of query terms in the sentence)^2/number of terms in query."
                    + "Summary of text is some the highest score sentences."), descr.getFirstChild());


            nList = doc.getElementsByTagName("snippet");
            numNodes = nList.getLength();

            String snippetText = "";
            String line = "";
            BufferedWriter bw;
            BufferedReader br;
            StringBuilder sb = new StringBuilder();
            System.out.println(numNodes);

            for (int i = 0; i < numNodes; i++) {
                snipNode = (Element) nList.item(i);
                String docId = snipNode.getAttribute("doc-id");

                //Reading file and
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream("run4_8Features/" + docId + ".xml_summary"), "UTF8"));
                    while ((line = br.readLine()) != null) {
                        sb.append(line.trim()).append(" ");
                    }
                    //resize summary to 180 character
                    if (sb.length() > 180) {
                        snippetText = sb.toString().substring(0, 180);
                    } else {
                        snippetText = sb.toString();
                    }
                    sb = new StringBuilder();

                    snipNode.replaceChild(doc.createTextNode(snippetText), snipNode.getFirstChild());
                } catch (Exception e) {
                    System.out.println(docId + ".xml");
                }
                //System.out.println("Process documnet Id: " + docId);
            }

            //write to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            //transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            //transformer.setOutputProperty(OutputKeys.STANDALONE, "");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "inex-snippet-submission.dtd");
            //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"inex-snippet-submission.dtd");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("TheCNGL_DCU_SnippetTrack2012_Run04.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (Exception ex) {
            System.err.println("Error message is: " + ex.getMessage());
        }


        /*
        Document doc = docBuilder.newDocument();
        //doc.setXmlStandalone(false);
        //doc.setXmlVersion("1.0");
        Element rootElement = doc.createElement("inex-snippet-submission");
        doc.appendChild(rootElement);

        Attr attr = doc.createAttribute("participant-id");
        attr.setValue("247");
        rootElement.setAttributeNode(attr);

        attr = doc.createAttribute("run-id");
        attr.setValue("QUT_Snippet_Run_01");
        rootElement.setAttributeNode(attr);

        Element descrip = doc.createElement("description");
        rootElement.appendChild(descrip);
        descrip.appendChild(doc.createTextNode("A description of method"));


        Element tmp;
        Element snippet_tmp;
        int numFiles = 0;
        File folder;
        File[] listOfFiles;
        String sum_filename = "";
        String filename = "";
        BufferedWriter bw;
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        String snippet = "";
        String line = "";
        String pathOfFile = "";

        String docId = "";

        for (int i = 0; i < 35; i++) {
        tmp = doc.createElement("topic");
        rootElement.appendChild(tmp);
        attr = doc.createAttribute("topic-id");
        attr.setValue("20120" + index[i]);
        tmp.setAttributeNode(attr);

        folder = new File("results1/20120" + index[i]);
        listOfFiles = folder.listFiles();
        numFiles = listOfFiles.length;

        for (int count = 0; count < numFiles; count++) {
        sum_filename = listOfFiles[count].getName();
        pathOfFile = listOfFiles[count].getPath();
        filename = sum_filename.split("_")[0];
        docId = filename.substring(0, filename.length() - 4);

        if (filename.equals(".directory") == false) {

        //System.out.println(filename);

        snippet_tmp = doc.createElement("snippet");
        tmp.appendChild(snippet_tmp);
        attr = doc.createAttribute("doc-id");
        attr.setValue(docId);

        snippet_tmp.setAttributeNode(attr);
        attr = doc.createAttribute("rsv");
        attr.setValue("" + (count + 1));
        snippet_tmp.setAttributeNode(attr);

        //Reading file and
        br = new BufferedReader(new FileReader(pathOfFile));
        while ((line = br.readLine()) != null) {
        sb.append(line.trim()).append(" ");
        }

        if (sb.length() > 180) {
        snippet = sb.toString().substring(0, 176) + "...";
        } else {
        snippet = sb.toString();
        }
        sb = new StringBuilder();

        snippet_tmp.appendChild(doc.createTextNode(snippet));
        }
        }
        }

        //write to file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        //transformer.setOutputProperty(OutputKeys.ENCODING, "ISO8859-1");

        //transformer.setOutputProperty(OutputKeys.STANDALONE, "");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "inex-snippet-submission.dtd");


        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("TheCNGL_2012_Run01.xml"));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);

        System.out.println("File saved!");
         * 
        }
        catch (Exception ex) {
        System.err.println("Error message is: " + ex.getMessage());
        }*/
    }
}
