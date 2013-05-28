package pt.ua.tm.trigner.model.transformer;

import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.VertexFeatureType;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeatureTransformer implements Transformer<String, Types.VertexFeatureType> {
    @Override
    public Types.VertexFeatureType transform(String input) {
        return Types.VertexFeatureType.valueOf(input);
    }
}
