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
 * Date: 02/03/13
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class ConceptHeads implements FeatureExtractor {

    private String prefix;

    public ConceptHeads(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {
        Set<String> heads = getConceptsHeads(sentence);
        for (Token token : sentence.getTokens()) {
            for (String headLemma : heads) {
                token.putFeature(prefix, headLemma);
            }
        }
    }


    public Set<String> getConceptsHeads(final Sentence sentence) {

        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
        Set<String> headsLemmas = new HashSet<>();

        for (final AnnotationID annotation : annotations) {

            for (Identifier identifier:annotation.getIDs()){
                if (Global.projectConfiguration.getConcepts().contains(identifier.getGroup())){
                    Token headToken = annotation.getSentence().getToken(annotation.getStartIndex());
                    headsLemmas.add(headToken.getFeature("LEMMA").get(0));
                }
            }
        }
        return headsLemmas;
    }
}
