package pt.ua.tm.trigner.shared;

import pt.ua.tm.trigner.model.transformer.Transformer;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/21/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomHashSet<T> extends HashSet<T> {

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object obj : this) {
            sb.append(obj.toString());
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public CustomHashSet() {
        super();
    }

    public CustomHashSet(final String string, final Transformer<String, T> transformer) {
        super();
        String[] parts = string.split(",");
        for (String part : parts) {
            add(transformer.transform(part));
        }
    }

    public CustomHashSet(final T[] array) {
        super();
        addAll(Arrays.asList(array));
    }
}
