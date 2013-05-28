package pt.ua.tm.trigner.model.transformer;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 3:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Transformer<I, O> {
    O transform(I input);
}
