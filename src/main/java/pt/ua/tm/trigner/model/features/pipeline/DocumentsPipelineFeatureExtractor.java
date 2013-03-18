package pt.ua.tm.trigner.model.features.pipeline;

import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.features.corpus.pipeline.PipelineFeatureExtractor;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 23:49
 * To change this template use File | Settings | File Templates.
 */
public class DocumentsPipelineFeatureExtractor extends ArrayList<FeatureExtractor> implements PipelineFeatureExtractor {
    @Override
    public void run(Iterable documents) {
        Iterable<Corpus> iterable = documents;
        for (Corpus corpus : iterable) {
            for (Sentence sentence : corpus) {
                for (FeatureExtractor featureExtractor : this) {
                    featureExtractor.extract(sentence);
                }
            }
        }
    }
}
