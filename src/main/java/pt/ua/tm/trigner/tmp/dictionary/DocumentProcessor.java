package pt.ua.tm.trigner.tmp.dictionary;

import com.aliasi.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.context.ContextProcessors;
import pt.ua.tm.neji.core.corpus.InputCorpus;
import pt.ua.tm.neji.core.corpus.OutputCorpus;
import pt.ua.tm.neji.core.pipeline.DefaultPipeline;
import pt.ua.tm.neji.core.pipeline.Pipeline;
import pt.ua.tm.neji.core.processor.BaseProcessor;
import pt.ua.tm.neji.dictionary.Dictionary;
import pt.ua.tm.neji.dictionary.DictionaryHybrid;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.neji.nlp.NLP;
import pt.ua.tm.neji.reader.RawReader;
import pt.ua.tm.neji.sentence.SentenceTagger;
import pt.ua.tm.neji.writer.A1Writer;

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

    public DocumentProcessor(Context context, InputCorpus textCorpus,
                             OutputCorpus outputCorpus) {
        super(context, textCorpus, outputCorpus);
    }

    @Override
    public void run() {
        try {
            // Get processors
            ContextProcessors processors = getContext().take();
            Corpus corpus = getInputCorpus().getCorpus();
            List<Pair> sentencesPositions = new ArrayList<>();

            Pipeline p = new DefaultPipeline();

            p.add(new RawReader());
            p.add(new SentenceTagger(processors.getSentenceSplitter(), sentencesPositions));
            p.add(new NLP(corpus, processors.getParser(), sentencesPositions));

            // Annotate
            for (Dictionary dictionary : getContext().getDictionaries()) {
                p.add(new DictionaryHybrid(dictionary, corpus));
            }

            // Change to proper writer
            p.add(new A1Writer(corpus));

            // Run processing pipeline
            p.run(getInputCorpus().getInStream(), getOutputCorpus().getOutStream());

            // Return processors
            getContext().put(processors);
        } catch (IOException | NejiException | InterruptedException e) {
            logger.error("ERROR:", e);
            return;
        }


    }
}
