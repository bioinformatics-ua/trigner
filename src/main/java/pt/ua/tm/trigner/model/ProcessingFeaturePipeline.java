package pt.ua.tm.trigner.model;

import pt.ua.tm.gimli.features.corpus.ChunkTags;
import pt.ua.tm.gimli.features.corpus.DependencyNER;
import pt.ua.tm.gimli.features.corpus.pipeline.PipelineFeatureExtractor;
import pt.ua.tm.trigner.model.configuration.ModelConfiguration;
import pt.ua.tm.trigner.model.features.ConceptCounting;
import pt.ua.tm.trigner.model.features.ConceptTags;
import pt.ua.tm.trigner.model.features.FeatureType;
import pt.ua.tm.trigner.model.features.NGramsUtil;
import pt.ua.tm.trigner.model.features.dependency.*;
import pt.ua.tm.trigner.model.features.pipeline.DocumentsPipelineFeatureExtractor;
import pt.ua.tm.trigner.model.features.shortestpath.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 23:17
 * To change this template use File | Settings | File Templates.
 */
public class ProcessingFeaturePipeline {
    public static PipelineFeatureExtractor get(final ModelConfiguration mc) {
        PipelineFeatureExtractor p = new DocumentsPipelineFeatureExtractor();
        FeatureType ft;

        // Chunk
        if (mc.isProperty("chunk")) {
            p.add(new ChunkTags("CHUNK"));
        }

        // Concepts
        if (mc.isProperty("concept_tags")) {
            p.add(new ConceptTags());
        }
        if (mc.isProperty("concept_counting")) {
            p.add(new ConceptCounting());
        }

        // Dependency Features
        if (mc.isProperty("dp_modifiers")) {
            p.add(new DependencyNER());
        }

        int[] hops = NGramsUtil.fromString(mc.getProperty("dp_hops"));

        if (mc.isProperty("dp_vertex")) {
            ft = FeatureType.valueOf(mc.getProperty("dp_vertex_feature"));
            for (int hop : hops) {
                p.add(new DPVertexWalk("DP_VERTEX_WALK", ft, hop));
            }

        }
        if (mc.isProperty("dp_edge")) {
            for (int hop : hops) {
                p.add(new DPEdgeWalk("DP_EDGE_WALK", hop));
            }
        }
        if (mc.isProperty("dp_vertex_edge")) {
            ft = FeatureType.valueOf(mc.getProperty("dp_vertex_edge_feature"));
            for (int hop : hops) {
                p.add(new DPVertexEdgeWalk("DP_VERTEX_EDGE_WALK", ft, hop));
            }
        }

        int[] ngrams = NGramsUtil.fromString(mc.getProperty("dp_ngrams"));

        if (mc.isProperty("dp_ngrams_vertex")) {
            ft = FeatureType.valueOf(mc.getProperty("dp_ngrams_vertex_feature"));
            for (int hop : hops) {
                for (int ngram : ngrams) {
                    p.add(new DPVertexNGrams("DP_VERTEX_" + ngram + "GRAMS", ft, hop, ngram));
                }
            }
        }
        if (mc.isProperty("dp_ngrams_edge")) {
            p.add(new DPEdgeNGrams("DP_EDGE_3GRAMS", 3, 3));
            for (int hop : hops) {
                for (int ngram : ngrams) {
                    p.add(new DPEdgeNGrams("DP_EDGE_" + ngram + "GRAMS", hop, ngram));
                }
            }
        }

        // Dependency window
        ModelConfiguration.ContextType context = ModelConfiguration.ContextType.valueOf(mc.getProperty("context"));
        if (context.equals(ModelConfiguration.ContextType.DEPENDENCY_WINDOW)) {
            int maxHop = 3;
            p.add(new DependencyWindow("DEPENDENCY_WINDOW",
                    new FeatureType[]{FeatureType.LEMMA, FeatureType.POS, FeatureType.CHUNK},
                    maxHop));
        }

        // Shortest Path features
        if (mc.isProperty("sp_distance")) {
            p.add(new SPEdgeDistance("SP_EDGE_DISTANCE"));
        }
        if (mc.isProperty("sp_vertex")) {
            ft = FeatureType.valueOf(mc.getProperty("sp_vertex_feature"));
            p.add(new SPVertexWalk("SP_VERTEX_WALK", ft));
        }
        if (mc.isProperty("sp_edge")) {
            p.add(new SPEdgeWalk("SP_EDGE_WALK"));
        }
        if (mc.isProperty("sp_vertex_edge")) {
            ft = FeatureType.valueOf(mc.getProperty("sp_vertex_edge_feature"));
            p.add(new SPVertexEdgeWalk("SP_VERTEX_EDGE_WALK", ft));
        }

        ngrams = NGramsUtil.fromString(mc.getProperty("sp_ngrams"));
        if (mc.isProperty("sp_ngrams_vertex")) {
            ft = FeatureType.valueOf(mc.getProperty("sp_ngrams_vertex_feature"));
            for (int ngram : ngrams) {
                p.add(new SPVertexNGrams("SP_VERTEX_" + ngram + "GRAMS", ft, ngram));
            }
        }
        if (mc.isProperty("sp_ngrams_edge")) {
            for (int ngram : ngrams) {
                p.add(new SPEdgeNGrams("SP_EDGE_" + ngram + "GRAMS", ngram));
            }
        }

        return p;
    }
}
