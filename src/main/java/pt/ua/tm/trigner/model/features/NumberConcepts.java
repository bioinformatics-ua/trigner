package pt.ua.tm.trigner.model.features;

import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public class NumberConcepts {

    public static void add(Documents documents) {
        for (Corpus corpus : documents) {
            for (Sentence sentence : corpus) {
                for (String concept : Configuration.getConcepts()) {
                    setNumberOfConceptAsFeature(sentence, concept);
                }
            }
        }
    }

    private static int getNumberOfConcept(final Sentence sentence, final String concept) {
        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
        int counter = 0;
        for (AnnotationID annotation : annotations) {
            for (Identifier identifier : annotation.getIDs()) {
                if (identifier.getGroup().equals(concept)) {
                    counter++;
                    break;
                }
            }
        }
        return counter;
    }

    private static void setNumberOfConceptAsFeature(Sentence sentence, final String concept) {

        int numberOfConcepts = getNumberOfConcept(sentence, concept);

        for (Token token : sentence.getTokens()) {
            token.putFeature("NUM_CONCEPT", concept.toUpperCase() + "_" + Integer.toString(numberOfConcepts));
        }
    }
}
