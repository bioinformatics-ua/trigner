package pt.ua.tm.trigner.model.features.sentence;

import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/12/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SentenceTokensCounting implements FeatureExtractor {

    private String prefix;

    public SentenceTokensCounting(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {
        //To change body of implemented methods use File | Settings | File Templates.

        int numTokens = sentence.size();
        String cluster = getNumberOfTokensCluster(numTokens);

        for (Token token : sentence) {
            token.putFeature(prefix, cluster);
        }
    }

    private String getNumberOfTokensCluster(final int numTokens) {
        if (numTokens < 15) {
            return "Less15";
        } else if (numTokens >= 15 && numTokens < 20) {
            return "Between15_20";
        } else if (numTokens >= 20 && numTokens < 25) {
            return "Between20_25";
        } else if (numTokens >= 25 && numTokens < 30) {
            return "Between25_30";
        } else if (numTokens >= 30 && numTokens < 35) {
            return "Between30_35";
        } else if (numTokens >= 35 && numTokens < 40) {
            return "Between35_40";
        } else {
            return "More40";
        }
    }
}
