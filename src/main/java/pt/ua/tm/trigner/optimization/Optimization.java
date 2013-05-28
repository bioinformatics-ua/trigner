package pt.ua.tm.trigner.optimization;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import martin.common.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.Evaluator;
import pt.ua.tm.trigner.model.Documents2InstancesConverter;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.model.ModelFeaturePipeline;
import pt.ua.tm.trigner.model.ProcessingFeaturePipeline;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.configuration.ModelConfiguration.ContextType;
import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.Feature;
import pt.ua.tm.trigner.shared.CustomHashSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 00:13
 * To change this template use File | Settings | File Templates.
 */
public class Optimization {

    private static Logger logger = LoggerFactory.getLogger(Optimization.class);

//    //    public static Map<String, Model> run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
//    public static void run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
//                           int numThreads)
//            throws NejiException {
////        return run(trainCorpusFolderPath, devCorpusFolderPath, null, numThreads);
//        run(trainCorpusFolderPath, devCorpusFolderPath, null, numThreads);
//    }
//
//    //    public static Map<String, Model> run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
//    public static void run(final String trainCorpusFolderPath, final String devCorpusFolderPath,
//                           final String gdepPath, int numThreads) {
//        Documents trainDocuments = DocumentsLoader.load(trainCorpusFolderPath, gdepPath, numThreads);
//        Documents devDocuments = DocumentsLoader.load(devCorpusFolderPath, gdepPath, numThreads);
//
////        return run(trainDocuments, devDocuments);
//        run(trainDocuments, devDocuments);
//    }
//
//    //    public static Map<String, Model> run(final InputStream trainDocumentsStream, final InputStream devDocumentsStream)
//    public static void run(final InputStream trainDocumentsStream, final InputStream devDocumentsStream)
//            throws NejiException {
//
//        Documents trainDocuments, devDocuments;
//        try {
//            trainDocuments = Documents.read(trainDocumentsStream);
//            devDocuments = Documents.read(devDocumentsStream);
//        } catch (ClassNotFoundException | IOException ex) {
//            throw new NejiException("There was a problem reading the input documents.");
//        }
////        return run(trainDocuments, devDocuments);
//        run(trainDocuments, devDocuments);
//    }

//    public static void run(final Documents trainDocuments, final Documents devDocuments, final String dictionaryFolderPath) {
//
//        for (int k = 0; k < Global.projectConfiguration.getEvents().size(); k++) {
//            String label = Global.projectConfiguration.getEvents().get(k);
//            run(trainDocuments, devDocuments, label);
//        }
////        return models;
//    }


    public static ModelConfiguration run(Documents trainDocuments, Documents devDocuments, String dictionaryPath, String label) {
        Model bestModel = null;

        logger.info("############################");
        logger.info(label);
        logger.info("############################");

        // Start features flags
        Map<Types.Feature, Boolean> features = new HashMap<>();
        Map<Types.NGrams, int[]> ngrams = new HashMap<>();
        Map<Types.VertexType, Set<Types.VertexFeatureType>> vertexTypes = new HashMap<>();
        Map<Types.HopsLength, Set<Integer>> hopsLength = new HashMap<>();


        ContextType context = Global.optimizationConfiguration.getContexts().get(0);
        Set<ContextType> contexts = new CustomHashSet<>();
        contexts.add(context);


        double bestF1 = Double.MIN_VALUE;
        int bestOrder = Global.optimizationConfiguration.getOrders().get(0);

        // Optimize features usage
        for (Types.Feature feature : Global.optimizationConfiguration.getFeatures()) {
            features.put(feature, true);

            logger.info("FEATURE: {}", feature);

            // Get model performance
            Tuple<Evaluator, Model> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                    features, ngrams, vertexTypes, hopsLength, contexts);

            Evaluator evaluator = tuple.getA();
            Model model = tuple.getB();

            boolean improvement = false;
            if (evaluator != null) {
                improvement = evaluator.getF1() > bestF1;
            }

            // Set best F1
            bestF1 = improvement ? evaluator.getF1() : bestF1;
            bestOrder = improvement ? Integer.parseInt(model.getModelConfiguration().getProperty("model_order")) : bestOrder;
            if (improvement) {
//                    models.put(label, model);
                bestModel = model;
                logger.info("There was a better model: F1 = {}", evaluator.getF1());
            } else {
                features.put(feature, false);
            }
        }

        // Optimize contexts
        ContextType bestContext = ContextType.NONE;

        // Continue if it is possible to build windows and conjunctions
        for (int i = 1; i < Global.optimizationConfiguration.getContexts().size(); i++) {
            context = Global.optimizationConfiguration.getContexts().get(i);
            contexts = new CustomHashSet<>();
            contexts.add(context);
            logger.info("CONTEXT: {}", context);

            // Get model performance
            Tuple<Evaluator, Model> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                    features, ngrams, vertexTypes, hopsLength, contexts);

