package pt.ua.tm.trigner.annotate;

import monq.jfa.*;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.model.Documents2InstancesConverter;
import pt.ua.tm.trigner.model.ProcessingFeaturePipeline;
import pt.ua.tm.trigner.configuration.ModelConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenerateSentenceInModelFormat extends BaseLoader {
    private int sentenceCounter;
    private List<String> sentencesInModelFormat;
    private ModelConfiguration combinedModelConfiguration;

    public GenerateSentenceInModelFormat(Corpus corpus, List<String> sentencesInModelFormat, List<MLModel> models) throws NejiException {
        super(corpus);
        this.sentenceCounter = 0;
        this.sentencesInModelFormat = sentencesInModelFormat;
        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.ETag("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }

        // Generate combined model configuration
        List<ModelConfiguration> modelConfigurations = new ArrayList<>();
        for (MLModel model : models) {
            modelConfigurations.add(model.getConfig());
        }
        this.combinedModelConfiguration = CombineModelConfigurations.combineModelConfigurations(modelConfigurations);
    }

    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            Sentence s = getCorpus().getSentence(sentenceCounter);
            Corpus c = new Corpus();
            c.addSentence(s);
            Documents d = new Documents();
            d.add(c);

            ProcessingFeaturePipeline.get(combinedModelConfiguration).run(d);

            String sentenceInModelFormat = Documents2InstancesConverter.getSentenceData(s);
            sentencesInModelFormat.add(sentenceInModelFormat);

            sentenceCounter++;
        }
    };
}
