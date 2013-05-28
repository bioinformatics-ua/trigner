package pt.ua.tm.trigner.model.features.concept;

import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;

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
public class ConceptTags implements FeatureExtractor {

    private String prefix;
    public ConceptTags(final String prefix){
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {
        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
        for (Token token : sentence.getTokens()) {
            addAnnotationsAsFeatures(annotations, token);
        }
    }

    // Get concept annotations
    private void addAnnotationsAsFeatures(final List<AnnotationID> annotations, final Token token) {
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
//                token.putFeature("CONCEPT=" + group.toUpperCase(), "");
                token.putFeature(prefix, group.toUpperCase());
            }
        }
    }
}
