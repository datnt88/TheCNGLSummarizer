package ie.dcu.cngl.summarizer;

import ie.dcu.cngl.summarizer.feature.AffixPresenceFeature;
import ie.dcu.cngl.summarizer.feature.BasicWordsFeature;
import ie.dcu.cngl.summarizer.feature.ClusterKeywordFeature;

import ie.dcu.cngl.summarizer.feature.CuePhraseFeature;

import ie.dcu.cngl.summarizer.feature.Feature;
import ie.dcu.cngl.summarizer.feature.GlobalBushyFeature;
import ie.dcu.cngl.summarizer.feature.ImportantTermsFeature;
import ie.dcu.cngl.summarizer.feature.NamedEntityFeature;
import ie.dcu.cngl.summarizer.feature.PunctuationFeature;
import ie.dcu.cngl.summarizer.feature.QueryBiasFeature;
import ie.dcu.cngl.summarizer.feature.SectionImportanceFeature;
import ie.dcu.cngl.summarizer.feature.ShortSentenceFeature;
import ie.dcu.cngl.summarizer.feature.SkimmingFeature;
import ie.dcu.cngl.summarizer.feature.TFISFFeature;
import ie.dcu.cngl.summarizer.feature.TitleTermFeature;

import ie.dcu.cngl.summarizer.feature.AmbiguationFeature;
import ie.dcu.cngl.summarizer.feature.DatetimeTermFeature;
import ie.dcu.cngl.summarizer.feature.ConceptHierarchyFeature;
import ie.dcu.cngl.summarizer.feature.QuerySynsetFeature;
import ie.dcu.cngl.summarizer.feature.LongSentenceFeature;


import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A Weighter executes all summarization features chosen. Several features are
 * provided by default.
 *
 * @author Shane
 *
 */
public class Weighter implements IWeighter {

    private PageStructure structure;
    ArrayList<Feature> features;
    private ArrayList<TokenInfo> title;
    private ArrayList<TokenInfo> query;
    private ArrayList<TokenInfo> phraseQuery;

    /**
     * Creates new weighter. Features can be added before using
     * calculateWeights.
     */
    public Weighter() {
        this.features = new ArrayList<Feature>();
    }

    public void calculateWeights(ArrayList<Double[]> weights) {
        addFeatures();
        for (Feature feature : features) {
            if (feature.getMultiplier() != 0) {
                feature.setStructure(structure);
                weights.add(feature.getWeights());
            }
        }
    }

    @Override
    public void addFeature(Feature feature) {
        features.add(feature);
    }

    private void addFeatures() {
        /*
         try {
         SkimmingFeature skimFeat = new SkimmingFeature();
         features.add(skimFeat);
         } catch (IOException e) {
         System.err.println("Skimming feature failed.");
         e.printStackTrace();
         }
         */
        
        try {
            features.add(new TFISFFeature());
        } catch (IOException e) {
            System.err.println("TS-ISF feature failed.");
            e.printStackTrace();
        }

        try {
            features.add(new NamedEntityFeature());
        } catch (IOException e) {
            System.err.println("Named entity feature failed.");
            e.printStackTrace();
        }


        if (title != null) {
            try {
                features.add(new TitleTermFeature(title));
            } catch (IOException e) {
                System.err.println("Title terms feature failed.");
                e.printStackTrace();
            }
        }

        try {
            features.add(new CuePhraseFeature());
        } catch (IOException e) {
            System.err.println("Cue phrases feature failed.");
            e.printStackTrace();
        }


        try {
            features.add(new ShortSentenceFeature());
        } catch (IOException e) {
            System.err.println("Short sentence feature failed.");
            e.printStackTrace();
        }
        /*
         try {
         features.add(new LongSentenceFeature());
         } catch (IOException e) {
         System.err.println("Long sentence feature failed.");
         e.printStackTrace();
         }
         */

        try {
            features.add(new GlobalBushyFeature());
        } catch (Exception e) {
            System.err.println("Global busy feature failed.");
            e.printStackTrace();
        }
        /*

         try {
         features.add(new PunctuationFeature());
         } catch (Exception e) {
         System.err.println("Punctuation feature failed.");
         e.printStackTrace();
         }

         try {
         features.add(new AffixPresenceFeature());
         } catch (Exception e) {
         System.err.println("Affix presence feature failed.");
         e.printStackTrace();
         }

         try {
         features.add(new BasicWordsFeature());
         } catch (Exception e) {
         System.err.println("Basic words feature failed.");
         e.printStackTrace();
         }

         try {
         features.add(new SectionImportanceFeature());
         } catch (Exception e) {
         System.err.println("Section importance feature failed.");
         e.printStackTrace();
         }

         try {
         features.add(new ImportantTermsFeature());
         } catch (Exception e) {
         System.err.println("Important terms feature failed.");
         e.printStackTrace();
         }
         */

        try {
            features.add(new ClusterKeywordFeature());
        } catch (Exception e) {
            System.err.println("Cluster keyword feature failed.");
            e.printStackTrace();
        }
        /*
         try {
         features.add(new AmbiguationFeature());
         } catch (Exception e) {
         System.err.println("Ambiguation feature failed.");
         e.printStackTrace();
         }

         try {
         features.add(new DatetimeTermFeature());
         } catch (Exception e) {
         System.err.println("Datetime term  feature failed.");
         e.printStackTrace();
         }
         */
       if (query != null) {
            try {
                features.add(new QueryBiasFeature(query, phraseQuery));

            } catch (IOException e) {
                System.err.println("Query bias feature failed.");
                e.printStackTrace();
            }
        }

        /*
         try {
         features.add(new ConceptHierarchyFeature());
         } catch (Exception e) {
         System.err.println("Concept Hierarchy feature failed.");
         e.printStackTrace();
         }        
         *
         if (phraseQuery != null) {
         try {
         features.add(new QuerySynsetFeature(query, phraseQuery));
         } catch (Exception e) {
         System.err.println("Query Synset feature failed.");
         e.printStackTrace();
         }
         }
         **/

    }

    @Override
    public void setStructure(PageStructure structure) {
        this.structure = structure;
    }

    public void setTitle(ArrayList<TokenInfo> titleTokens) {
        this.title = titleTokens;
    }

    public void setQuery(ArrayList<TokenInfo> queryTokens) {
        this.query = queryTokens;
    }

    public void setPhraseQuery(ArrayList<TokenInfo> phraseQueryTokens) {
        this.phraseQuery = phraseQueryTokens;
    }
}
