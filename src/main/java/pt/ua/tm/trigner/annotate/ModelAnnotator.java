package pt.ua.tm.trigner.annotate;

import cc.mallet.fst.CRF;
import monq.jfa.*;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.processing.Parentheses;
import pt.ua.tm.neji.core.module.BaseHybrid;
import pt.ua.tm.neji.exception.NejiException;

import java.util.List;

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

    private List<String> sentencesInModelFormat;
    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            Sentence s = getCorpus().getSentence(sentence);

//            Corpus c = new Corpus();
//            c.addSentence(s);
//            Documents d = new Documents();
//            d.add(c);
//
//            ProcessingFeaturePipeline.get(modelConfiguration).run(d);

            String sentenceInModelFormat = sentencesInModelFormat.get(sentence);
//            Instance instance = instances.get(sentence);


            // Annotate sentence
            Annotator.annotate(s, crf, sentenceInModelFormat);

            // Post-processing
            Parentheses.processTreeRemoving(s);
            sentence++;

//            String[] featuresToKeep = new String[]{"POS", "LEMMA"};
//            d.cleanFeatures(featuresToKeep);
        }
    };

    public ModelAnnotator(Corpus corpus, CRF crf, List<String> sentencesInModelFormat) throws NejiException {
        super(corpus);
        this.crf = crf;
        this.sentence = 0;
        this.sentencesInModelFormat = sentencesInModelFormat;
//        this.modelConfiguration = modelConfiguration;

        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.ETag("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }
}
