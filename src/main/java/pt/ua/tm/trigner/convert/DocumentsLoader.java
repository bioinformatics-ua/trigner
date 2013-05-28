package pt.ua.tm.trigner.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.gimli.external.gdep.GDepParser.ParserLevel;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.documents.Documents;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 01/03/13
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class DocumentsLoader {

    private static Logger logger = LoggerFactory.getLogger(DocumentsLoader.class);

    public static Documents load(final String inputFolderPath, final int numThreads) {
        return load(inputFolderPath, null, numThreads);
    }

    public static Documents load(final String inputFolderPath, final String gdepPath, final int numThreads) {

        Context context = (gdepPath == null)
                ? new Context(null, null, ParserLevel.DEPENDENCY, false)
                : new Context(null, null, gdepPath, GDepParser.ParserLevel.DEPENDENCY, false);

        FolderBatch batch = new FolderBatch(inputFolderPath, numThreads);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }

        return new Documents(batch.getProcessedCorpora());
    }
}
