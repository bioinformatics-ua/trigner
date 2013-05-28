package pt.ua.tm.trigner.model.features.shortestpath;

import martin.common.Tuple;
import org.jgrapht.alg.DijkstraShortestPath;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.DependencyTag;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.VertexFeatureType;
import pt.ua.tm.trigner.util.NGramsUtil;
import pt.ua.tm.trigner.util.ShortestPathUtil;
import pt.ua.tm.trigner.util.TokenFeatureUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class SPVertexNGrams implements FeatureExtractor {

    private String prefix;
    private Types.VertexFeatureType feature;
    private int n;

    public SPVertexNGrams(final String prefix, final Types.VertexFeatureType feature, final int n) {
        this.prefix = prefix;
        this.feature = feature;
        this.n = n;
    }

    @Override
    public void extract(Sentence sentence) {

        for (Token token : sentence) {

            Tuple<AnnotationID, Integer> closest = ShortestPathUtil.getClosestConcept(sentence, token);

            if (closest == null) {
                token.putFeature(prefix, "NULL");
                continue;
            }

            Token closestToken = getClosestToken(token, closest.getA());

            DijkstraShortestPath path = new DijkstraShortestPath(sentence.getDependencyGraph(), token, closestToken);


            if (path != null && path.getPathEdgeList() != null) {
                // Vertex walk
                List<String> features = new ArrayList<>();

                Token previous = token;
                features.add(TokenFeatureUtil.getFeature(token, feature));

                for (Object obj : path.getPathEdgeList()) {
                    LabeledEdge<Token, DependencyTag> edge = (LabeledEdge<Token, DependencyTag>) obj;

                    Token token3;
                    if (edge.getV1().equals(previous)) {
                        token3 = edge.getV2();
                    } else {
                        token3 = edge.getV1();
                    }

                    features.add(TokenFeatureUtil.getFeature(token3, feature));
                    previous = token3;
                }


                // Add n-grams
//                Collections.sort(features);
                boolean added = false;
                for (String ngram : NGramsUtil.getNGrams(features, n, '_')) {
                    token.putFeature(prefix, ngram);
                    added = true;
                }

                if (!added){
                    token.putFeature(prefix, "NULL");
                }
            }

        }
    }

    private Token getClosestToken(final Token token, final AnnotationID concept) {
        if (token.getIndex() < concept.getStartIndex()) {
            return token.getSentence().getToken(concept.getStartIndex());
        } else {
            return token.getSentence().getToken(concept.getEndIndex());
        }

    }
}
