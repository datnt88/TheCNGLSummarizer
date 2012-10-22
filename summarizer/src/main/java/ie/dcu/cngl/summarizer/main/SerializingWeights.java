package ie.dcu.cngl.summarizer.main;

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
import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.summarizer.feature.SummaryAnalyzer;
import ie.dcu.cngl.tokenizer.TokenizerUtils;
import java.io.StringReader;
import java.text.DecimalFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;

public class SerializingWeights {

    /**
     * @param args
     * @throws IOException
     */
    public static double numberOfTerms(ArrayList<TokenInfo> sentence) throws IOException {
        SummaryAnalyzer analyzer = new SummaryAnalyzer();
        
        double numTerms = 0;
        StringReader reader = new StringReader(TokenizerUtils.recombineTokens1d(sentence));
        TokenStream tokenStream = analyzer.tokenStream(null, reader);

        try {
            while (tokenStream.incrementToken()) {
                numTerms++;
            }
        } catch (IOException e) {
        }
        return numTerms;
    }

    public static void main(String[] args) throws IOException {

        DecimalFormat df = new DecimalFormat("0.0000");

        Tokenizer tokenizer = Tokenizer.getInstance();

        String text = FileUtils.readFileToString(new File("26110_stand"), "UTF-8");
        String title_text = "List of fictional robots and androids";

        Structurer structurer = new Structurer();
        PageStructure structure = structurer.getStructure(text);

        Weighter weighter = new Weighter();
        ArrayList<Double[]> weights = new ArrayList<Double[]>();
        weighter.setStructure(structure);
        weighter.setTitle(StringUtils.isNotEmpty(title_text) ? tokenizer.tokenize(title_text) : null);
        weighter.calculateWeights(weights);


        int numSense = structure.getNumSentences();

        System.out.println("weight size is: " + weights.size());
        System.out.println("the number of sentences is: " + numSense);
        System.out.println();

        ArrayList<TokenInfo> sentence = new ArrayList<TokenInfo>();

        for (int nSense = 0; nSense < numSense; nSense++) {

            sentence = structure.getSentenceTokens(nSense);
            for (int i = 0; i < sentence.size(); i++) {
                System.out.print(sentence.get(i).getValue().toString() + " ");
            }

            System.out.println();
            
            System.err.println("The number of Term: " + numberOfTerms(sentence));
            System.err.println("The number of Token: " + sentence.size());
            
            Double[] tmp = weights.get(14);
            System.out.println("feature 13 = " + tmp[nSense].toString());
        }

        /*
        
         for (int i = 0; i < weights.size(); i++) {
         Double[] tmp = weights.get(i);
         for (int j = 0; j < tmp.length; j++) {
         System.out.print(tmp[j] + ",");
         }
         System.out.println();
         }
         * */

        try {
            FileOutputStream fileOut = new FileOutputStream(new File("test"));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(weights);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
