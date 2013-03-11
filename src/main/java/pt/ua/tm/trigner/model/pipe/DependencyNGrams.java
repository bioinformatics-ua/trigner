package pt.ua.tm.trigner.model.pipe;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class DependencyNGrams extends Pipe {

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        List<Dependency> dependencies = Dependency.getDependencies(ts);

        for (int i = 0; i < ts.size(); i++) {
            Token token = ts.get(i);
            Dependency dependency = dependencies.get(i);
        }

        return carrier;
    }
}
