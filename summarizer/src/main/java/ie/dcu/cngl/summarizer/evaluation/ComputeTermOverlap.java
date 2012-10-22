/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.evaluation;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.summarizer.feature.MyTokenStream;
import ie.dcu.cngl.summarizer.feature.NumericTokenFilter;
import ie.dcu.cngl.summarizer.feature.PunctuationTokenFilter;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.util.ArrayList;

import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.Structurer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Attribute;

/**
 * Evaluate each sentence in body that included in abstract (or nearest meaning
 * to one of the sentences in abstract) using term overlap technique
 *
 * @author Dat Tien Nguyen
 */
public class ComputeTermOverlap {

    public static String[] filterComments(String[] input) {
        List<String> stopwords = new ArrayList<String>();
        for (String stopword : input) {
            if (!stopword.startsWith("#")) {
                stopwords.add(stopword);
            }
        }
        return stopwords.toArray(new String[0]);
    }

    public static ArrayList<TokenInfo> ignoreStopList(ArrayList<TokenInfo> sentence) throws Exception {

        int numTerms = 0;
        ArrayList<TokenInfo> results = new ArrayList<TokenInfo>();

        Set<Object> stopset;
        String[] stopwords = filterComments(StringUtils.split(FileUtils.readFileToString(new File(SummarizerUtils.stopwords), "UTF-8")));
        stopset = StopFilter.makeStopSet(Version.LUCENE_36, stopwords, true);


        Iterator<TokenInfo> iter = sentence.iterator();

        TokenStream tokenStream =
                new StopFilter(Version.LUCENE_36,
                new StandardFilter(Version.LUCENE_36,
                new NumericTokenFilter(
                new PunctuationTokenFilter(
                new LowerCaseFilter(Version.LUCENE_36, new MyTokenStream(iter))))), stopset);

        OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        TermAttribute termAttribute = tokenStream.getAttribute(TermAttribute.class);

        /*
         while (tokenStream.incrementToken()) {
         int startOffset = offsetAttribute.startOffset();
         int endOffset = offsetAttribute.endOffset();
         //System.out.print(termAttribute.term().toString() + " , ");
         //results.add(new TokenInfo(termAttribute.term()));
         //results.add(new TokenInfo("investigation"));

         }
       
       
         CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

         while (tokenStream.incrementToken()) {
         int startOffset = offsetAttribute.startOffset();
         int endOffset = offsetAttribute.endOffset();
         String term = charTermAttribute.toString();
         System.out.print(charTermAttribute.toString() + " + ");
         results.add(new TokenInfo(term));
         }
         */


        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {

            results.add(new TokenInfo(cattr.toString()));
        }


        //System.out.println();
        return results;
    }

