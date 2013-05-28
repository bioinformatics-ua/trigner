package pt.ua.tm.trigner.annotate;

import com.aliasi.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.neji.core.corpus.InputCorpus;
import pt.ua.tm.neji.core.corpus.OutputCorpus;
import pt.ua.tm.neji.core.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentence.SentenceTagger;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.postprocessors.ConceptFilter;
import pt.ua.tm.trigner.postprocessors.ParenthesesFilter;

import java.io.IOException;
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
            List<String> sentencesInModelFormat = new ArrayList<>();

            Pipeline p = new DefaultPipeline();

            p.add(new RawReader());
            p.add(new SentenceTagger(processors.getSentenceSplitter(), sentencesPositions));
            p.add(new NLP(corpus, processors.getParser(), sentencesPositions));

            // Load concepts
            p.add(new A1Loader(corpus, a1Corpus));

            // Generate sentence in model format
            if (!getContext().getModels().isEmpty()) {
                p.add(new GenerateSentenceInModelFormat(corpus, sentencesInModelFormat, getContext().getModels()));
            }

            // Annotate
            for (int i = 0; i < getContext().getModels().size(); i++) {
                // Take model
                Model crf = processors.getCRF(i);
                // Add ML recognizer to pipeline
                p.add(new ModelAnnotator(corpus, crf.getCRF(), sentencesInModelFormat));
            }

            // Dictionaries
            for (Dictionary d : getContext().getDictionaries()) {
                DictionaryHybrid dtl = new DictionaryHybrid(d, corpus);
                p.add(dtl);
            }

            // Post-processing
            p.add(new ConceptFilter(corpus));
            p.add(new ParenthesesFilter(corpus));

            // Change to proper writer
            p.add(new A1Writer(corpus));

            // Run processing pipeline
            p.run(getInputCorpus().getInStream(), getOutputCorpus().getOutStream());

            // Return processors
            getContext().put(processors);

            logger.info("Finished processing {}", getInputCorpus().getFile().getName());
        } catch (IOException | NejiException | InterruptedException e) {
            logger.error("ERROR:", e);
            return;
        }


    }
}
