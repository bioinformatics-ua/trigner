package pt.ua.tm.trigner.model.pipe;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 */
public class DependencyWindow extends Pipe {

    private int distance;
    private String prefix;
    private Pattern featureRegex;

    public DependencyWindow(final String prefix, final int distance) {
        this(prefix, distance, Pattern.compile(".*"));
    }

    public DependencyWindow(final String prefix, final int distance, final Pattern featureRegex) {
        this.prefix = prefix;
        this.distance = distance;
        this.featureRegex = featureRegex;
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        List<Dependency> dependencies = Dependency.getDependencies(ts);

        for (int i = 0; i < ts.size(); i++) {
            Token token = ts.get(i);
            Dependency dependency = dependencies.get(i);

            for (int currentStep = 0; currentStep < distance; currentStep++) {
                if (dependency.getPos() == -1) {
                    break;
                }

                Token t = ts.get(dependency.getPos());
                List<String> features = TokenUtil.getFeatures(t, featureRegex);

                for (String feature : features) {
                    feature = feature.substring(feature.indexOf("=") + 1);
                    token.setFeatureValue(prefix + "=" + feature, 1.0);
                }
                dependency = dependencies.get(dependency.getPos());
            }
        }
        return carrier;
    }
}
