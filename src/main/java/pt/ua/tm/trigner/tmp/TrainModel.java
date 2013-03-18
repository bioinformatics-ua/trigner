package pt.ua.tm.trigner.tmp;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.corpus.dependency.DependencyTag;
import pt.ua.tm.gimli.corpus.dependency.LabeledEdge;
import pt.ua.tm.gimli.features.corpus.ChunkTags;
import pt.ua.tm.gimli.features.corpus.pipeline.PipelineFeatureExtractor;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.CompleteEvaluator;
import pt.ua.tm.trigner.evaluation.Evaluator;
import pt.ua.tm.trigner.evaluation.Trigger;
import pt.ua.tm.trigner.evaluation.TriggerList;
import pt.ua.tm.trigner.model.Documents2InstancesConverter;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.model.features.ConceptTags;
import pt.ua.tm.trigner.model.features.FeatureType;
import pt.ua.tm.trigner.model.features.NumberConcepts;
import pt.ua.tm.trigner.model.features.dependency.*;
import pt.ua.tm.trigner.model.features.pipeline.DocumentsPipelineFeatureExtractor;
import pt.ua.tm.trigner.model.features.shortestpath.*;
import pt.ua.tm.trigner.output.Annotator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/03/13
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class TrainModel {

    private static Logger logger = LoggerFactory.getLogger(TrainModel.class);

    public static void main(String... args) {
//        String trainDocumentsFilePath = "resources/corpus/bionlp2009/train/documents.gz";
        String trainDocumentsFilePath = "/Users/david/Downloads/tmp/documents.gz";
//        String devDocumentsFilePath = "resources/corpus/bionlp2009/dev/documents.gz";
        String label = "Gene_expression";
        String modelConfigFilePath = "/Users/david/Downloads/" + label + ".config";


        Documents trainDocuments, devDocuments;
        ModelConfig modelConfig;

        try {
            trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(trainDocumentsFilePath)));
//            devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devDocumentsFilePath)));
            modelConfig = new ModelConfig(modelConfigFilePath);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("ERROR:", ex);
            return;
        }

        // Add pre-processing features
        PipelineFeatureExtractor p = getFeatureExtractorPipeline();
        p.run(trainDocuments);

        for (Corpus corpus : trainDocuments) {
            for (Sentence sentence : corpus) {
                Graph graph = sentence.getDependencyGraph();
                Iterator<Token> iter = new DepthFirstIterator<Token, LabeledEdge>(graph);
                while (iter.hasNext()) {
                    Token sourceToken = iter.next();

                    Set<LabeledEdge<Token, DependencyTag>> edgeSet = graph.edgesOf(sourceToken);
                    System.out.println("Vertex [" + sourceToken.getText() + "]:");

                    for (LabeledEdge<Token, DependencyTag> edge : edgeSet) {
                        Token targetToken = edge.getV1().equals(sourceToken) ? edge.getV2() : edge.getV1();
                        DependencyTag tag = edge.getLabel();

                        System.out.println("\t" + tag + "\t->\t" + targetToken.getText());
                    }


//                    LabeledEdge<Token, DependencyTag> edge = dependency.ed
//                    vertex = iter.next();
//                    System.out.println("Vertex [" + vertex.getText() + "] is connected to: " + dependency.edgesOf(vertex).toString());
                }

//                Token from = sentence.getToken(0);
//                Token to = sentence.getToken(17);
//                DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(graph, from, to);
//                System.out.println("Dijkstra:");
//                System.out.println("\tFrom: " + from.getText());
//                System.out.println("\tTo: " + to.getText());
//                System.out.println("LENGTH: " + dijkstraShortestPath.getPathLength());
//                GraphPath path = dijkstraShortestPath.getPath();

                sentence.getChunks().print();

            }
        }

        Model model = new Model(modelConfig);
        String dictionaryPath = "resources/dictionaries/" + label + ".txt";
        Pipe pipe = model.getFeaturePipe(dictionaryPath);

        // Load data
        InstanceList trainInstanceList = Documents2InstancesConverter.getInstanceList(trainDocuments, pipe, label);
        System.exit(0);
