/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author cngl
 */
public class getFinal {

    public static void main(String[] args) {
        String bdy_path = "final_bdy";
        String abs_path = "final_abs";
        //String weight_path = "training_data/";
        String testing_path = "testing_data_stoplist/";

        String fileName = "";
        String pathBdyFile = "";
        String pathAbsFile = "";
        String pathWeightsFile = "";
        String line = "";
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        BufferedWriter bw;
        String trainingData = "";
        File tmp;

        try {
            File folder = new File(testing_path);
            File[] listOfFiles = folder.listFiles();
            int numOfFiles = listOfFiles.length;

            for (int i = 0; i < numOfFiles; i++) {
                
                if(i%100 == 0){
                    System.out.println("Processed: " + i +  " files");
                }
                
                fileName = listOfFiles[i].getName();
                pathWeightsFile = "5classese/" + fileName;


                br = new BufferedReader(new FileReader(pathWeightsFile));
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                    sb.append(System.getProperty("line.separator"));
                }

                //write to new file
                trainingData = sb.toString();//.replaceAll("\\<.*?>", "");

                sb = new StringBuilder();

                //print2file#
                try {
                    tmp = new File("stoplist_testing/" + fileName);
                    bw = new BufferedWriter(new FileWriter(tmp, true));
                    bw.write(trainingData);

                    bw.close();

                } catch (Exception e) {
                    System.out.println("Loi gi file: " + fileName);
                }


            }

        } catch (Exception ex) {
            System.out.println("Reading file error!" + fileName);
        }
    }
}
