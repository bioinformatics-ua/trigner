package pt.ua.tm.trigner;

import cc.mallet.fst.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.tsf.*;
import cc.mallet.types.InstanceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.features.*;
import pt.ua.tm.gimli.model.CRFBase;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/01/13
 * Time: 09:09
 * To change this template use File | Settings | File Templates.
 */
public class Model {
    private static Logger logger = LoggerFactory.getLogger(Model.class);
    private static String CAPS = "[A-Z]";
    private static String LOW = "[a-z]";

    private CRF crf;

    private static final String[] allowedTags = new String[]{"Gene_expression", "Transcription", "Protein_catabolism",
            "Phosphorylation", "Localization", "Binding", "Regulation", "Positive_regulation",
            "Negative_regulation"};


    public Model() {
        this.crf = null;
    }

    public Pipe getFeaturePipe() {
        ArrayList<Pipe> pipe = new ArrayList<>();

        // Input parsing
        pipe.add(new Input2TokenSequence());

        // Capitalization
        pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
        pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
        pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
        pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
        pipe.add(new MixCase());
        pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));

        // Couting
        pipe.add(new NumberOfCap());
        pipe.add(new NumberOfDigit());
        pipe.add(new WordLength());

        // Symbols
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

        // Char n-gram
        pipe.add(new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}));

        // Suffixes
        pipe.add(new TokenTextCharSuffix("2SUFFIX=", 2));
        pipe.add(new TokenTextCharSuffix("3SUFFIX=", 3));
        pipe.add(new TokenTextCharSuffix("4SUFFIX=", 4));

        // Prefixes
        pipe.add(new TokenTextCharPrefix("2PREFIX=", 2));
        pipe.add(new TokenTextCharPrefix("3PREFIX=", 3));
        pipe.add(new TokenTextCharPrefix("4PREFIX=", 4));

        // Word shape
        pipe.add(new WordShape());

        // Conjunctions
//        pipe.add(new OffsetConjunctions(true, Pattern.compile("TOKEN=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
//        pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));

        // WINDOW
//        pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//        pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//        pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//        pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));
//        pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, Pattern.compile("(LEMMA|POS|CHUNK)=.*"), true));

        pipe.add(new FeaturesInWindow("WINDOW=", 3, 3));

//        pipe.add(new PrintTokenSequenceFeatures());

        pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        return new SerialPipes(pipe);
    }

    public void train(InstanceList instances) {

        logger.info("Train size: {}", instances.size());

        // Temporary split for faster training
        InstanceList[] split = instances.splitInOrder(new double[]{0.15, 0.85});
        instances = split[0];


        logger.info("Selected train size: {}", instances.size());

        // Define CRF
        int order = 2;
        int[] orders = new int[order];
        for (int i = 0; i < order; i++) {
            orders[i] = i;
        }

        crf = new CRF(instances.getPipe(), (Pipe) null);
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

        int numThreads = 6;
        CRFTrainerByThreadedLabelLikelihood crfTrainer = new CRFTrainerByThreadedLabelLikelihood(crf, numThreads);
        crfTrainer.train(instances);
        crfTrainer.shutdown();

        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{instances},
                new String[]{"train"}, allowedTags, allowedTags) {
        };
        evaluator.evaluate(crfTrainer);
    }

    public void test(InstanceList instances) {
        if (crf == null) {
            throw new RuntimeException("The CRF model was not trained or loaded yet.");
        }

        // Define Evaluator
        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{instances},
                new String[]{"test"}, allowedTags, allowedTags) {
        };

        // Evaluate
        evaluator.evaluateInstanceList(new NoopTransducerTrainer(crf), instances, "test");
    }

    public void write(OutputStream outputStream) {
        if (crf == null) {
            throw new RuntimeException("The CRF model was not trained or loaded yet.");
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(crf);
            oos.close();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem writing the model.", ex);
        }
    }

    public void read(InputStream inputStream) {
        CRF crf = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            crf = (CRF) ois.readObject();
            ois.close();
            inputStream.close();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Provided model is not in CRF format.", ex);
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the CRF model.", ex);
        }
        this.crf = crf;
    }
}