//        InstanceList devInstanceList = Documents2InstancesConverter.getInstanceList(devDocuments, pipe, label);

        // Train model
        model.train(trainInstanceList);


        // Evaluate
        Evaluator evaluator = new Evaluator(model);
//        evaluator.evaluate(devInstanceList);
        logger.info("P:{}\tR:{}\tF1:{}", new Object[]{evaluator.getPrecision(), evaluator.getRecall(),
                evaluator.getF1()});
    }

    private static PipelineFeatureExtractor getFeatureExtractorPipeline() {
        PipelineFeatureExtractor p = new DocumentsPipelineFeatureExtractor();

        p.add(new ChunkTags("CHUNK"));
        p.add(new ConceptTags());
        p.add(new NumberConcepts());
//        p.add(new DependencyWindowFeature("DEP_PATH",  new FeatureType[]{FeatureType.CHUNK, FeatureType.POS, FeatureType.LEMMA}, true, 3));
//        p.add(new DependencyNER());

        // Dependency Features
        p.add(new DPEdgeWalk("DP_EDGE_WALK", 3));
        p.add(new DPVertexWalk("DP_VERTEX_WALK", FeatureType.LEMMA, 3));
        p.add(new DPVertexEdgeWalk("DP_VERTEX_EDGE_WALK", FeatureType.LEMMA, 3));

        p.add(new DPVertexNGrams("DP_VERTEX_3GRAMS", FeatureType.LEMMA, 3, 3));

        p.add(new DPEdgeNGrams("DP_EDGE_3GRAMS", 3, 3));

        // Shortest Path features
        p.add(new SPEdgeDistance("SP_EDGE_DISTANCE"));

        p.add(new SPEdgeWalk("SP_EDGE_WALK"));
        p.add(new SPVertexWalk("SP_VERTEX_WALK", FeatureType.LEMMA));

        p.add(new SPVertexEdgeWalk("SP_VERTEX_EDGE_WALK", FeatureType.LEMMA));

        p.add(new SPVertexNGrams("SP_VERTEX_3GRAMS", FeatureType.LEMMA, 3));

        return p;
    }

    private static void eval(Documents documents, Model model) {
        // Get gold triggers
        List<TriggerList> gold = getTriggerList(documents);

        // Annotate
        Annotator.annotate(documents, model);

        // Get silver triggers
        List<TriggerList> silver = getTriggerList(documents);

        // Evaluate
        CompleteEvaluator evaluator = new CompleteEvaluator();

        for (int i = 0; i < gold.size(); i++) {
            TriggerList g = gold.get(i);
            TriggerList s = silver.get(i);

            evaluator.evaluate(g, s, false);
        }

        evaluator.print();
    }

    private static List<TriggerList> getTriggerList(Documents documents) {

        List<TriggerList> triggerLists = new ArrayList<>();

        for (Corpus corpus : documents) {
            TriggerList triggerList = new TriggerList();
            for (Sentence sentence : corpus) {
                List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);

                for (AnnotationID annotation : annotations) {
                    if (annotation.getIDs().isEmpty()) {
                        continue;
                    }

                    for (Identifier identifier : annotation.getIDs()) {
                        String group = identifier.getGroup();
                        if (Configuration.getConceptsList().contains(group)) {
                            continue;
                        }

                        // Create trigger
                        int start = sentence.getStartSource() + sentence.getToken(annotation.getStartIndex()).getStartSource();
                        int end = sentence.getStartSource() + sentence.getToken(annotation.getEndIndex()).getEndSource();
                        Trigger trigger = new Trigger(start, end, group);

                        // Add trigger to list
                        triggerList.add(trigger);
                    }
                }
            }
            // Add trigger list of this corpus to all trigger lists
            triggerLists.add(triggerList);
        }
        return triggerLists;
    }
}
