package pt.ua.tm.trigner.model;

import pt.ua.tm.gimli.features.corpus.ChunkBIOTags;
import pt.ua.tm.gimli.features.corpus.pipeline.PipelineFeatureExtractor;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.model.features.concept.ConceptCounting;
import pt.ua.tm.trigner.model.features.concept.ConceptHeads;
import pt.ua.tm.trigner.model.features.concept.ConceptNames;
import pt.ua.tm.trigner.model.features.concept.ConceptTags;
import pt.ua.tm.trigner.model.features.dependency.*;
import pt.ua.tm.trigner.model.features.pipeline.DocumentsPipelineFeatureExtractor;
import pt.ua.tm.trigner.model.features.sentence.SentenceTokensCounting;
import pt.ua.tm.trigner.model.features.shortestpath.*;
import pt.ua.tm.trigner.model.transformer.ContextTransformer;
import pt.ua.tm.trigner.model.transformer.FeatureTransformer;
import pt.ua.tm.trigner.model.transformer.IntegerTransformer;
import pt.ua.tm.trigner.shared.CustomHashSet;
import pt.ua.tm.trigner.shared.Types;

import java.util.Set;

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

        // Chunk
        if (mc.isProperty("Chunk")) {
            p.add(new ChunkBIOTags("Chunk"));
        }

        // Concepts
        if (mc.isProperty("ConceptTags")) {
            p.add(new ConceptTags("ConceptTags"));
        }
        if (mc.isProperty("ConceptCounting")) {
            p.add(new ConceptCounting());
        }
        if (mc.isProperty("ConceptHeads")) {
            p.add(new ConceptHeads("ConceptHead"));
        }
        if (mc.isProperty("ConceptNames")) {
            p.add(new ConceptNames("ConceptNames"));
        }

        // Sentence
        if (mc.isProperty("SentenceTokensCounting")) {
            p.add(new SentenceTokensCounting("SentenceTokensCounting"));
        }

        // Dependency Feature
        if (mc.isProperty("DPModifiers")) {
            p.add(new DPModifiers("DPModifiers"));
        }
        if (mc.isProperty("DPInOutDependencies")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPInOutDependencies_length"), new IntegerTransformer());
            for (Integer hop : setHops) {
                String featureName = "DPInOutDependencies_" + hop;
                p.add(new DPInOutDependencies(featureName, hop));
            }
        }

        if (mc.isProperty("DPVertex")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPVertex_length"), new IntegerTransformer());
            Set<Types.VertexFeatureType> setFeatures = new CustomHashSet<>(mc.getProperty("DPVertex_type"), new FeatureTransformer());

            for (Integer hop : setHops) {
                for (Types.VertexFeatureType feature : setFeatures) {
                    String featureName = "DPVertex_" + hop + "_" + feature;
                    p.add(new DPVertexWalk(featureName, feature, hop));
                }
            }
        }
        if (mc.isProperty("DPEdge")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPEdge_length"), new IntegerTransformer());
            for (Integer hop : setHops) {
                String featureName = "DPEdge_" + hop;
                p.add(new DPEdgeWalk(featureName, hop));
            }
        }
        if (mc.isProperty("DPEdgeType")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPEdge_length"), new IntegerTransformer());
            for (Integer hop : setHops) {
                String featureName = "DPEdgeType_" + hop;
                p.add(new DPEdgeWalkType(featureName, hop));
            }
        }

        if (mc.isProperty("DPNGramsVertex")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPNGramsVertex_length"), new IntegerTransformer());
            Set<Types.VertexFeatureType> setFeatures = new CustomHashSet<>(mc.getProperty("DPNGramsVertex_type"), new FeatureTransformer());
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("DPNGramsVertex_sizes"), new IntegerTransformer());

            for (Integer hop : setHops) {
                for (Types.VertexFeatureType feature : setFeatures) {
                    for (Integer ngram : setGrams) {
                        String featureName = "DPNGramsVertex_" + hop + "_" + feature + "_" + ngram + "GRAMS";
                        p.add(new DPVertexNGrams(featureName, feature, hop, ngram));
                    }
                }
            }
        }
        if (mc.isProperty("DPNGramsEdge")) {
            Set<Integer> setHops = new CustomHashSet<>(mc.getProperty("DPNGramsEdge_length"), new IntegerTransformer());
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("DPNGramsEdge_sizes"), new IntegerTransformer());

            for (Integer hop : setHops) {
                for (Integer ngram : setGrams) {
                    String featureName = "DPNGramsEdge_" + hop + "_" + ngram + "GRAMS";
                    p.add(new DPEdgeNGrams(featureName, hop, ngram));
                }
            }
        }

        // Shortest Path features
        if (mc.isProperty("SPDistance")) {
            p.add(new SPEdgeDistance("SPDistance"));
        }
        if (mc.isProperty("SPChunkDistance")) {
            p.add(new SPChunkDistance("SPChunkDistance"));
        }
        if (mc.isProperty("SPVertex")) {
            Set<Types.VertexFeatureType> setFeatures = new CustomHashSet<>(mc.getProperty("SPVertex_type"), new FeatureTransformer());
            for (Types.VertexFeatureType feature : setFeatures) {
                String featureName = "SPVertex_" + feature;
                p.add(new SPVertexWalk(featureName, feature));
            }
        }
        if (mc.isProperty("SPEdge")) {
            p.add(new SPEdgeWalk("SPEdge"));
        }
        if (mc.isProperty("SPEdgeType")) {
            p.add(new SPEdgeWalkType("SPEdgeType"));
        }

        if (mc.isProperty("SPNGramsVertex")) {
            Set<Types.VertexFeatureType> setFeatures = new CustomHashSet<>(mc.getProperty("SPNGramsVertex_type"), new FeatureTransformer());
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("SPNGramsVertex_sizes"), new IntegerTransformer());
            for (Types.VertexFeatureType feature : setFeatures) {
                for (Integer ngram : setGrams) {
                    String featureName = "SPNGramsVertex_" + feature + "_" + ngram + "GRAMS";
                    p.add(new SPVertexNGrams(featureName, feature, ngram));
                }
            }
        }

        if (mc.isProperty("SPNGramsEdge")) {
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("SPNGramsEdge_sizes"), new IntegerTransformer());
            for (Integer ngram : setGrams) {
                String featureName = "SPNGramsEdge_" + ngram + "GRAMS";
                p.add(new SPEdgeNGrams(featureName, ngram));
            }
        }

        // Dependency window
        Set<ModelConfiguration.ContextType> contexts = new CustomHashSet<>(mc.getProperty("context"), new ContextTransformer());
//        ModelConfiguration.ContextType context = ModelConfiguration.ContextType.valueOf(mc.getProperty("context"));
        if (contexts.contains(ModelConfiguration.ContextType.DEPENDENCY_WINDOW)) {
            int maxHop = 3;
            p.add(new DependencyWindow("DEPENDENCY_WINDOW",
                    new Types.VertexFeatureType[]{Types.VertexFeatureType.LEMMA, Types.VertexFeatureType.POS, Types.VertexFeatureType.CHUNK},
                    maxHop));
            p.add(new DependencyWindowExtra("DEPENDENCY_WINDOW",
                    maxHop));
        }

        return p;
    }
}
