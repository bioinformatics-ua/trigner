package pt.ua.tm.trigner.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import martin.common.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.Evaluator;
import pt.ua.tm.trigner.input.DocumentsLoader;
import pt.ua.tm.trigner.model.configuration.ModelConfiguration;
import pt.ua.tm.trigner.model.configuration.ModelConfiguration.ContextType;
import pt.ua.tm.trigner.model.configuration.OptimizationConfiguration;
import pt.ua.tm.trigner.model.features.FeatureType;

import java.io.FileWriter;
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
public class Optimization {

    private static Logger logger = LoggerFactory.getLogger(Optimization.class);

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

        for (int k = 0; k < Configuration.getTriggers().length; k++) {
            String label = Configuration.getTriggers()[k];

//        for (String label : Configuration.getTriggers()) {

            logger.info("############################");
            logger.info(label);
            logger.info("############################");

            // Get dictionary
            String dictionaryPath = "resources/dictionaries/cg/training/" + label + ".txt";

            // Start features flags
            boolean[] b = new boolean[26];
            FeatureType[] f = new FeatureType[6];
            int[][] g = new int[5][];
            int[] h;

            // Initialize booleans
            for (int i = 0; i < b.length; i++) {
                b[i] = false;
            }
            // Initialize feature types
            for (int i = 0; i < f.length; i++) {
                f[i] = OptimizationConfiguration.features[0];
            }
            // Initialize Ngrams
            for (int i = 0; i < g.length; i++) {
                g[i] = OptimizationConfiguration.ngrams[0];
            }
            // Initialize hops
            h = OptimizationConfiguration.hops[0];
            // Initialize context
            ContextType context = OptimizationConfiguration.contexts[0];


            double bestF1 = Double.MIN_VALUE;
            int bestOrder = OptimizationConfiguration.orders[0];

            // Optimize features usage
            for (int i = 0; i < b.length; i++) {
                b[i] = true;
                logger.info("FEATURE: {}", Features.values()[i]);

                boolean thereWasABetterModel = false;
                for (int order : OptimizationConfiguration.orders) {
                    // Get model performance
                    Tuple<Evaluator, Boolean> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                            models, bestF1,
                            b, f, g, h, context, order);

                    // Set best F1
                    Evaluator evaluator = tuple.getA();
                    boolean improvement = tuple.getB();
                    bestF1 = improvement ? evaluator.getF1() : bestF1;
                    // Set best order
                    bestOrder = improvement ? order : bestOrder;
                    if (improvement) {
                        thereWasABetterModel = true;
                    }
                }

                // No optimization using that feature
                if (!thereWasABetterModel) {
                    b[i] = false;
                }

            }

