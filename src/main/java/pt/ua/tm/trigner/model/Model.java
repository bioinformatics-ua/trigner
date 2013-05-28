package pt.ua.tm.trigner.model;

import cc.mallet.fst.*;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.model.CRFBase;
import pt.ua.tm.trigner.configuration.ModelConfiguration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/01/13
 * Time: 09:09
 * To change this template use File | Settings | File Templates.
 */
public class Model extends CRFBase {
    private static Logger logger = LoggerFactory.getLogger(Model.class);
    private String[] allowedTags;
    private ModelConfiguration mc;

    public Model(final ModelConfiguration mc, final InputStream inputStream) throws GimliException {
        super(null, Constants.Parsing.FW, inputStream);
        this.mc = mc;
    }

    public Model(final ModelConfiguration mc) {
        super(null, Constants.Parsing.FW);
        this.mc = mc;
    }

    public ModelConfiguration getModelConfiguration() {
        return mc;
    }

    @Deprecated
    public Pipe getFeaturePipe() {
        throw new UnsupportedOperationException("Use Pipe getFeaturePipe(String dictionaryPath) instead.");
    }

    private void printInstancesSizes(final InstanceList instances) {
        Map<String, Integer> counters = new HashMap<>();
        for (Instance instance : instances) {

            LabelSequence labelSequence = (LabelSequence) instance.getTarget();
            for (int i = 0; i < labelSequence.size(); i++) {
                Label label = labelSequence.getLabelAtPosition(i);
                String l = label.toString();

                if (counters.containsKey(l)) {
                    Integer count = counters.get(l);
                    counters.put(l, ++count);
                } else {
                    counters.put(l, 1);
                }
            }
        }

        for (String label : counters.keySet()) {
            logger.info("{}: {}", label, counters.get(label));
        }
    }

    @Override
    public void train(final Corpus corpus) throws GimliException {
        throw new UnsupportedOperationException("Use void train(InstanceList instances) instead.");
    }

    @Override
    public void test(final Corpus corpus) throws GimliException {
        throw new UnsupportedOperationException("Use void test(InstanceList instances) instead.");
    }

    private String[] getAllowedTagsFromInstances(final InstanceList instances) {
        Alphabet targetAlphabet = instances.getTargetAlphabet();
        String[] tags = new String[targetAlphabet.size() - 1];
        Iterator it = targetAlphabet.iterator();
        int j = 0;
        while (it.hasNext()) {
            String label = it.next().toString();
            if (label.equals("O")) {
                continue;
            }
            tags[j++] = label;
        }

        return tags;
    }

    public void train(InstanceList instances) {

        // Define allowed tags based on input instances+
        this.allowedTags = getAllowedTagsFromInstances(instances);

//        logger.info("Annotate size: {}", instances.size());
//        printInstancesSizes(instances);

        // Define CRF
//        int order = 2;
//        int order = getConfig().getOrder();
        int order = Integer.parseInt(mc.getProperty("model_order"));
        int[] orders = new int[order];
        for (int i = 0; i < order; i++) {
            orders[i] = i;
        }

        CRF crf = new CRF(instances.getPipe(), (Pipe) null);
        String startStateName = crf.addOrderNStates(
                instances,
                orders,
                null, // "defaults" parameter; see mallet javadoc
                "O",
                Pattern.compile(""),
                null,
                true); // true for a fully connected CRF

        for (int i = 0; i < crf.numStates(); i++) {
            crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
        }
        crf.getState(startStateName).setInitialWeight(0.0);
        crf.setWeightsDimensionAsIn(instances, false);

        int numThreads = 8;
        CRFTrainerByThreadedLabelLikelihood crfTrainer = new CRFTrainerByThreadedLabelLikelihood(crf, numThreads);
        crfTrainer.train(instances);
        crfTrainer.shutdown();

        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{instances},
                new String[]{"train"}, allowedTags, allowedTags) {
        };
        evaluator.evaluate(crfTrainer);
        setCRF(crf);

    }

    public void test(InstanceList instances) {
        CRF crf = getCRF();
        if (crf == null) {
            throw new RuntimeException("The CRF model was not trained or loaded yet.");
        }

        // Define Evaluator
        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{instances},
                new String[]{"test"}, allowedTags, allowedTags) {
        };

        // Evaluator
        evaluator.evaluateInstanceList(new NoopTransducerTrainer(crf), instances, "test");

    }
}
