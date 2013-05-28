package pt.ua.tm.trigner.model.features.concept;

import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.global.Global;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 4/12/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConceptNames implements FeatureExtractor {

    private String prefix;

    public ConceptNames(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {
        for (Token token : sentence) {
            for (String name : getConceptNames(sentence)) {
                token.putFeature(prefix, name);
            }
        }
    }

    private Set<String> getConceptNames(final Sentence sentence) {
        Set<String> names = new HashSet<>();

        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
        for (AnnotationID annotation : annotations) {

            for (Identifier identifier : annotation.getIDs()) {
                if (Global.projectConfiguration.getConcepts().contains(identifier.getGroup())) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = annotation.getStartIndex(); i <= annotation.getEndIndex(); i++) {
                        sb.append(sentence.getToken(i).getText());
                        sb.append("_");
                    }
                    sb.setLength(sb.length() - 1);
                    names.add(sb.toString());
                }
            }
        }

        return names;
    }
}
