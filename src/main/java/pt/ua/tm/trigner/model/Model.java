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
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.features.*;
import pt.ua.tm.gimli.model.CRFBase;
import pt.ua.tm.trigner.model.pipe.DependencyFeaturePath;
import pt.ua.tm.trigner.model.pipe.DependencyPath;
import pt.ua.tm.trigner.model.pipe.DependencyWindow;
import pt.ua.tm.trigner.model.pipe.RemoveDependencyOutput;

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

    public Model(final ModelConfig config) {
        super(config, Constants.Parsing.FW);
    }

    @Deprecated
    public Pipe getFeaturePipe() {
        throw new UnsupportedOperationException("Use Pipe getFeaturePipe(String dictionaryPath) instead.");
    }

    public Pipe getFeaturePipe(final String dictionaryPath) {
        ArrayList<Pipe> pipe = new ArrayList<>();
        ModelConfig config = getConfig();

        // Input parsing
        pipe.add(new pt.ua.tm.trigner.model.pipe.Input2TokenSequence(config));

        if (config.isVerbs()) {
//            File file = new File("resources/dictionaries/triggers.txt");
            File file = new File(dictionaryPath);
            try {
                pipe.add(new TrieLexiconMembership("TRIGGER", file, true));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("There was a problem reading the dictionary for triggers matching: " + file.getName(), ex);
            }
        }

        // Capitalization
        if (config.isCapitalization()) {
            pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
            pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
            pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
            pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
            pipe.add(new MixCase());
            pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
        }

        // Counting
        if (config.isCounting()) {
            pipe.add(new NumberOfCap());
            pipe.add(new NumberOfDigit());
            pipe.add(new WordLength());
        }

        // Symbols
        if (config.isSymbols()) {
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
        if (config.isNgrams()) {
//            pipe.add(new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}));
            pipe.add(new TokenTextCharNGrams("CHARNGRAM=", new int[]{3, 4}));
        }

        // Suffixes
        if (config.isSuffix()) {
//            pipe.add(new TokenTextCharSuffix("2SUFFIX=", 2));
            pipe.add(new TokenTextCharSuffix("3SUFFIX=", 3));
            pipe.add(new TokenTextCharSuffix("4SUFFIX=", 4));
        }

        // Prefixes
        if (config.isPrefix()) {
//            pipe.add(new TokenTextCharPrefix("2PREFIX=", 2));
            pipe.add(new TokenTextCharPrefix("3PREFIX=", 3));
            pipe.add(new TokenTextCharPrefix("4PREFIX=", 4));
        }

        // Word shape
        if (config.isMorphology()) {
            pipe.add(new WordShape());
        }


        pipe.add(new DependencyWindow("DEP_WINDOW", 3, Pattern.compile("(LEMMA|POS|CHUNK)=.*")));
//        pipe.add(new DependencyFeaturePath("1DEP_PATH_WORD", 1, Pattern.compile("WORD=.*")));
//        pipe.add(new DependencyFeaturePath("2DEP_PATH_WORD", 2, Pattern.compile("WORD=.*")));
//        pipe.add(new DependencyFeaturePath("2DEP_PATH_LEMMA", 2, Pattern.compile("LEMMA=.*")));
//        pipe.add(new DependencyFeaturePath("2DEP_PATH_POS", 2, Pattern.compile("POS=.*")));
//        pipe.add(new DependencyFeaturePath("2DEP_PATH_TAG", 2, Pattern.compile("DEP_TAG=.*")));
//        pipe.add(new DependencyFeaturePath("2DEP_PATH_CHUNK", 2, Pattern.compile("CHUNK=.*")));
//        pipe.add(new DependencyFeaturePath("3DEP_PATH_WORD", 3, Pattern.compile("WORD=.*")));
        pipe.add(new DependencyFeaturePath("3DEP_PATH_LEMMA", 3, Pattern.compile("LEMMA=.*")));
        pipe.add(new DependencyPath("DEP_PATH", 3, DependencyPath.DependencyPathInfo.TAG));
        pipe.add(new DependencyPath("DEP_PATH", 3, DependencyPath.DependencyPathInfo.COMPOSITE));
        pipe.add(new DependencyPath("DEP_PATH", 3, DependencyPath.DependencyPathInfo.LEMMA));

        pipe.add(new RemoveDependencyOutput());

        // Conjunctions
        if (config.isConjunctions()) {
            pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
            pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
//            pipe.add(new OffsetConjunctions(true, new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
        }


        // WINDOW
        if (config.isWindow()) {
//            pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//            pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//            pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//            pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//            pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));

            pipe.add(new FeaturesInWindow("WINDOW=", -3, 3));
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
