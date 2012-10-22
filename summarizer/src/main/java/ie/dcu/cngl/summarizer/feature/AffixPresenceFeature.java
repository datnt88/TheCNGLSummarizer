package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.summarizer.feature.Affix.AffixType;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.util.ArrayList;

/**
 * Calculates the number of supplied affixes that occur in each sentence. </br>
 * sentence score = number of affixes present/number of terms
 *
 * @author Shane
 *
 */
public class AffixPresenceFeature extends TermCheckingFeature {

    private ArrayList<Affix> affixes;
    private int extraLettersForMatch;		//Extra letters at either end of affix necessary for match

    /**
     * Constructs an AffixPresenceFeature with a default of 3 extra letters
     * needed for a match.
     *
     * @throws Exception
     */
    public AffixPresenceFeature() throws Exception {
        this.extraLettersForMatch = SummarizerUtils.extraLettersForMatch == -1 ? 3 : SummarizerUtils.extraLettersForMatch;		//Default
        this.affixes = new ArrayList<Affix>();
        for (String line : terms) {
            line = line.toLowerCase();
            Affix affix = new Affix(line);
            affixes.add(affix);
        }
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.affixPresenceMultiplier;
    }

    /**
     * Sets the number of letters necessary at the appropriate ends of an affix
     * to count as a match.
     *
     * @param extraLettersForMatch
     */
    public void setExtraLettersForMatch(int extraLettersForMatch) {
        this.extraLettersForMatch = extraLettersForMatch;
    }

    public int getExtraLettersForMatch() {
        return extraLettersForMatch;
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        int sentenceNum = 0;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                double numOccurences = 0;
                double numTerms = numberOfTerms(sentence);

                if (numTerms > 0.0) {
                    for (Affix affix : affixes) {
                        if (affix.getAffix().length() > 2) {
                            numOccurences += getNumAffixOccurences(affix, sentence);
                        }
                    }
                    
                    weights[sentenceNum++] = numOccurences / numTerms;
                    
                                     
                } else {
                    weights[sentenceNum++] = 0.0000;
                }
            }
        }
        return weights;
    }

    private Double getNumAffixOccurences(Affix affix, ArrayList<TokenInfo> sentence) {
        final String affixStr = affix.getAffix();
        final int affixLength = affixStr.length();
        double numOccurences = 0;

        for (TokenInfo token : sentence) {
            String tokenStr = token.getValue();
            int tokenLength = token.getLength();
            AffixType type = affix.getType();
            int minimumLength = type == AffixType.INFIX ? affixLength + 2 * extraLettersForMatch : affixLength + extraLettersForMatch;
            if (tokenLength > minimumLength) {
                switch (affix.getType()) {
                    case PREFIX:
                        if (tokenStr.startsWith(affixStr)) {
                            numOccurences++;
                        }
                        break;
                    case SUFFIX:
                        if (tokenStr.endsWith(affixStr)) {
                            numOccurences++;
                        }
                        break;
                    case INFIX:
                        if (tokenStr.indexOf(affixStr) != -1) {
                            numOccurences++;
                        }
                        break;
                }
            }
        }

        return numOccurences;
    }

    @Override
    public String getTermsFileName() {
        return SummarizerUtils.affixesFile;
    }
}
