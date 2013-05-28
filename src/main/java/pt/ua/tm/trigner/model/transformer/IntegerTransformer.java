package pt.ua.tm.trigner.model.transformer;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntegerTransformer implements Transformer<String, Integer> {
    @Override
    public Integer transform(String input) {
        return Integer.parseInt(input);
    }
}
