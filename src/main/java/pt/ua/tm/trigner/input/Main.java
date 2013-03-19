package pt.ua.tm.trigner.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.documents.Documents;

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
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {


//        String inputFolder = "resources/corpus/bionlp2013/cg/dev/";
        String gdepPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep";

        String inputFolder = "resources/corpus/bionlp2009/dev/gold";
        String outputFile = "resources/corpus/bionlp2009/dev/documents.gz";
        int numThreads = 4;

//        String inputFolder = "/Users/david/Downloads/tmp/";
//        String outputFile = "/Users/david/Downloads/tmp/documents.gz";


        Documents documents = DocumentsLoader.load(inputFolder, gdepPath, numThreads);

        try {
            documents.write(new GZIPOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException e) {
            logger.error("ERROR: ", e);
        }

    }
}