            Evaluator evaluator = tuple.getA();
            Model model = tuple.getB();
            boolean improvement = false;
            if (evaluator != null) {
                improvement = evaluator.getF1() > bestF1;
            }

            // Set best F1
            bestF1 = improvement ? evaluator.getF1() : bestF1;
            bestOrder = improvement ? Integer.parseInt(model.getModelConfiguration().getProperty("model_order")) : bestOrder;
            bestContext = improvement ? context : bestContext;
            if (improvement) {
//                        models.put(label, model);
                bestModel = model;
                logger.info("There was a better model: F1 = {}", evaluator.getF1());
            }
        }
        context = bestContext;
        contexts = new CustomHashSet<>();
        contexts.add(context);


        // Optimize feature type for vertex
        for (Types.VertexType vertexType : Types.VertexType.values()) {
            String vertexFeature = vertexType.toString().substring(0, vertexType.toString().lastIndexOf('_'));
            Feature feature = Feature.valueOf(vertexFeature);

            if (features.get(feature) == null || !features.get(feature)) {
                logger.info("Skipping {}. It is not used by the feature set.", feature);
                continue;
            }

            Types.VertexFeatureType bestVertexFeatureType = Global.optimizationConfiguration.getVertex().get(0);
            for (int j = 1; j < Global.optimizationConfiguration.getVertex().size(); j++) {
                Types.VertexFeatureType vertexFeatureType = Global.optimizationConfiguration.getVertex().get(j);

                Set<Types.VertexFeatureType> set = new CustomHashSet<>();
                set.add(vertexFeatureType);
//                vertexTypes.put(vertexType, vertexFeatureType);
                vertexTypes.put(vertexType, set);

                logger.info("FEATURE: {}, FEATURE TYPE: {}", vertexType, vertexFeatureType);

                // Get model performance
                Tuple<Evaluator, Model> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                        features, ngrams, vertexTypes, hopsLength, contexts);

                Evaluator evaluator = tuple.getA();
                Model model = tuple.getB();
                boolean improvement = false;
                if (evaluator != null) {
                    improvement = evaluator.getF1() > bestF1;
                }

                // Set best F1
                bestF1 = improvement ? evaluator.getF1() : bestF1;
                bestOrder = improvement ? Integer.parseInt(model.getModelConfiguration().getProperty("model_order")) : bestOrder;
                bestVertexFeatureType = improvement ? vertexFeatureType : bestVertexFeatureType;
                if (improvement) {
//                        models.put(label, model);
                    bestModel = model;
                    logger.info("There was a better model: F1 = {}", evaluator.getF1());
                }
            }
            Set<Types.VertexFeatureType> set = new CustomHashSet<>();
            set.add(bestVertexFeatureType);
            vertexTypes.put(vertexType, set);
        }

        // Optimize n-grams
        for (Types.NGrams ngramType : Types.NGrams.values()) {
            String ngramFeature = ngramType.toString().substring(0, ngramType.toString().lastIndexOf('_'));
            Feature feature = Feature.valueOf(ngramFeature);
            if (features.get(feature) == null || !features.get(feature)) {
                logger.info("Skipping {}. It is not used by the feature set.", feature);
                continue;
            }

            int[] bestNGrams = Global.optimizationConfiguration.getNgrams().get(0);
            for (int j = 1; j < Global.optimizationConfiguration.getNgrams().size(); j++) {
                int[] ngram = Global.optimizationConfiguration.getNgrams().get(j);
                ngrams.put(ngramType, ngram);

                logger.info("FEATURE: {}, N-GRAMS: {}", ngramType, ngram);

                // Get model performance
                Tuple<Evaluator, Model> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                        features, ngrams, vertexTypes, hopsLength, contexts);

                Evaluator evaluator = tuple.getA();
                Model model = tuple.getB();
                boolean improvement = false;
                if (evaluator != null) {
                    improvement = evaluator.getF1() > bestF1;
                }

                // Set best F1
                bestF1 = improvement ? evaluator.getF1() : bestF1;
                bestOrder = improvement ? Integer.parseInt(model.getModelConfiguration().getProperty("model_order")) : bestOrder;
                bestNGrams = improvement ? ngram : bestNGrams;
                if (improvement) {
//                        models.put(label, model);
                    bestModel = model;
                    logger.info("There was a better model: F1 = {}", evaluator.getF1());
                }

            }
            ngrams.put(ngramType, bestNGrams);
        }


        // Optimize dependency hops
        for (Types.HopsLength hopsType : Types.HopsLength.values()) {
            String hopsFeature = hopsType.toString().substring(0, hopsType.toString().lastIndexOf('_'));
            Feature feature = Feature.valueOf(hopsFeature);
            if (features.get(feature) == null || !features.get(feature)) {
                logger.info("Skipping {}. It is not used by the feature set.", feature);
                continue;
            }

            int bestHopLength = Global.optimizationConfiguration.getHops().get(0);
            for (int i = 1; i < Global.optimizationConfiguration.getHops().size(); i++) {
                Integer hopLength = Global.optimizationConfiguration.getHops().get(i);

                Set<Integer> set = new CustomHashSet<>();
                set.add(hopLength);

                hopsLength.put(hopsType, set);

                logger.info("HOPS: {}", hopLength);

                // Get model performance
                Tuple<Evaluator, Model> tuple = getModelPerformance(trainDocuments, devDocuments, label, dictionaryPath,
                        features, ngrams, vertexTypes, hopsLength, contexts);

                Evaluator evaluator = tuple.getA();
                Model model = tuple.getB();
                boolean improvement = false;
                if (evaluator != null) {
                    improvement = evaluator.getF1() > bestF1;
                }

                // Set best F1
                bestF1 = improvement ? evaluator.getF1() : bestF1;
                bestOrder = improvement ? Integer.parseInt(model.getModelConfiguration().getProperty("model_order")) : bestOrder;
                bestHopLength = improvement ? hopLength : bestHopLength;
                if (improvement) {
//                        models.put(label, model);
                    bestModel = model;
                    logger.info("There was a better model: F1 = {}", evaluator.getF1());
                }

            }
            Set<Integer> set = new CustomHashSet<>();
            set.add(bestHopLength);
            hopsLength.put(hopsType, set);
        }

        // Suggest garbage collection
        System.gc();

        return bestModel.getModelConfiguration();
    }

    private static Tuple<Evaluator, Model> getModelPerformance(Documents trainDocuments, Documents devDocuments,
                                                               final String label, final String dictionaryPath,

                                                               Map<Types.Feature, Boolean> features,
                                                               Map<Types.NGrams, int[]> ngrams,
                                                               Map<Types.VertexType, Set<Types.VertexFeatureType>> vertexTypes,
                                                               Map<Types.HopsLength, Set<Integer>> hopsLength,

                                                               final Set<ModelConfiguration.ContextType> contexts) {

        // Get Model Configuration
        ModelConfiguration modelConfiguration = new ModelConfiguration(features, ngrams, vertexTypes, hopsLength, contexts, 1);

//        try {
//            modelConfiguration.store(new FileWriter("model.config"), "TEMP");
//        } catch (IOException e) {
//            throw new RuntimeException("There was a problem storing the configuration file.");
//        }

        // Pre-processing features
        ProcessingFeaturePipeline.get(modelConfiguration).run(trainDocuments);
        ProcessingFeaturePipeline.get(modelConfiguration).run(devDocuments);

        // Model features
        Pipe pipe = ModelFeaturePipeline.get(modelConfiguration, dictionaryPath);

        // Get Annotate instances
        InstanceList train = Documents2InstancesConverter.getInstanceList(trainDocuments, pipe, label);
        // Get Dev instances
        InstanceList dev = Documents2InstancesConverter.getInstanceList(devDocuments, pipe, label);

        double bestF1 = Double.MIN_VALUE;
        Evaluator bestEvaluator = null;
        Model bestModel = null;
        int bestOrder = Global.optimizationConfiguration.getOrders().get(0);
        for (int order : Global.optimizationConfiguration.getOrders()) {
            modelConfiguration.setProperty("model_order", new Integer(order).toString());

            // Annotate model
            Model model = new Model(modelConfiguration);
            model.train(train);

            // Get performance
            Evaluator evaluator = new Evaluator(model);
            evaluator.evaluate(dev);

            double f1 = evaluator.getF1();

            // Best F1
            if (f1 > bestF1) {
                bestEvaluator = evaluator;
                bestModel = model;
                bestF1 = f1;
                bestOrder = order;
            }
            // Print best one so far
            logger.info("\t\tFEATURE_SIZE: {} | ORDER: {} | LABEL: {} | P: {} | R: {} | F1: {}",
                    new Object[]{train.getDataAlphabet().size(), order, label,
                            evaluator.getPrecision(), evaluator.getRecall(), f1});
        }


        // Clean features from documents
        String[] featuresToKeep = new String[]{"POS", "LEMMA"};
        trainDocuments.cleanFeatures(featuresToKeep);
        devDocuments.cleanFeatures(featuresToKeep);

        modelConfiguration.setProperty("model_order", new Integer(bestOrder).toString());
        System.gc();

        return new Tuple<>(bestEvaluator, bestModel);
    }
}
