package pt.ua.tm.trigner.model.configuration;

import pt.ua.tm.trigner.model.features.FeatureType;
import pt.ua.tm.trigner.model.features.NGramsUtil;

import java.util.Properties;

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

    public ModelConfiguration(final boolean[] b, final FeatureType[] f, final int[][] g, final int[] h, final ContextType context, final int order) {
        super();

        if (b.length != 26 || f.length != 6 || g.length != 5) {
            throw new IllegalArgumentException("Provided arrays are not compatible!");
        }

        // Token
        setProperty("token", b[0]);
        setProperty("lemma", b[1]);
        setProperty("pos", b[2]);
        setProperty("chunk", b[3]);

        // Orthographic
        setProperty("capitalization", b[4]);
        setProperty("counting", b[5]);
        setProperty("symbols", b[6]);

        // Morphological
        setProperty("char_ngrams", b[7]);
        setProperty("char_ngrams_sizes", NGramsUtil.toString(g[0]));

        setProperty("suffix", b[8]);
        setProperty("suffix_sizes", NGramsUtil.toString(g[1]));

        setProperty("prefix", b[9]);
        setProperty("prefix_sizes", NGramsUtil.toString(g[2]));

        setProperty("word_shape", b[10]);

        // Concept-based
        setProperty("concept_tags", b[11]);
        setProperty("concept_counting", b[12]);

        // Triggers
        setProperty("triggers", b[13]);

        // Dependency
        setProperty("dp_modifiers", b[14]);
        setProperty("dp_hops", NGramsUtil.toString(h));

        setProperty("dp_vertex", b[15]);
        setProperty("dp_vertex_feature", f[0].toString());

        setProperty("dp_edge", b[16]);

        setProperty("dp_vertex_edge", b[17]);
        setProperty("dp_vertex_edge_feature", f[1].toString());

        setProperty("dp_ngrams", NGramsUtil.toString(g[3]));
        setProperty("dp_ngrams_vertex", b[18]);
        setProperty("dp_ngrams_vertex_feature", f[2].toString());

        setProperty("dp_ngrams_edge", b[19]);

        // Shortest path
        setProperty("sp_distance", b[20]);
        setProperty("sp_vertex", b[21]);
        setProperty("sp_vertex_feature", f[3].toString());

        setProperty("sp_edge", b[22]);

        setProperty("sp_vertex_edge", b[23]);
        setProperty("sp_vertex_edge_feature", f[4].toString());

        setProperty("sp_ngrams", NGramsUtil.toString(g[4]));
        setProperty("sp_ngrams_vertex", b[24]);
        setProperty("sp_ngrams_vertex_feature", f[5].toString());

        setProperty("sp_ngrams_edge", b[25]);

        // Context
        setProperty("context", context.toString());

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
