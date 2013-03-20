package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.model.features.FeatureType;
import pt.ua.tm.trigner.model.features.TokenFeatureUtil;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DependencyWindow implements FeatureExtractor {

    private int maxHops;
    private String prefix;
    private FeatureType[] features;

    public DependencyWindow(final String prefix, final FeatureType[] features, final int maxHops) {
        this.prefix = prefix;
        this.maxHops = maxHops;
        this.features = features;
    }

    @Override
    public void extract(Sentence sentence) {
        Graph graph = sentence.getDependencyGraph();
        for (Token token1 : sentence) {
            BellmanFordShortestPath<Token, LabeledEdge> bellman =
                    new BellmanFordShortestPath<Token, LabeledEdge>(graph, token1, maxHops);

            for (Token token2 : sentence) {
                if (token1.equals(token2)) {
                    continue;
                }
                List<LabeledEdge> edges = bellman.getPathEdgeList(token2);
                if (edges == null) {
                    continue;
                }

                Token previous = token1;
                for (LabeledEdge edge : edges) {

                    Token token3;
                    if (edge.getV1().equals(previous)) {
                        token3 = (Token) edge.getV2();
                    } else {
                        token3 = (Token) edge.getV1();
                    }

                    for (FeatureType feature : features) {
                        token1.putFeature(prefix, feature + "=" + TokenFeatureUtil.getFeature(token3, feature));
                    }

                    previous = token3;
                }
            }

        }
    }
}
