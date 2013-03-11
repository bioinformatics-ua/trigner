package pt.ua.tm.trigner.model.features;

import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class ConceptTags {

    public static void add(Documents documents) {
        for (Corpus corpus : documents) {
            for (Sentence sentence : corpus) {
                List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
                for (Token token : sentence.getTokens()) {
                    addAnnotationsAsFeatures(annotations, token);
                }
            }
        }
    }

    // Get concept annotations
    public static void addAnnotationsAsFeatures(final List<AnnotationID> annotations, final Token token) {
        final Set<String> semGroups = new HashSet<>();

        for (final AnnotationID annotation : annotations) {
            // Check if current node refers to our token
            if (token.getIndex() >= annotation.getStartIndex()
                    && token.getIndex() <= annotation.getEndIndex()) {

                for (final Identifier id : annotation.getIDs()) {
                    semGroups.add(id.getGroup());
                }
            }
        }

        // Sort semantic groups
        List<String> groups = Arrays.asList(semGroups.toArray(new String[]{}));
        for (String group : groups) {
            if (Configuration.getConceptsList().contains(group)) {
                token.putFeature("CONCEPT=" + group.toUpperCase(), "");
            }
        }
    }
}
