package ie.dcu.cngl.summarizer.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import ie.dcu.cngl.summarizer.Weighter;
import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.Structurer;
import ie.dcu.cngl.tokenizer.Tokenizer;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.text.*;
import org.apache.commons.lang.StringUtils;

public class Weights2Files {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        DecimalFormat df = new DecimalFormat("0.0000");

        String path = "final_bdy";
        String file_ = "";
        String pathOfDocFile = "";
        String pathOfTitleFile = "";

        int count_file = 0;
        StringBuilder sb = new StringBuilder();

        BufferedWriter bw;
        BufferedReader br;
        File weight2file;
        int num_feature = 0;
        int num_sentences = 0;
        
        Tokenizer tokenizer = Tokenizer.getInstance();

        try {
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            int numOfFiles = listOfFiles.length;

            for (int i = 0; i < numOfFiles; i++) {
                
                if(i%50 == 0){
                    System.out.println("Processed: " + i +  " files");
                }

                file_ = listOfFiles[i].getName();
                pathOfDocFile = listOfFiles[i].getPath();
                pathOfTitleFile = "final_title/" + file_;

                String text = FileUtils.readFileToString(new File(pathOfDocFile), "UTF-8");
                String title_text = FileUtils.readFileToString(new File(pathOfTitleFile), "UTF-8");
                //System.out.println(title_text);
                
                Structurer structurer = new Structurer();
                PageStructure structure = structurer.getStructure(text);

                Weighter weighter = new Weighter();
                ArrayList<Double[]> weights = new ArrayList<Double[]>();
                weighter.setStructure(structure);
                //Set title
                weighter.setTitle(StringUtils.isNotEmpty(title_text) ? tokenizer.tokenize(title_text) : null);
                
                //Set query
                //weighter.setQuery(StringUtils.isNotEmpty(title_text) ? tokenizer.tokenize(title_text) : null);

                weighter.calculateWeights(weights);

                // wite to string builder
                num_feature = weights.size();
                num_sentences = structure.getNumSentences();

                //System.out.println("The number of sentences in this bdy: " + num_sentences);
                //System.out.println("The number of feature: " + num_feature);


                for (int k = 0; k < num_sentences; k++) {
                    for (int count_ = 0; count_ < num_feature ; count_++) {
                        Double[] tmp = weights.get(count_);
                        sb.append(df.format(tmp[k]) + ",");
                    }
                    sb.append("\n");
                }

                try {
                    weight2file = new File("bdy_weights/" + file_ + "_weights");
                    bw = new BufferedWriter(new FileWriter(weight2file, true));
                    bw.write(sb.toString());
                    //System.out.println("wrote in: " + file_ + " Order is: " + count_file++);
                    bw.close();

                } catch (Exception e) {
                    System.out.println("writing file error: " + file_);
                }

                sb = new StringBuilder();

                /*
                 // write 2 cache
                 try {
                 FileOutputStream fileOut = new FileOutputStream(new File("test"));
                 ObjectOutputStream out = new ObjectOutputStream(fileOut);
                 out.writeObject(weights);
                 out.close();
                 fileOut.close();
                 } catch (IOException e) {
                 e.printStackTrace();
                 System.out.println("Cache error: " + file_);
                 }
                 * */

            }
        } catch (Exception ex) {
            System.out.println("Reading file error: " + file_);
        }

        return;
    }
}
