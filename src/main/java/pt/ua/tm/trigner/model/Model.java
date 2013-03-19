package pt.ua.tm.trigner.model;

import cc.mallet.fst.*;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintTokenSequenceFeatures;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.*;
import cc.mallet.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.features.mallet.*;
import pt.ua.tm.gimli.model.CRFBase;
import pt.ua.tm.trigner.model.configuration.ModelConfiguration;
import pt.ua.tm.trigner.model.features.NGramsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    private static String CAPS = "[A-Z]";
    private static String LOW = "[a-z]";
    private String[] allowedTags;
    private ModelConfiguration mc;

    public Model(final ModelConfiguration mc) {
        super(null, Constants.Parsing.FW);
        this.mc = mc;
    }

    @Deprecated
    public Pipe getFeaturePipe() {
        throw new UnsupportedOperationException("Use Pipe getFeaturePipe(String dictionaryPath) instead.");
    }

    public Pipe getFeaturePipe(final String dictionaryPath) {
        ArrayList<Pipe> pipe = new ArrayList<>();

        // Input parsing
        pipe.add(new pt.ua.tm.trigner.model.pipe.Input2TokenSequence(mc));

        // Capitalization
        if (mc.isProperty("capitalization")) {
            pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
            pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
            pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
            pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
            pipe.add(new MixCase());
            pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
        }

        // Counting
        if (mc.isProperty("counting")) {
            pipe.add(new NumberOfCap());
            pipe.add(new NumberOfDigit());
            pipe.add(new WordLength());
        }

        // Symbols
        if (mc.isProperty("symbols")) {
            pipe.add(new RegexMatches("Hyphen", Pattern.compile(".*[-].*")));
            pipe.add(new RegexMatches("BackSlash", Pattern.compile(".*[/].*")));
            pipe.add(new RegexMatches("OpenSquare", Pattern.compile(".*[\\[].*")));
            pipe.add(new RegexMatches("CloseSquare", Pattern.compile(".*[\\]].*")));
            pipe.add(new RegexMatches("Colon", Pattern.compile(".*[:].*")));
            pipe.add(new RegexMatches("SemiColon", Pattern.compile(".*[;].*")));
            pipe.add(new RegexMatches("Percent", Pattern.compile(".*[%].*")));
            pipe.add(new RegexMatches("OpenParen", Pattern.compile(".*[(].*")));
            pipe.add(new RegexMatches("CloseParen", Pattern.compile(".*[)].*")));
            pipe.add(new RegexMatches("Comma", Pattern.compile(".*[,].*")));
            pipe.add(new RegexMatches("Dot", Pattern.compile(".*[\\.].*")));
            pipe.add(new RegexMatches("Apostrophe", Pattern.compile(".*['].*")));
            pipe.add(new RegexMatches("QuotationMark", Pattern.compile(".*[\"].*")));
            pipe.add(new RegexMatches("Star", Pattern.compile(".*[*].*")));
            pipe.add(new RegexMatches("Equal", Pattern.compile(".*[=].*")));
            pipe.add(new RegexMatches("Plus", Pattern.compile(".*[+].*")));
        }


        // Char n-gram
        if (mc.isProperty("char_ngrams")) {
            int[] ngrams = NGramsUtil.fromString(mc.getProperty("char_ngrams_sizes"));
            pipe.add(new TokenTextCharNGrams("CHARNGRAM=", ngrams));
        }

        // Suffixes
        if (mc.isProperty("suffix")) {
            int[] ngrams = NGramsUtil.fromString(mc.getProperty("suffix_sizes"));
            for (int ngram : ngrams) {
                pipe.add(new TokenTextCharSuffix(ngram + "SUFFIX=", ngram));
            }
        }

        // Prefixes
        if (mc.isProperty("prefix")) {
            int[] ngrams = NGramsUtil.fromString(mc.getProperty("prefix_sizes"));
            for (int ngram : ngrams) {
                pipe.add(new TokenTextCharSuffix(ngram + "PREFIX=", ngram));
            }

        }

        // Word shape
        if (mc.isProperty("word_shape")) {
            pipe.add(new WordShape());
        }

        if (mc.isProperty("triggers")) {
            File file = new File(dictionaryPath);
            try {
                pipe.add(new TrieLexiconMembership("TRIGGER", file, true));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("There was a problem reading the dictionary for triggers matching: " + file.getName(), ex);
            }
        }

        ModelConfiguration.ContextType context = ModelConfiguration.ContextType.valueOf(mc.getProperty("context"));
        switch (context) {
            case WINDOW:
                pipe.add(new FeaturesInWindow("WINDOW=", -3, 3));
                break;
            case CONJUNCTIONS:
                pipe.add(new OffsetConjunctions(true, Pattern.compile("WORD=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                break;
        }

        // Print
        pipe.add(new PrintTokenSequenceFeatures());

        pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        return new SerialPipes(pipe);
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

//        logger.info("Train size: {}", instances.size());
//        printInstancesSizes(instances);

        // Define CRF
//        int order = 2;
        int order = getConfig().getOrder();
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
