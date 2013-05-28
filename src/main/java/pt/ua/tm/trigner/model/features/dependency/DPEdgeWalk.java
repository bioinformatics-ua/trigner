package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DPEdgeWalk implements FeatureExtractor {

    private String prefix;
    private int maxHops;


    public DPEdgeWalk(final String prefix, final int maxHops) {
        this.prefix = prefix;
        this.maxHops = maxHops;
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

                StringBuilder sb = new StringBuilder();
                for (LabeledEdge edge : edges) {
                    sb.append(edge.getLabel());
                    sb.append("-");
                }

                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                token1.putFeature(prefix, sb.toString());
            }

        }
    }
}
