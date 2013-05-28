package pt.ua.tm.trigner.configuration;

import org.apache.commons.lang3.ArrayUtils;
import pt.ua.tm.trigner.configuration.Global;
import pt.ua.tm.trigner.model.configuration.Types;
import pt.ua.tm.trigner.shared.CustomHashSet;
import pt.ua.tm.trigner.util.FeatureType;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public class ModelConfiguration extends Properties {


    public ModelConfiguration() {
        super();
    }

    public ModelConfiguration(Map<Types.Feature, Boolean> features, Map<Types.NGrams, int[]> ngrams,
                              Map<Types.VertexType, Set<FeatureType>> vertexTypes,
                              Map<Types.HopsLength, Set<Integer>> hopsLength,
                              Set<ContextType> contexts, final int order) {
        super();

        // Assume non existent with default values
        for (Types.Feature feature : Types.Feature.values()) {
            if (!features.containsKey(feature)) {
                features.put(feature, Boolean.FALSE);
            }
        }
        for (Types.NGrams ngram : Types.NGrams.values()) {
            if (!ngrams.containsKey(ngram)) {
//                ngrams.put(ngram, OptimizationConfiguration.ngrams[0]);
                ngrams.put(ngram, Global.optimizationConfiguration.getNgrams().get(0));
            }
        }
        for (Types.VertexType vertexType : Types.VertexType.values()) {
            if (!vertexTypes.containsKey(vertexType)) {
                Set<FeatureType> set = new CustomHashSet<>();

                set.add(Global.optimizationConfiguration.getVertex().get(0));
//                set.add(OptimizationConfiguration.features[0]);
//                vertexTypes.put(vertexType, OptimizationConfiguration.features[0]);
                vertexTypes.put(vertexType, set);
            }
        }
        for (Types.HopsLength hopLength : Types.HopsLength.values()) {
            if (!hopsLength.containsKey(hopLength)) {
                Set<Integer> set = new CustomHashSet<>();
                set.add(Global.optimizationConfiguration.getHops().get(0));
//                set.add(OptimizationConfiguration.hops[0]);
//                hopsLength.put(hopLength, OptimizationConfiguration.hops[0]);
                hopsLength.put(hopLength, set);
            }
        }


        // Feature usage
        for (Types.Feature feature : features.keySet()) {
            setProperty(feature.toString(), features.get(feature).toString());
        }

        // N-grams sizes
        for (Types.NGrams ngram : ngrams.keySet()) {
            Integer[] newArray = ArrayUtils.toObject(ngrams.get(ngram));
            Set<Integer> set = new CustomHashSet<>(newArray);
//            setProperty(ngram.toString(), NGramsUtil.toString(ngrams.get(ngram)));
            setProperty(ngram.toString(), set.toString());
        }

        // Vertex types
        for (Types.VertexType vertexType : vertexTypes.keySet()) {
            CustomHashSet<FeatureType> set = (CustomHashSet<FeatureType>) vertexTypes.get(vertexType);
//            setProperty(vertexType.toString(), vertexTypes.get(vertexType).toString());
            setProperty(vertexType.toString(), set.toString());
        }

        // Hops
        for (Types.HopsLength hopLength : hopsLength.keySet()) {
            CustomHashSet<Integer> set = (CustomHashSet<Integer>) hopsLength.get(hopLength);
//            setProperty(hopLength.toString(), hopsLength.get(hopLength).toString());
            setProperty(hopLength.toString(), set.toString());
        }

        // Context
        setProperty("context", contexts.toString());

        // Model order
        setProperty("model_order", new Integer(order).toString());
    }

    public boolean isProperty(final String key) {
        if (containsKey(key)) {
            String value = getProperty(key);
            if (value.equals("true")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void setProperty(final String key, boolean b) {
        setProperty(key, new Boolean(b).toString());
    }

    public static enum ContextType {
        NONE,
        WINDOW,
        CONJUNCTIONS,
        DEPENDENCY_WINDOW
    }
}
