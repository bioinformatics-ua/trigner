package pt.ua.tm.trigner.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/22/13
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Configuration {
    void read(final InputStream inputStream) throws IOException;

    void write(final OutputStream outputStream) throws IOException;
}
