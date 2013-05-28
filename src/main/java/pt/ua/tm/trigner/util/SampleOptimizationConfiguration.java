package pt.ua.tm.trigner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.configuration.OptimizationConfiguration;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.VertexFeatureType;
import pt.ua.tm.trigner.shared.Types.Feature;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/22/13
 * Time: 5:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleOptimizationConfiguration {

    private static Logger logger = LoggerFactory.getLogger(SampleOptimizationConfiguration.class);

    public static OptimizationConfiguration getSampleOptimizationConfiguration() {
        OptimizationConfiguration optimizationConfiguration = new OptimizationConfiguration();

        // Features
        optimizationConfiguration.getFeatures().add(Feature.Token);
        optimizationConfiguration.getFeatures().add(Feature.Lemma);
        optimizationConfiguration.getFeatures().add(Feature.POS);
        optimizationConfiguration.getFeatures().add(Feature.Chunk);

        optimizationConfiguration.getFeatures().add(Feature.Capitalization);
        optimizationConfiguration.getFeatures().add(Feature.Counting);
        optimizationConfiguration.getFeatures().add(Feature.Symbols);

        optimizationConfiguration.getFeatures().add(Feature.CharNGrams);
        optimizationConfiguration.getFeatures().add(Feature.Suffix);
        optimizationConfiguration.getFeatures().add(Feature.Prefix);
        optimizationConfiguration.getFeatures().add(Feature.WordShape);

        optimizationConfiguration.getFeatures().add(Feature.ConceptTags);
        optimizationConfiguration.getFeatures().add(Feature.ConceptCounting);
        optimizationConfiguration.getFeatures().add(Feature.ConceptHeads);
        optimizationConfiguration.getFeatures().add(Feature.ConceptNames);

        optimizationConfiguration.getFeatures().add(Feature.SentenceTokensCounting);

        optimizationConfiguration.getFeatures().add(Feature.Triggers);

        optimizationConfiguration.getFeatures().add(Feature.DPModifiers);
        optimizationConfiguration.getFeatures().add(Feature.DPInOutDependencies);
        optimizationConfiguration.getFeatures().add(Feature.DPEdge);
        optimizationConfiguration.getFeatures().add(Feature.DPEdgeType);
        optimizationConfiguration.getFeatures().add(Feature.DPNGramsVertex);
        optimizationConfiguration.getFeatures().add(Feature.DPNGramsEdge);

        optimizationConfiguration.getFeatures().add(Feature.SPDistance);
        optimizationConfiguration.getFeatures().add(Feature.SPChunkDistance);
        optimizationConfiguration.getFeatures().add(Feature.SPVertex);
        optimizationConfiguration.getFeatures().add(Feature.SPEdge);
        optimizationConfiguration.getFeatures().add(Feature.SPEdgeType);
        optimizationConfiguration.getFeatures().add(Feature.SPNGramsVertex);
        optimizationConfiguration.getFeatures().add(Feature.SPNGramsEdge);

        // Contexts
        optimizationConfiguration.getContexts().add(ModelConfiguration.ContextType.NONE);
        optimizationConfiguration.getContexts().add(ModelConfiguration.ContextType.WINDOW);
        optimizationConfiguration.getContexts().add(ModelConfiguration.ContextType.DEPENDENCY_WINDOW);

        // Orders
        optimizationConfiguration.getOrders().add(1);
        optimizationConfiguration.getOrders().add(2);
        optimizationConfiguration.getOrders().add(3);

        // N-grams
        optimizationConfiguration.getNgrams().add(new int[]{2, 3, 4});
        optimizationConfiguration.getNgrams().add(new int[]{2, 3});
        optimizationConfiguration.getNgrams().add(new int[]{3, 4});

        // Hops
        optimizationConfiguration.getHops().add(3);
        optimizationConfiguration.getHops().add(2);

        // Vertex
        optimizationConfiguration.getVertex().add(Types.VertexFeatureType.LEMMA);
        optimizationConfiguration.getVertex().add(Types.VertexFeatureType.WORD);
        optimizationConfiguration.getVertex().add(Types.VertexFeatureType.POS);
        optimizationConfiguration.getVertex().add(Types.VertexFeatureType.CHUNK);

        return optimizationConfiguration;
    }

    public static void main(String... args) {
        String filePath = "optimizationConfiguration.json";
        OptimizationConfiguration optimizationConfiguration = getSampleOptimizationConfiguration();
        try {
            optimizationConfiguration.write(new FileOutputStream(filePath));
        } catch (IOException e) {
            logger.info("There was a problem storing the optimization configuration in the file {}.", filePath);
            return;
        }
    }
}
