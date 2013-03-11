package pt.ua.tm.trigner.output;

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
//        String outputFolder = "resources/corpus/bionlp2011/dev/silver/ml/";
//        String modelsFolderPath = "resources/models/bionlp2011/";

        // TEST
//        String inputFolder = "resources/corpus/bionlp2013/cg/dev/";
//        String outputFolder = "resources/corpus/bionlp2013/cg/dev/silver/ml/";

        // TRAIN
        String inputFolder = "resources/corpus/bionlp2013/cg/train/";
        String outputFolder = "resources/corpus/bionlp2013/cg/train/silver/ml/";

        String modelsFolderPath = "resources/models/bionlp2013/cg/";

        String parserPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep/gdep_gimli";
        int numThreads = 1;

        Context context = new Context(modelsFolderPath, null, parserPath, GDepParser.ParserLevel.DEPENDENCY, false);

        FolderBatch batch = new FolderBatch(inputFolder, outputFolder, numThreads);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }

    }
}
