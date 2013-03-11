package pt.ua.tm.trigner.evaluation;

import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.Transducer;
import cc.mallet.types.*;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 15/01/13
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator {

    private int tp, fp, fn;
    private Model model;

    public Evaluator(final Model model) {
        this.model = model;
    }

    public void evaluate(Documents documents){

    }

    public void evaluate(final InstanceList instances) {
        tp = 0;
        fp = 0;
        fn = 0;

        NoopTransducerTrainer trainer = new NoopTransducerTrainer(model.getCRF());
        for (Instance instance : instances) {

            // Get gold
            LabelSequence goldLabels = (LabelSequence) instance.getTarget();
            List<Trigger> goldEntities = getEntities(goldLabels);

            // Get silver
            Sequence input = (Sequence) instance.getData();
            Transducer tran = trainer.getTransducer();
            Sequence pred = tran.transduce(input);
            List<Trigger> silverEntities = getEntities(pred);


            //Silver on Gold
            for (Trigger gold : goldEntities) {
                if (!silverEntities.contains(gold)) {
                    fn++;
                }
            }

            // Gold on Silver
            for (Trigger silver : silverEntities) {
                if (!goldEntities.contains(silver)) {
                    fp++;
                } else {
                    tp++;
                }
            }
        }
    }

    public double getPrecision() {
        if (tp == 0 && fp == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fp)) * 100.0;
    }

    public double getRecall() {
        if (tp == 0 && fn == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fn)) * 100.0;
    }

    public double getF1() {
        double p = getPrecision();
        double r = getRecall();

        if (p == 0 && r == 0) {
            return 0.0;
        }

        return 2.0 * ((p * r) / (p + r));
    }

    private List<Trigger> getEntities(Sequence sequence) {
        List<Trigger> entities = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            String l1 = sequence.get(i).toString();

            if (!l1.equals("O")) {
                int start = i;
                int end = i;
                for (int j = i + 1; j < sequence.size(); j++) {
                    String l2 = sequence.get(j).toString();
                    if (!l2.equals(l1)) {
                        break;
                    }
                    end = j;
                    i++;
                }

                entities.add(new Trigger(start, end, l1));
            }
        }
        return entities;
    }


}
