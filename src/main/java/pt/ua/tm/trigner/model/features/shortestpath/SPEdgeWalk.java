package pt.ua.tm.trigner.model.features.shortestpath;

import martin.common.Tuple;
import org.jgrapht.alg.DijkstraShortestPath;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.corpus.dependency.DependencyTag;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.util.ShortestPathUtil;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class SPEdgeWalk implements FeatureExtractor {

    private String prefix;

    public SPEdgeWalk(final String prefix) {
        this.prefix = prefix;
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

                // Edge walk
                StringBuilder sb = new StringBuilder();
                for (Object obj : path.getPathEdgeList()) {
                    LabeledEdge<Token, DependencyTag> edge = (LabeledEdge<Token, DependencyTag>) obj;

                    sb.append(edge.getLabel());
                    sb.append("-");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }

                token.putFeature(prefix, sb.toString());
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
