package pt.ua.tm.trigner.annotate;

import cc.mallet.fst.CRF;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Sequence;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.trigner.configuration.Global;
import pt.ua.tm.trigner.documents.Documents;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class Annotator {

//    public static void annotate(Documents documents, final Model model) {
//        // Remove trigger annotations from documents
//        removeTriggerAnnotations(documents);
//
//        CRF crf = model.getCRF();
//
//        for (Corpus corpus : documents) {
//            for (Sentence sentence : corpus) {
//                annotate(sentence, crf);
//            }
//        }
//
//    }

    public static void removeTriggerAnnotations(Documents documents) {
        for (Corpus corpus : documents) {
            for (Sentence sentence : corpus) {

                List<TreeNode<AnnotationID>> nodes = sentence.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
                for (TreeNode<AnnotationID> node : nodes) {
                    if (node.equals(sentence.getTree().getRoot())) {
                        continue;
                    }
                    AnnotationID annotation = node.getData();
                    List<Identifier> toRemove = new ArrayList<>();
                    for (Identifier identifier : annotation.getIDs()) {
                        if (Global.projectConfiguration.getEvents().contains(identifier.getGroup())) {
                            toRemove.add(identifier);
                        }
                    }
                    annotation.getIDs().removeAll(toRemove);

                    if (annotation.getIDs().isEmpty()) {
                        TreeNode<AnnotationID> parent = node.getParent();
                        int idx = parent.getChildren().indexOf(node);
                        parent.removeChildAt(idx);
                    }
                }
            }
        }
    }

    public static void annotate(Sentence sentence, final CRF crf, final String sentenceInModelFormat) {
        // Get pipe
        crf.getInputPipe().getDataAlphabet().stopGrowth();
        Pipe pipe = crf.getInputPipe();

//        String sentenceInModelFormat = getSentenceInModelFormat(sentence);
//        String sentenceInModelFormat = Documents2InstancesConverter.getSentenceData(sentence);

        // Get instance
        Instance instance = new Instance(sentenceInModelFormat, null, 0, null);
        instance = pipe.instanceFrom(instance);

        // Get predictions
        NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(crf);

        Sequence input = (Sequence) instance.getData();
        Transducer tran = crfTrainer.getTransducer();
        Sequence pred = tran.transduce(input);

        // Get score
        double logScore = new SumLatticeDefault(crf, input, pred).getTotalWeight();
        double logZ = new SumLatticeDefault(crf, input).getTotalWeight();
        double prob = Math.exp(logScore - logZ);

        // Add annotations to tree
        for (int i = 0; i < pred.size(); i++) {
            String l1 = pred.get(i).toString();

            if (!l1.equals("O")) {
                int start = i;
                int end = i;
                for (int j = i + 1; j < pred.size(); j++) {
                    String l2 = pred.get(j).toString();
                    if (!l2.equals(l1)) {
                        break;
                    }
                    end = j;
                    i++;
                }

                AnnotationID annotation = AnnotationID.newAnnotationIDByTokenPositions(sentence, start, end, prob);
                annotation.addID(new Identifier("", "", "", l1));
                sentence.addAnnotationToTree(annotation);
            }
        }

    }
}