            // Optimize contexts
            ContextType bestContext = ContextType.NONE;
            for (int i = 1; i < OptimizationConfiguration.contexts.length; i++) {
                context = OptimizationConfiguration.contexts[i];
                logger.info("CONTEXT: {}", context);

                for (int order : OptimizationConfiguration.orders) {
                    // Get model performance
                    Tuple<Evaluator, Boolean> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                            models, bestF1,
                            b, f, g, h, context, order);

                    // Set best F1
                    Evaluator evaluator = tuple.getA();
                    boolean thereWasABetterModel = tuple.getB();
                    bestF1 = thereWasABetterModel ? evaluator.getF1() : bestF1;
                    // Set best context
                    bestContext = thereWasABetterModel ? context : bestContext;
                    bestOrder = thereWasABetterModel ? order : bestOrder;
                }


            }
            context = bestContext;


            // Optimize feature type for vertex
            for (int i = 0; i < f.length; i++) {

                Features currentFeature = Features.valueOf(FeatureTypesBridge.values()[i].toString());
                int pos = getFeatureIndex(currentFeature);
                if (!b[pos]) {
                    logger.info("Skipping {}. It is not used by the feature set.", currentFeature);
                    continue;
                }


                FeatureType bestFeatureType = f[i];
                for (int j = 1; j < OptimizationConfiguration.features.length; j++) {
                    f[i] = OptimizationConfiguration.features[j];

                    logger.info("FEATURE: {}, FEATURE TYPE: {}", FeatureTypesBridge.values()[i], f[i]);

                    // Get model performance
                    Tuple<Evaluator, Boolean> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                            models, bestF1,
                            b, f, g, h, context, bestOrder);

                    // Set best F1
                    Evaluator evaluator = tuple.getA();
                    boolean thereWasABetterModel = tuple.getB();
                    bestF1 = thereWasABetterModel ? evaluator.getF1() : bestF1;
                    bestFeatureType = thereWasABetterModel ? f[i] : bestFeatureType;
                }
                f[i] = bestFeatureType;
            }


            // Optimize n-grams
            for (int i = 0; i < g.length; i++) {

                Features currentFeature = Features.valueOf(NGramsBridge.values()[i].toString());
                int pos = getFeatureIndex(currentFeature);
                if (!b[pos]) {
                    logger.info("Skipping {}. It is not used by the feature set.", currentFeature);
                    continue;
                }


                int[] bestNGrams = g[i];
                for (int j = 1; j < OptimizationConfiguration.ngrams.length; j++) {
                    g[i] = OptimizationConfiguration.ngrams[j];

                    logger.info("FEATURE: {}, N-GRAMS: {}", NGramsBridge.values()[i], g[i]);

                    // Get model performance
                    Tuple<Evaluator, Boolean> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                            models, bestF1,
                            b, f, g, h, context, bestOrder);

                    // Set best F1
                    Evaluator evaluator = tuple.getA();
                    boolean thereWasABetterModel = tuple.getB();
                    bestF1 = thereWasABetterModel ? evaluator.getF1() : bestF1;
                    bestNGrams = thereWasABetterModel ? g[i] : bestNGrams;
                }
                g[i] = bestNGrams;
            }


            // Optimize dependency hops
            int[] bestHops = h;
            for (int i = 1; i < OptimizationConfiguration.hops.length; i++) {
                h = OptimizationConfiguration.hops[i];

                logger.info("HOPS: {}", h);

                // Get model performance
                Tuple<Evaluator, Boolean> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                        models, bestF1,
                        b, f, g, h, context, bestOrder);

                // Set best F1
                Evaluator evaluator = tuple.getA();
                boolean thereWasABetterModel = tuple.getB();
                bestF1 = thereWasABetterModel ? evaluator.getF1() : bestF1;
                bestHops = thereWasABetterModel ? h : bestHops;
            }
            h = bestHops;

        }
        return models;
    }

    private static ModelConfiguration getModelConfiguration(final boolean[] b, final FeatureType[] f, final int[][] g, final int[] h,
                                                            final ModelConfiguration.ContextType context, final int order) {
        return new ModelConfiguration(b, f, g, h, context, order);
    }

    private static Tuple<Evaluator, Boolean> getModelPerformance(Documents trainDocuments, Documents devDocuments,
                                                                 final String label, final String dictionaryPath,
                                                                 Map<String, Model> models, double bestF1,

                                                                 final boolean[] b, final FeatureType[] f, final int[][] g, final int[] h,
                                                                 final ModelConfiguration.ContextType context, final int order) {

        // Get Model Configuration
        ModelConfiguration modelConfiguration = getModelConfiguration(b, f, g, h, context, order);

        try {
            modelConfiguration.store(new FileWriter("/Users/david/Downloads/tmp.config"), "");
        } catch (IOException e) {
            throw new RuntimeException("There was a problem storing the model configuration.", e);
        }

        // Model
        Model model = new Model(modelConfiguration);

        // Pre-processing features
        ProcessingFeaturePipeline.get(modelConfiguration).run(trainDocuments);
        ProcessingFeaturePipeline.get(modelConfiguration).run(devDocuments);

        // Model features
        Pipe pipe = ModelFeaturePipeline.get(modelConfiguration, dictionaryPath);

        // Get Train instances
        InstanceList train = Documents2InstancesConverter.getInstanceList(trainDocuments, pipe, label);

        // Train model
        model.train(train);

        // Get Dev instances
        InstanceList dev = Documents2InstancesConverter.getInstanceList(devDocuments, pipe, label);

        // Get performance
        Evaluator evaluator = new Evaluator(model);
        evaluator.evaluate(dev);

        double f1 = evaluator.getF1();

        // Best F1
        boolean thereWasABetterModel = false;
        if (f1 > bestF1) {
            models.put(label, model);
            thereWasABetterModel = true;

            // Print best one so far
            logger.info("\t\tFEATURE_SIZE: {} | ORDER: {} | LABEL: {} | P: {} | R: {} | F1: {}",
                    new Object[]{train.getDataAlphabet().size(), order, label,
                            evaluator.getPrecision(), evaluator.getRecall(), f1});
        } else {
            logger.info("\t\tNo improvement.\tFEATURE_SIZE: {} | ORDER: {} | LABEL: {} | P: {} | R: {} | F1: {}",
                    new Object[]{train.getDataAlphabet().size(), order, label,
                            evaluator.getPrecision(), evaluator.getRecall(), f1});
        }

        // Clean features from documents
        String[] featuresToKeep = new String[]{"POS", "LEMMA"};
        trainDocuments.cleanFeatures(featuresToKeep);
        devDocuments.cleanFeatures(featuresToKeep);

        return new Tuple<>(evaluator, thereWasABetterModel);
    }

    private static int getFeatureIndex(Features feature) {
        for (int i = 0; i < Features.values().length; i++) {
            if (Features.values()[i].equals(feature)) {
                return i;
            }
        }
        return -1;
    }

    private enum Features {
        Token,
        Lemma,
        POS,
        Chunk,

        Capitalization,
        Counting,
        Symbols,

        CharNGrams,
        Suffix,
        Prefix,
        WordShape,

        ConceptsTags,
        ConceptsCounting,

        Triggers,

        DPModifiers,
        DPVertex,
        DPEdge,
        DPVertexEdge,
        DPNGramsVertex,
        DPNGramsEdge,

        SPDistance,
        SPVertex,
        SPEdge,
        SPVertexEdge,
        SPNGramsVertex,
        SPNGramsEdge
    }

    private enum FeatureTypesBridge {
        DPVertex,
        DPVertexEdge,
        DPNGramsVertex,

        SPVertex,
        SPVertexEdge,
        SPNGramsVertex,
    }

    private enum NGramsBridge {
        CharNGrams,
        Suffix,
        Prefix,

        DPNGramsVertex,
        DPNGramsEdge,

        SPNGramsVertex,
        SPNGramsEdge
    }

    private enum HopsBridge {
        DPVertex,
        DPEdge,
        DPVertexEdge,
        DPNGramsVertex,
        DPNGramsEdge,
    }
}
