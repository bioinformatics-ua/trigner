package pt.ua.tm.trigner.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.Evaluator;
import pt.ua.tm.trigner.input.DocumentsLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 00:13
 * To change this template use File | Settings | File Templates.
 */
public class Optmization {

    private static Logger logger = LoggerFactory.getLogger(Optmization.class);

    public static Map<String, Model> run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
                                         int numThreads)
            throws NejiException {
        return run(trainCorpusFolderPath, devCorpusFolderPath, null, numThreads);
    }

    public static Map<String, Model> run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
                                         final String gdepPath, int numThreads) {
        Documents trainDocuments = DocumentsLoader.load(trainCorpusFolderPath, gdepPath, numThreads);
        Documents devDocuments = DocumentsLoader.load(devCorpusFolderPath, gdepPath, numThreads);

        return run(trainDocuments, devDocuments);
    }

    public static Map<String, Model> run(final InputStream trainDocumentsStream, final InputStream devDocumentsStream)
            throws NejiException {

        Documents trainDocuments, devDocuments;
        try {
            trainDocuments = Documents.read(trainDocumentsStream);
            devDocuments = Documents.read(devDocumentsStream);
        } catch (ClassNotFoundException | IOException ex) {
            throw new NejiException("There was a problem reading the input documents.");
        }
        return run(trainDocuments, devDocuments);
    }

    public static Map<String, Model> run(final Documents trainDocuments, final Documents devDocuments) {
        Map<String, Model> models = new HashMap<>();


        for (String label : Configuration.getTriggers()) {
            // Start features flags
            boolean[] flags = new boolean[16];
            for (int i = 0; i < flags.length; i++) {
                flags[i] = false;
            }

            String dictionaryPath = "resources/dictionaries/cg/training/" + label + ".txt";

            double bestF1 = -1.0;
            for (int i = 0; i < flags.length; i++) {

                flags[i] = true;

                if (i == flags.length - 1) {
                    flags[i - 1] = false;
                }
                boolean thereWasABetterModel = false;
                for (int order = 1; order <= 3; order++) {
                    ModelConfig config = getModelConfig(flags, order);

                    Model model = new Model(config);
                    Pipe pipe = model.getFeaturePipe(dictionaryPath);

                    // Train
                    InstanceList train = TempData.getInstanceList(trainDocuments, pipe, label);
                    model.train(train);

                    // Dev
                    InstanceList dev = TempData.getInstanceList(devDocuments, pipe, label);

                    // Get performance
                    Evaluator evaluator = new Evaluator(model);
                    evaluator.evaluate(dev);
                    double f1 = evaluator.getF1();

                    // Keep or discard feature
                    if (f1 > bestF1) {
                        bestF1 = f1;
                        thereWasABetterModel = true;

                        // Add best model
                        models.put(label, model);

                        // Print best one so far
                        logger.info("FEATURE: {} | FEATURE_SIZE: {} | ORDER: {} | LABEL: {} | P: {} | R: {} | F1: {}",
                                new Object[]{Features.values()[i], train.getDataAlphabet().size(), order, label,
                                        evaluator.getPrecision(), evaluator.getRecall(), f1});
                    }

                    if (bestF1 == 100) {
                        break;
                    }
                }

                if (!thereWasABetterModel) {
                    flags[i] = false;
                }

            }
        }
        return models;
    }

    private static ModelConfig getModelConfig(boolean[] f, int order) {
        return new ModelConfig(f[0], false, f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10], false,
                false, f[11], false, f[12], f[13], f[14], f[15], order);
    }

    private enum Features {
        Token,
        Lemma,
        POS,
        Chunk,
        DependencyParsing,
        Capitalization,
        Counting,
        Symbols,
        CharNGrams,
        Suffix,
        Prefix,
        WordShape,
        Concepts,
        Verbs,
        Window,
        Conjunctions
    }
}
