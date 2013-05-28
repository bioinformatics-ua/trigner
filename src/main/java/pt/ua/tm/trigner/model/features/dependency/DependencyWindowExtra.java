package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DependencyWindowExtra implements FeatureExtractor {

    private int maxHops;
    private String prefix;

    public DependencyWindowExtra(final String prefix, final int maxHops) {
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

                Token previous = token1;
                for (LabeledEdge edge : edges) {

                    Token token3;
                    if (edge.getV1().equals(previous)) {
                        token3 = (Token) edge.getV2();
                    } else {
                        token3 = (Token) edge.getV1();
                    }


                    Collection<String> values = token3.getFeaturesMap().get("SP_EDGE_DISTANCE");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "SP_EDGE_DISTANCE=" + values.iterator().next());
                    }

                    values = token3.getFeaturesMap().get("SP_CHUNK_DISTANCE");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "SP_CHUNK_DISTANCE=" + values.iterator().next());
                    }

                    values = token3.getFeaturesMap().get("DPInOutDependencies_IN");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "DPInOutDependencies_IN=" + values.iterator().next());
                    }

                    values = token3.getFeaturesMap().get("DPInOutDependencies_OUT");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "DPInOutDependencies_OUT=" + values.iterator().next());
                    }

                    values = token3.getFeaturesMap().get("ConceptTags");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "ConceptTags=" + values.iterator().next());
                    }

                    values = token3.getFeaturesMap().get("DPModifiers");
                    if (!values.isEmpty()){
                        token1.putFeature(prefix, "DPModifiers=" + values.iterator().next());
                    }

                    previous = token3;
                }
            }

        }
    }
}
