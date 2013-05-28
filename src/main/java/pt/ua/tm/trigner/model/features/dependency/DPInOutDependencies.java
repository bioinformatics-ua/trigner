package pt.ua.tm.trigner.model.features.dependency;

import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 12/03/13
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class DPInOutDependencies implements FeatureExtractor {

    private int maxHops;
    private String prefix;

    public DPInOutDependencies(final String prefix, final int maxHops) {
        this.prefix = prefix;
        this.maxHops = maxHops;
    }

    @Override
    public void extract(Sentence sentence) {
        Graph graph = sentence.getDependencyGraph();
        for (Token token1 : sentence) {
            BellmanFordShortestPath<Token, LabeledEdge> bellman =
                    new BellmanFordShortestPath<Token, LabeledEdge>(graph, token1, maxHops);

            boolean inDependencies = false, outDependencies = false;
            for (Token token2 : sentence) {
                if (token1.equals(token2)) {
                    continue;
                }
                List<LabeledEdge> edges = bellman.getPathEdgeList(token2);
                if (edges == null) {
                    continue;
                }


                // In dependencies
                String newPrefix = prefix + "_IN";
                List<LabeledEdge> inEdges = getInEdges(token1, edges);
                for (LabeledEdge inEdge : inEdges) {
                    Token token = (Token) inEdge.getV1();

                    String label = inEdge.getLabel().toString();
                    String pos = token.getFeature("POS").get(0);
                    String lemma = token.getFeature("LEMMA").get(0);
                    String chunk = sentence.getChunks().getTokenChunk(token).getTag().toString();
//                    String text = token.getText().toLowerCase();

                    token1.putFeature(newPrefix, "Label=" + label);

                    token1.putFeature(newPrefix, "POS=" + pos);
                    token1.putFeature(newPrefix, label + "_" + pos);

//                    token1.putFeature(newPrefix, text);
//                    token1.putFeature(newPrefix, label + "_" + text);

                    token1.putFeature(newPrefix, "Lemma=" + lemma);
                    token1.putFeature(newPrefix, label + "_" + lemma);

                    token1.putFeature(newPrefix, "Chunk=" + chunk);
                    token1.putFeature(newPrefix, label + "_" + chunk);

                    inDependencies = true;
                }

                // Out dependencies
                newPrefix = prefix + "_OUT";
                List<LabeledEdge> outEdges = getOutEdges(token1, edges);
                for (LabeledEdge inEdge : outEdges) {
                    Token token = (Token) inEdge.getV2();

                    String label = inEdge.getLabel().toString();
                    String pos = token.getFeature("POS").get(0);
                    String lemma = token.getFeature("LEMMA").get(0);
                    String chunk = sentence.getChunks().getTokenChunk(token).getTag().toString();
//                    String text = token.getText().toLowerCase();

                    token1.putFeature(newPrefix, "Label=" + label);

                    token1.putFeature(newPrefix, "POS=" + pos);
                    token1.putFeature(newPrefix, label + "_" + pos);

//                    token1.putFeature(newPrefix, text);
//                    token1.putFeature(newPrefix, label + "_" + text);

                    token1.putFeature(newPrefix, "Lemma=" + lemma);
                    token1.putFeature(newPrefix, label + "_" + lemma);

                    token1.putFeature(newPrefix, "Chunk=" + chunk);
                    token1.putFeature(newPrefix, label + "_" + chunk);

                    outDependencies = true;
                }

            }

            if (!inDependencies) {
                token1.putFeature(prefix + "_IN", "NULL");
            }
            if (!outDependencies) {
                token1.putFeature(prefix + "_OUT", "NULL");
            }
        }
    }

    private List<LabeledEdge> getInEdges(Token token, List<LabeledEdge> edges) {
        List<LabeledEdge> inEdges = new ArrayList<>();

        for (LabeledEdge edge : edges) {
            Token token2 = (Token) edge.getV2();

            if (token2.equals(token)) {
                inEdges.add(edge);
            }
        }

        return inEdges;
    }

    private List<LabeledEdge> getOutEdges(Token token, List<LabeledEdge> edges) {
        List<LabeledEdge> outEdges = new ArrayList<>();

        for (LabeledEdge edge : edges) {
            Token token1 = (Token) edge.getV1();

            if (token1.equals(token)) {
                outEdges.add(edge);
            }
        }

        return outEdges;
    }
}
