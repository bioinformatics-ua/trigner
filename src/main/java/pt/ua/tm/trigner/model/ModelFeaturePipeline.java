package pt.ua.tm.trigner.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.*;
import pt.ua.tm.gimli.features.mallet.*;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.shared.CustomHashSet;
import pt.ua.tm.trigner.model.transformer.ContextTransformer;
import pt.ua.tm.trigner.model.transformer.IntegerTransformer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 19/03/13
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class ModelFeaturePipeline {

    private static final String CAPS = "[A-Z]";
    private static final String LOW = "[a-z]";

    public static Pipe get(final ModelConfiguration mc, final String dictionaryPath) {
        ArrayList<Pipe> pipe = new ArrayList<>();

        // Input parsing
        pipe.add(new pt.ua.tm.trigner.model.pipe.Input2TokenSequence(mc));

        // Capitalization
        if (mc.isProperty("Capitalization")) {
            pipe.add(new RegexMatches("InitCap", Pattern.compile(CAPS + ".*")));
            pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + CAPS)));
            pipe.add(new RegexMatches("AllCaps", Pattern.compile(CAPS + "+")));
            pipe.add(new RegexMatches("Lowercase", Pattern.compile(LOW + "+")));
            pipe.add(new MixCase());
            pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
        }

        // Counting
        if (mc.isProperty("Counting")) {
            pipe.add(new NumberOfCap());
            pipe.add(new NumberOfDigit());
            pipe.add(new WordLength());
        }

        // Symbols
        if (mc.isProperty("Symbols")) {
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
        if (mc.isProperty("CharNGrams")) {
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("CharNGrams_sizes"), new IntegerTransformer());
            for (Integer ngram : setGrams) {
                pipe.add(new TokenTextCharNGrams(ngram + "CHARNGRAM=", new int[]{ngram}));
            }
        }

        // Suffixes
        if (mc.isProperty("Suffix")) {
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("Suffix_sizes"), new IntegerTransformer());
            for (Integer ngram : setGrams) {
                pipe.add(new TokenTextCharSuffix(ngram + "SUFFIX=", ngram));
            }
        }

        // Prefixes
        if (mc.isProperty("Prefix")) {
            Set<Integer> setGrams = new CustomHashSet<>(mc.getProperty("Prefix_sizes"), new IntegerTransformer());
            for (Integer ngram : setGrams) {
                pipe.add(new TokenTextCharPrefix(ngram + "PREFIX=", ngram));
            }
        }

        // Word shape
        if (mc.isProperty("WordShape")) {
            pipe.add(new WordShape());
        }

        if (mc.isProperty("Triggers")) {
            File file = new File(dictionaryPath);
            try {
                pipe.add(new TrieLexiconMembership("TRIGGER", file, true));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("There was a problem reading the dictionary for triggers matching: " + file.getName(), ex);
            }
        }

        Set<ModelConfiguration.ContextType> contexts = new CustomHashSet<>(mc.getProperty("context"), new ContextTransformer());
        if (contexts.contains(ModelConfiguration.ContextType.WINDOW)) {
            //                pipe.add(new FeaturesInWindow("WINDOW=", -3, 3));


//                pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, Pattern.compile("(WORD|LEMMA|POS|Chunk)=.*"), true));
//                pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, Pattern.compile("(WORD|LEMMA|POS|Chunk)=.*"), true));
//                pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, Pattern.compile("(WORD|LEMMA|POS|Chunk)=.*"), true));
//                pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, Pattern.compile("(WORD|LEMMA|POS|Chunk)=.*"), true));
//                pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, Pattern.compile("(WORD|LEMMA|POS|Chunk)=.*"), true));

            String regex = "(WORD|LEMMA|POS|Chunk|DPModifiers|SP_EDGE_DISTANCE|SP_CHUNK_DISTANCE|ConceptTags|DPInOutDependencies_IN|DPInOutDependencies_OUT)=.*";
            Pattern pattern = Pattern.compile(regex);

            pipe.add(new FeaturesInWindow("WINDOW=", -1, 0, pattern, true));
            pipe.add(new FeaturesInWindow("WINDOW=", -2, -1, pattern, true));
            pipe.add(new FeaturesInWindow("WINDOW=", 0, 1, pattern, true));
            pipe.add(new FeaturesInWindow("WINDOW=", -1, 1, pattern, true));
            pipe.add(new FeaturesInWindow("WINDOW=", -3, -1, pattern, true));
        }

        if (contexts.contains(ModelConfiguration.ContextType.CONJUNCTIONS)) {
            pipe.add(new OffsetConjunctions(true, Pattern.compile("WORD=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
            pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
            pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
        }

        // Print
//        pipe.add(new PrintTokenSequenceFeatures());

        pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        return new SerialPipes(pipe);
    }
}
