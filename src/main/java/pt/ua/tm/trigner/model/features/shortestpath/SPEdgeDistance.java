package pt.ua.tm.trigner.model.features.shortestpath;

import martin.common.Tuple;
import org.jgrapht.alg.DijkstraShortestPath;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class SPEdgeDistance implements FeatureExtractor {

    private String prefix;

    public SPEdgeDistance(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {

        for (Token token : sentence) {

            Tuple<AnnotationID, Integer> closest = ShortestPathUtil.getClosestConcept(sentence, token);

            if (closest == null) {
                continue;
            }

            Token closestToken = getClosestToken(token, closest.getA());

            DijkstraShortestPath path = new DijkstraShortestPath(sentence.getDependencyGraph(), token, closestToken);

            if (path != null) {
                token.putFeature(prefix, new Double(path.getPathLength()).toString());
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
