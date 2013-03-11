package pt.ua.tm.trigner.tmp;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.configuration.Configuration;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.CompleteEvaluator;
import pt.ua.tm.trigner.evaluation.Evaluator;
import pt.ua.tm.trigner.evaluation.Trigger;
import pt.ua.tm.trigner.evaluation.TriggerList;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.model.TempData;
import pt.ua.tm.trigner.model.features.Features;
import pt.ua.tm.trigner.output.Annotator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        String devDocumentsFilePath = "resources/corpus/bionlp2009/dev/documents.gz";
        String label = "Gene_expression";
        String modelConfigFilePath = "/Users/david/Downloads/" + label + ".config";


        Documents trainDocuments, devDocuments;
        ModelConfig modelConfig;

        try {
            trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(trainDocumentsFilePath)));
            devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devDocumentsFilePath)));
            modelConfig = new ModelConfig(modelConfigFilePath);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("ERROR:", ex);
            return;
        }

        // Add pre-processing features
        Features.add(new Documents[]{trainDocuments, devDocuments});

        Model model = new Model(modelConfig);
        String dictionaryPath = "resources/dictionaries/" + label + ".txt";
        Pipe pipe = model.getFeaturePipe(dictionaryPath);

        // Load data
        InstanceList trainInstanceList = TempData.getInstanceList(trainDocuments, pipe, label);
        System.exit(0);
        InstanceList devInstanceList = TempData.getInstanceList(devDocuments, pipe, label);

        // Train model
        model.train(trainInstanceList);


        // Evaluate
        Evaluator evaluator = new Evaluator(model);
        evaluator.evaluate(devInstanceList);
        logger.info("P:{}\tR:{}\tF1:{}", new Object[]{evaluator.getPrecision(), evaluator.getRecall(),
                evaluator.getF1()});
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
