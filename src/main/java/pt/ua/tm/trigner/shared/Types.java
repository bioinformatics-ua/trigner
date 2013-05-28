package pt.ua.tm.trigner.shared;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 21/03/13
 * Time: 15:12
 * To change this template use File | Settings | File Templates.
 */
public class Types {
    public enum Feature {
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

        ConceptTags,
        ConceptCounting,
        ConceptHeads,
        ConceptNames,

        SentenceTokensCounting,

        Triggers,

        DPModifiers,
        DPInOutDependencies,
        DPWindow,
        DPVertex,
        DPEdge,
        DPEdgeType,
        DPVertexEdge,
        DPNGramsVertex,
        DPNGramsEdge,


        SPDistance,
        SPChunkDistance,
        SPVertex,
        SPEdge,
        SPEdgeType,
        SPVertexEdge,
        SPNGramsVertex,
        SPNGramsEdge
    }

    public enum VertexFeatureType {
        WORD,
        LEMMA,
        POS,
        CHUNK
    }

    public enum NGrams {
        CharNGrams_sizes,
        Suffix_sizes,
        Prefix_sizes,

        DPNGramsVertex_sizes,
        DPNGramsEdge_sizes,

        SPNGramsVertex_sizes,
        SPNGramsEdge_sizes
    }

    public enum VertexType {
        DPVertex_type,
        DPVertexEdge_type,
        DPNGramsVertex_type,

        SPVertex_type,
        SPVertexEdge_type,
        SPNGramsVertex_type,
    }


    public enum HopsLength {
        DPInOutDependencies_length,
        DPVertex_length,
        DPEdge_length,
        DPVertexEdge_length,
        DPNGramsVertex_length,
        DPNGramsEdge_length,
    }
}
