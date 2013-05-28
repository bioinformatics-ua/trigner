package pt.ua.tm.trigner.model.transformer;

import pt.ua.tm.trigner.configuration.ModelConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContextTransformer implements Transformer<String, ModelConfiguration.ContextType> {
    @Override
    public ModelConfiguration.ContextType transform(String input) {
        return ModelConfiguration.ContextType.valueOf(input);
    }
}
