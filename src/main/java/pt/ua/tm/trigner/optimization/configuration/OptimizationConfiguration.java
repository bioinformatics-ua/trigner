package pt.ua.tm.trigner.optimization.configuration;

import pt.ua.tm.trigner.model.configuration.ModelConfiguration.ContextType;
import pt.ua.tm.trigner.model.features.FeatureType;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class OptimizationConfiguration extends Properties {
    public static final FeatureType[] features = new FeatureType[]{FeatureType.WORD, FeatureType.LEMMA, FeatureType.POS, FeatureType.CHUNK};
    public static final int[] orders = new int[]{1, 2, 3};
//    public static final int[] orders = new int[]{1};
    public static final ContextType[] contexts = new ContextType[]{ContextType.NONE, ContextType.WINDOW, ContextType.CONJUNCTIONS};
    public static final int[][] ngrams = new int[][]{
            {1, 2},
            {2, 3},
            {3, 4},
            {1, 2, 3},
            {2, 3, 4}
    };
    public static final int[][] hops = new int[][]{
            {2},
            {3},
            {2, 3}
    };

}
