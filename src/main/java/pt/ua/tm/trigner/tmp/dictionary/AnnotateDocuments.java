package pt.ua.tm.trigner.tmp.dictionary;

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
public class AnnotateDocuments {

    private static Logger logger = LoggerFactory.getLogger(AnnotateDocuments.class);

    public static void main(String... args) {

//        String inputFolder = "resources/corpus/bionlp2011/dev/";
//        String outputFolder = "resources/corpus/bionlp2011/dev/silver/dictionaries/";
//        String dictionariesFolderPath = "resources/dictionaries/annotation/";

        String inputFolder = "resources/corpus/bionlp2013/cg/dev/";
        String outputFolder = "resources/corpus/bionlp2013/cg/dev/silver/dictionaries/";
        String dictionariesFolderPath = "resources/dictionaries/cg/annotation/";

        String parserPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep/gdep_gimli";

        int numThreads = 1;

        Context context = new Context(null, dictionariesFolderPath, parserPath, GDepParser.ParserLevel.TOKENIZATION, false);

        FolderBatch batch = new FolderBatch(inputFolder, outputFolder, numThreads);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }

    }
}
