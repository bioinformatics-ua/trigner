package pt.ua.tm.trigner.postprocessors;

import monq.jfa.*;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.global.Global;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/14/13
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConceptFilter extends BaseLoader {
    private int sentence;

    public ConceptFilter(final Corpus corpus) throws NejiException {
        super(corpus);
        this.sentence = 0;

        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.ETag("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }


    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            Sentence s = getCorpus().getSentence(sentence);
            int numConcepts = getNumberOfConcepts(s);
            if (numConcepts == 0){
                s.getTree().getRoot().removeChildren();
            }
            sentence++;
        }
    };

    private int getNumberOfConcepts(final Sentence sentence) {
        int numConcepts = 0;
        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);
        for (AnnotationID annotation : annotations) {
            for (Identifier identifier : annotation.getIDs()) {
                if (Global.projectConfiguration.getConcepts().contains(identifier.getGroup())) {
                    numConcepts++;
                    break;
                }
            }
        }
        return numConcepts;
    }

}