    public static int[] compare2sentences(String pathBdyFile, String pathAbsFile) throws IOException {

        String abs_text = FileUtils.readFileToString(new File(pathAbsFile), "UTF-8");
        String bdy_text = FileUtils.readFileToString(new File(pathBdyFile), "UTF-8");

        Structurer abs_structurer = new Structurer();
        Structurer bdy_structurer = new Structurer();
        PageStructure abs_struct;
        PageStructure bdy_struct;
        //Loading abs and bdy
        abs_struct = abs_structurer.getStructure(abs_text);
        bdy_struct = bdy_structurer.getStructure(bdy_text);



        int numOfTerm_bdy_sens = 0;
        int numOfTerm_abs_sens = 0;


        int[] result_ = new int[bdy_struct.getNumSentences()];
        //System.out.println(bdy_struct.getNumSentences());
        int sens_index = 0;
        ArrayList<TokenInfo> bdy_sens = new ArrayList<TokenInfo>();
        ArrayList<TokenInfo> abs_sens = new ArrayList<TokenInfo>();


        for (ArrayList<ArrayList<TokenInfo>> bdy_paragraph : bdy_struct.getStructure()) {
            for (ArrayList<TokenInfo> bdy_sens_tmp : bdy_paragraph) {
                //Compare to each sentences in abstract
                result_[sens_index] = 0;
                //Check in stoplist
                //compute number of term include stoplist
                try {
                    bdy_sens = ignoreStopList(bdy_sens_tmp);
                } catch (Exception ex) {
                }



                ArrayList<Double> check_ = new ArrayList<Double>();
                for (ArrayList<ArrayList<TokenInfo>> abs_paragraph : abs_struct.getStructure()) {
                    for (ArrayList<TokenInfo> abs_sens_tmp : abs_paragraph) {
                        //compute term in abs_sens, include stoplist
                        try {
                            abs_sens = ignoreStopList(abs_sens_tmp);
                        } catch (Exception ex) {
                        }
                        //Compute termoverlap and return for each sentence 
                        numOfTerm_bdy_sens = bdy_sens.size();
                        numOfTerm_abs_sens = abs_sens.size();

                        // if (numOfTerm_bdy_sens > 0 & numOfTerm_abs_sens > 0) {

                        int count = 0;

                        for (int abs_term_count = 0; abs_term_count < numOfTerm_abs_sens; abs_term_count++) {
                            for (int bdy_term_count = 0; bdy_term_count < numOfTerm_bdy_sens; bdy_term_count++) {
                                if (abs_sens.get(abs_term_count).toString().equals(bdy_sens.get(bdy_term_count).toString())) {
                                    count++;
                                }

                            }
                        }

                        double check;
                        if (numOfTerm_abs_sens > numOfTerm_bdy_sens) {
                            check = (double) count / numOfTerm_bdy_sens;
                            // System.out.println("Count = " + count);
                            //System.out.println("So tu trong abs: " + numOfTerm_abs_sens);

                        } else {
                            check = (double) count / numOfTerm_abs_sens;


                        }
                        check_.add(check);
                        //System.out.println("Score = " + check);    
                        // }

                    }

                } //end for abs

                double tmp_check = 0;

                for (int k = 0; k < check_.size(); k++) {
                    if (check_.get(k) > tmp_check) {
                        tmp_check = check_.get(k);
                    }
                }


                if (tmp_check >= 0.8) {
                    result_[sens_index] = 4;
                } else if (tmp_check >= 0.6) {
                    result_[sens_index] = 3;
                } else if (tmp_check >= 0.4) {
                    result_[sens_index] = 2;
                } else if (tmp_check >= 0.2) {
                    result_[sens_index] = 1;
                } else {
                    result_[sens_index] = 0;
                }



                /*
                 
                 if (tmp_check >= 0.6) {
                 result_[sens_index] = 2;
                 } else if (tmp_check >= 0.3) {
                 result_[sens_index] = 1;
                 } else {
                 result_[sens_index] = 0;
                 }
                
                 if (tmp_check >= 0.75) {
                 result_[sens_index] = 3;
                 } else if (tmp_check >= 0.5) {
                 result_[sens_index] = 2;
                 } else if (tmp_check >= 0.25) {
                 result_[sens_index] = 1;
                 } else {
                 result_[sens_index] = 0;
                 }
                
                 if (tmp_check >= 0.8) {
                 result_[sens_index] = 4;
                 } else if (tmp_check >= 0.6) {
                 result_[sens_index] = 3;
                 } else if (tmp_check >= 0.4) {
                 result_[sens_index] = 2;
                 } else if (tmp_check >= 0.2) {
                 result_[sens_index] = 1;
                 } else {
                 result_[sens_index] = 0;
                 }

                 /*
                 
                 //System.out.println("ok " + tmp_check);

                 /*
                 if (tmp_check >= 0.8) {
                 result_[sens_index] = 1;
                 } else {
                 result_[sens_index] = 0;
                 }
                 
                 

                 
                 
                 */
                sens_index += 1;


            }
        }

        return result_;
    }

    public static void main(String[] args) {

        String bdy_path = "final_bdy";
        String abs_path = "final_abs";
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
            File folder = new File(bdy_path);
            File[] listOfFiles = folder.listFiles();
            int numOfFiles = listOfFiles.length;

            for (int i = 0; i < numOfFiles; i++) {

                if (i % 100 == 0) {
                    System.out.println("Processed: " + i + " files");
                }


                fileName = listOfFiles[i].getName();
                pathBdyFile = listOfFiles[i].getPath();
                pathAbsFile = "final_abs/" + fileName;
                pathWeightsFile = "4classese/" + fileName + "_weights";

                //Compute Term overlap between each sentences in Bdy to Abs
                int[] score;
                score = compare2sentences(pathBdyFile, pathAbsFile);


                int sens_index = 0;

                br = new BufferedReader(new FileReader(pathWeightsFile));
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim()).append(",");
                    sb.append(score[sens_index++]);
                    sb.append(System.getProperty("line.separator"));
                }

                //write to new file
                trainingData = sb.toString();//.replaceAll("\\<.*?>", "");

                sb = new StringBuilder();

                //print2file#
                try {
                    tmp = new File("5classese/" + fileName + "_weights");
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
    /*
     public static void main(String[] args) throws Exception {
     String abs_text = FileUtils.readFileToString(new File("bdy_test"), "UTF-8");

     Structurer abs_structurer = new Structurer();
     PageStructure abs_struct;

     //Loading abs and bdy
     abs_struct = abs_structurer.getStructure(abs_text);
     ArrayList<TokenInfo> tmp = new ArrayList<TokenInfo>();

     for (ArrayList<ArrayList<TokenInfo>> abs_paragraph : abs_struct.getStructure()) {
     for (ArrayList<TokenInfo> abs_sens : abs_paragraph) {
                               
     System.out.println("The number term in original is: " + abs_sens.size());
     for (int i = 0; i < abs_sens.size(); i++) {
     System.out.print(abs_sens.get(i).getValue() + " , ");
     }
                
     tmp = ignoreStopList(abs_sens);
     System.out.println("The number term in tmp is: " + tmp.size());
     for (int i = 0; i < tmp.size(); i++) {
     System.out.print(tmp.get(i).getValue() + " , ");
     }
                
     System.out.println();
                
     System.out.println("-----------------------------------");

     }
     }


     }
     */
}
