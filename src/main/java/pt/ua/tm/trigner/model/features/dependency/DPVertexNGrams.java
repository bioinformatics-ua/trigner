package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.VertexFeatureType;
import pt.ua.tm.trigner.util.NGramsUtil;
import pt.ua.tm.trigner.util.TokenFeatureUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DPVertexNGrams implements FeatureExtractor {

    private int maxHops;
    private int n;
    private String prefix;
    private Types.VertexFeatureType feature;

    public DPVertexNGrams(final String prefix, final Types.VertexFeatureType feature, final int maxHops, final int n) {
        this.prefix = prefix;
        this.maxHops = maxHops;
        this.n = n;
        this.feature = feature;
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

                features.add(TokenFeatureUtil.getFeature(token1, feature));

                Token previous = token1;
                for (LabeledEdge edge : edges) {

                    Token token3;
                    if (edge.getV1().equals(previous)) {
                        token3 = (Token) edge.getV2();
                    } else {
                        token3 = (Token) edge.getV1();
                    }

                    features.add(TokenFeatureUtil.getFeature(token3, feature));

                    previous = token3;
                }


                // Add n-grams
//                Collections.sort(features);
                for (String ngram : NGramsUtil.getNGrams(features, n, '_')) {
                    token1.putFeature(prefix, ngram);
                }
            }

        }
    }
}
