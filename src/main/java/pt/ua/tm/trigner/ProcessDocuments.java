package pt.ua.tm.trigner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.neji.context.Context;
import pt.ua.tm.neji.exception.NejiException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 20/12/12
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class ProcessDocuments {

    private static Logger logger = LoggerFactory.getLogger(A2Loader.class);

    public static void main(String... args) {

        String inputFolder = "resources/corpus/bionlp2009/dev/";
        String outputFolder = "resources/corpus/bionlp2009/dev/conll/";
        String parserPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep/gdep_gimli";
        int numThreads = 4;

        Context context = new Context(null, null, parserPath, GDepParser.ParserLevel.CHUNKING, false);

        FolderBatch batch = new FolderBatch(inputFolder, outputFolder, numThreads);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }

    }
}
