package pt.ua.tm.trigner;

import monq.jfa.AbstractFaAction;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.neji.core.corpus.InputCorpus;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 17/12/12
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
public class A2Loader extends BaseLoader {

    private static Logger logger = LoggerFactory.getLogger(A2Loader.class);
    private InputCorpus a1Corpus, a2Corpus;

    public A2Loader(final Corpus corpus, final InputCorpus a1Corpus, final InputCorpus a2Corpus) throws NejiException {
        super(corpus);
        this.a1Corpus = a1Corpus;
        this.a2Corpus = a2Corpus;

        Nfa nfa = new Nfa(Nfa.NOTHING);
        setNFA(nfa, DfaRun.UNMATCHED_COPY, eof);
    }

    private AbstractFaAction eof = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            // Add annotations from A1 file
            parseCorpus(a1Corpus);
            // Add annotations from A2 file
            parseCorpus(a2Corpus);
        }
    };

    private void parseCorpus(final InputCorpus inputCorpus) {
        String line;
        try (
                InputStreamReader reader = new InputStreamReader(inputCorpus.getInStream());
                BufferedReader br = new BufferedReader(reader)
        ) {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String[] fields = parts[1].split("\\s+");

                // Discard events
                if (parts.length < 3) {
                    continue;
                }

                String group = fields[0];
                int startCharPos = Integer.parseInt(fields[1]);
                int endCharPos = Integer.parseInt(fields[2]) -1;
                String text = parts[2];

                addAnnotation(group, startCharPos, endCharPos, text);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void addAnnotation(final String group, int start, int end, String text) {

        for (Sentence sentence : getCorpus()) {
            if (start >= sentence.getStartSource() && end <= sentence.getEndSource()) {

                int startToken = 0, endToken = 0;
                for (int i = 0; i < sentence.size(); i++) {
                    Token t = sentence.getToken(i);

                    if (start >= (sentence.getStartSource() + t.getStartSource())) {
                        startToken = i;
                    }
                    if (end <= (sentence.getStartSource() + t.getEndSource())) {
                        endToken = i;
                        break;
                    }
                }

                if (endToken < startToken) {
                    // Annotation that does not fit our tokenisation
                    logger.debug("Annotation is not compatible with tokenization: {}", text);
                } else {

                    AnnotationID annotation = AnnotationID.newAnnotationIDByTokenPositions(sentence, startToken, endToken, 1.0);
                    Identifier identifier = new Identifier("", "", "", group);
                    annotation.addID(identifier);
                    sentence.addAnnotationToTree(annotation);
                }
            }
        }
    }


}
