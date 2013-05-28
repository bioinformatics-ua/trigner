package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.DependencyTag;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class DPModifiers implements FeatureExtractor {

    private String prefix;
    public DPModifiers(final String prefix){
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {

        Graph graph = sentence.getDependencyGraph();

        for (Token token : sentence) {
            Set<LabeledEdge> edges = graph.edgesOf(token);
            for (LabeledEdge edge : edges) {
                DependencyTag tag = (DependencyTag) edge.getLabel();
                Token token2;

                if (tag.equals(DependencyTag.OBJ) || tag.equals(DependencyTag.SUB)) {
                    if (edge.getV2().equals(token)) {
                        continue;
                    }
                    token2 = (Token) edge.getV2();
                    token.putFeature(prefix, tag.toString() + "=" + token2.getFeature("LEMMA").get(0));
                } else if (tag.equals(DependencyTag.NMOD)) {
                    String key;
                    if (edge.getV1().equals(token)) { // NMOD OF
                        token2 = (Token) edge.getV2();
                        key = "NMOD_OF";
                    } else { // NMOD BY
                        token2 = (Token) edge.getV1();
                        key = "NMOD_BY";
                    }
                    token.putFeature(prefix, key + "=" + token2.getFeature("LEMMA").get(0));
                }
            }
        }
    }
}
