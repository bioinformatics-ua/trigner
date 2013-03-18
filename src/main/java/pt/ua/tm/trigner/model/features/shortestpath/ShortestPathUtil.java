package pt.ua.tm.trigner.model.features.shortestpath;

import martin.common.Tuple;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class ShortestPathUtil {

    public static Tuple<AnnotationID, Integer> getClosestConcept(final Sentence sentence, final Token token) {

        // Get Concept annotations
        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, false);
        List<AnnotationID> concepts = new ArrayList<>();
        for (AnnotationID annotation : annotations) {
            for (Identifier identifier : annotation.getIDs()) {
                if (Configuration.getConceptsList().contains(identifier.getGroup())) {
                    concepts.add(annotation);
                    break;
                }
            }
        }

        if (concepts.isEmpty()) {
            return null;
        }

        int minDistance = Integer.MAX_VALUE;
        AnnotationID closestConcept = null;
        for (AnnotationID concept : concepts) {

            int distance;
            if (token.getIndex() > concept.getEndIndex()) {
                distance = token.getIndex() - concept.getEndIndex();
            } else if (token.getIndex() < concept.getStartIndex()) {
                distance = concept.getStartIndex() - token.getIndex();
            } else {
                //The token is part of a concept annotation.
                return null;
            }

            if (distance < minDistance) {
                minDistance = distance;
                closestConcept = concept;
            }
        }

        if (closestConcept == null) {
            return null;
        }

        return new Tuple(closestConcept, minDistance);
    }
}
