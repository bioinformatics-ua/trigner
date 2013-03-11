package pt.ua.tm.trigner.model.pipe;

import cc.mallet.types.Token;
import cc.mallet.util.PropertyList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class TokenUtil {
    public static String getFeature(final Token token, final Pattern featureRegex) {
        PropertyList features = token.getFeatures();
        PropertyList.Iterator it = features.numericIterator();

        while (it.hasNext()) {
            String key = it.nextProperty().iterator().getKey();

            if (featureRegex.matcher(key).matches()) {
                return key;
            }
        }
        return null;
    }

    public static List<String> getFeatures(final Token token, final Pattern featureRegex) {
        PropertyList features = token.getFeatures();
        PropertyList.Iterator it = features.numericIterator();

        List<String> feats = new ArrayList<>();

        while (it.hasNext()) {
            String key = it.nextProperty().iterator().getKey();

            if (featureRegex.matcher(key).matches()) {
                feats.add(key);
            }
        }
        return feats;
    }
}
