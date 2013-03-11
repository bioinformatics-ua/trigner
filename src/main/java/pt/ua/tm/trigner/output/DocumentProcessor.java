package pt.ua.tm.trigner.output;

import com.aliasi.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.model.CRFBase;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.core.corpus.InputCorpus;
import pt.ua.tm.neji.core.corpus.OutputCorpus;
import pt.ua.tm.neji.core.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.core.processor.BaseProcessor;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.ml.MLModel;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentence.SentenceTagger;
import pt.ua.tm.neji.writer.A1Writer;
import pt.ua.tm.trigner.evaluation.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 17/12/12
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public class DocumentProcessor extends BaseProcessor {

    private static Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);

    private InputCorpus a1Corpus;

    public DocumentProcessor(Context context, InputCorpus textCorpus, InputCorpus a1Corpus,
                             OutputCorpus outputCorpus) {
        super(context, textCorpus, outputCorpus);
        this.a1Corpus = a1Corpus;
    }

    @Override
    public void run() {
        try {
            // Get processors
            ContextProcessors processors = getContext().take();
            Corpus corpus = getInputCorpus().getCorpus();
            List<Pair> sentencesPositions = new ArrayList<>();
//            List<Trigger> a1Annotations = new ArrayList<>();

            Pipeline p = new DefaultPipeline();

            // Load A1 annotations
//            p.add(new A1Loader(a1Corpus, a1Annotations));

            p.add(new RawReader());
            p.add(new SentenceTagger(processors.getSentenceSplitter(), sentencesPositions));
            p.add(new NLP(corpus, processors.getParser(), sentencesPositions));

            // Add annotations features to each sentence
//            p.add(new AnnotationsFeaturesLoader(corpus, a1Annotations));

            p.add(new A1Loader(corpus, a1Corpus));

            // Annotate
            for (int i = 0; i < getContext().getModels().size(); i++) {
                // Take model
                CRFBase crf = processors.getCRF(i);
                // Add ML recognizer to pipeline
                p.add(new ModelAnnotator(corpus, crf.getCRF()));
            }

            // Remove protein annotations
            p.add(new CleanTreeAnnotations(corpus, new String[]{"Protein"}));

            // Change to proper writer
            p.add(new A1Writer(corpus));

            // Run processing pipeline
            p.run(getInputCorpus().getInStream(), getOutputCorpus().getOutStream());

            // Return processors
            getContext().put(processors);
        } catch (NejiException | InterruptedException e) {
            logger.error("ERROR:", e);
            return;
        }


    }
}
