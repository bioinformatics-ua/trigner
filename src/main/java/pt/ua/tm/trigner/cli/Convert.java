package pt.ua.tm.trigner.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.input.DocumentsLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 20/12/12
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class Convert {

    private static Logger logger = LoggerFactory.getLogger(Convert.class);

    public static void main(String... args) {


//        String inputFolder = "resources/corpus/bionlp2013/cg/dev/";
        String gdepPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep";

//        String inputFolder = "resources/corpus/bionlp2013/dev/";
//        String outputFile = "resources/corpus/bionlp2013/dev.gz";

//        String inputFolder = "resources/corpus/bionlp2013/dev/";
//        String outputFile = "resources/corpus/bionlp2013/dev.gz";

        int numThreads = 1;

        String inputFolder = "/Users/david/Downloads/test/input/";
        String outputFile = "/Users/david/Downloads/test/documents.gz";


        Documents documents = DocumentsLoader.load(inputFolder, gdepPath, numThreads);

        try {
            documents.write(new GZIPOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException e) {
            logger.error("ERROR: ", e);
        }

    }
}
