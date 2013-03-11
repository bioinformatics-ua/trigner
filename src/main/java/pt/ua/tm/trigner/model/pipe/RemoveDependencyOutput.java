package pt.ua.tm.trigner.model.pipe;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class RemoveDependencyOutput extends Pipe {

    public RemoveDependencyOutput() {
    }

    @Override
    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();

        for (Token token : ts) {

            PropertyList pl = token.getFeatures();
            PropertyList.Iterator it = pl.iterator();

            while (it.hasNext()) {
                String feature = it.nextProperty().iterator().getKey();
                if (feature.contains("DEP_TOK=")) {
                    pl = PropertyList.remove(feature, pl);
                }
                if (feature.contains("DEP_TAG=")) {
                    pl = PropertyList.remove(feature, pl);
                }

            }
            token.setFeatures(pl);
        }
        return carrier;
    }
}
