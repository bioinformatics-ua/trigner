package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.model.features.NGramsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DPEdgeNGrams implements FeatureExtractor {

    private int maxHops;
    private int n;
    private String prefix;

    public DPEdgeNGrams(final String prefix, final int maxHops, final int n) {
        this.prefix = prefix;
        this.maxHops = maxHops;
        this.n = n;
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
                if (edges.size() < maxHops) {
                    continue;
                }


                List<String> features = new ArrayList<>();

                for (LabeledEdge edge : edges) {
                    features.add(edge.getLabel().toString());
                }


                // Add n-grams
                for (String ngram : NGramsUtil.getNGrams(features, n, '_')) {
                    token1.putFeature(prefix, ngram);
                }
            }

        }
    }
}
