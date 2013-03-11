package pt.ua.tm.trigner.output;

import cc.mallet.fst.CRF;
import monq.jfa.*;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.processing.Parentheses;
import pt.ua.tm.neji.core.module.BaseHybrid;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class ModelAnnotator extends BaseHybrid {

    private CRF crf;
    private int sentence;
    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            Sentence s = getCorpus().getSentence(sentence);

            // Annotate sentence
            Annotator.annotate(s, crf);

            // Post-processing
            Parentheses.processTreeRemoving(s);
            sentence++;
        }
    };

    public ModelAnnotator(Corpus corpus, CRF crf) throws NejiException {
        super(corpus);
        this.crf = crf;
        this.sentence = 0;

        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.ETag("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }
}
