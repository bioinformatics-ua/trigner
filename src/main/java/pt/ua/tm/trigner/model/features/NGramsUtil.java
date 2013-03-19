package pt.ua.tm.trigner.model.features;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 19:22
 * To change this template use File | Settings | File Templates.
 */
public class NGramsUtil {

    public static List<String> getNGrams(final List<String> grams, final int n, final char concatenator) {

        List<String> ngrams = new ArrayList<>();

        // Build n-grams
        for (int i = 0; i < grams.size(); i++) {
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            for (int j = i; j < i + n && j < grams.size(); j++) {
                sb.append(grams.get(j));
                sb.append(concatenator);
                counter++;
            }

            if (counter < n) {
                continue;
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            // Add new ngram
            ngrams.add(sb.toString());
        }


        return ngrams;
    }

    public static int[] fromString(final String text) {
        String[] parts = text.split(",");

        int[] ngrams = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ngrams[i] = Integer.parseInt(parts[i]);
        }

        return ngrams;
    }

    public static String toString(final int[] ngrams) {
        return StringUtils.join(Arrays.asList(ngrams), ',');
    }
}
