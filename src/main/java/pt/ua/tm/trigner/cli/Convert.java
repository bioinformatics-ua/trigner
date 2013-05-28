package pt.ua.tm.trigner.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.convert.DocumentsLoader;

import java.io.File;
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

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./convert.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition" +
            "\nCONVERT";
    private static final String USAGE = "-i <folder> "
            + "-o <file> "
            + "-g <folder> "
            + "[-t <threads>]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-folder -o output-file.gz -g gdep-folder -t 2\n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";
    private static Logger logger = LoggerFactory.getLogger(Convert.class);

    /**
     * Print help message of the program.
     *
     * @param options Command line arguments.
     * @param msg     Message to be displayed.
     */
    private static void printHelp(final Options options, final String msg) {
        if (msg.length() != 0) {
            logger.error(msg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(150, TOOLPREFIX + USAGE, HEADER, options, EXAMPLES + FOOTER);
    }

    public static void main(String... args) {


        // Set default number of threads
        int NUM_THREADS = Runtime.getRuntime().availableProcessors() / 2;
        NUM_THREADS = NUM_THREADS > 0 ? NUM_THREADS : 1;

        String gdepFolderPath = "resources/tools/gdep";

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        options.addOption("i", "input", true, "Input folder with text, a1 and a2 files.");
        options.addOption("o", "output", true, "Compressed file to store processed output.");
        options.addOption("g", "gdep", true, "Folder that contains GDep.");

        options.addOption("t", "threads", true,
                "Number of threads. By default, if more than one core is available, it is half the number of cores.");

        CommandLine commandLine = null;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.", ex);
            return;
        }

        // Show help text
        if (commandLine.hasOption('h')) {
            printHelp(options, "");
            return;
        }

        // No options
        if (commandLine.getOptions().length == 0) {
            printHelp(options, "");
            return;
        }

        File test = null;

        // Get input folder
        String inputFolderPath = null;
        if (commandLine.hasOption('i')) {
            inputFolderPath = commandLine.getOptionValue('i');
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(inputFolderPath);
        if (!test.isDirectory() || !test.canRead()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        inputFolderPath = test.getAbsolutePath();
        inputFolderPath += File.separator;


        // Get output folder
        String outputFilePath;
        if (commandLine.hasOption('o')) {
            outputFilePath = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }
        test = new File(outputFilePath);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFilePath = test.getAbsolutePath();
        outputFilePath += File.separator;


        // Get GDep Folder
        if (commandLine.hasOption('g')) {
            gdepFolderPath = commandLine.getOptionValue('g');
        }
        test = new File(gdepFolderPath);
        if (!test.isDirectory() || !test.canWrite()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        gdepFolderPath = test.getAbsolutePath();
        gdepFolderPath += File.separator;

        // Get threads
        String threadsText = null;
        if (commandLine.hasOption('t')) {
            threadsText = commandLine.getOptionValue('t');
            NUM_THREADS = Integer.parseInt(threadsText);
            if (NUM_THREADS <= 0 || NUM_THREADS > 32) {
                logger.error("Illegal number of threads. Must be between 1 and 32.");
                return;
            }
        }


//        String inputFolder = "resources/corpus/bionlp2013/cg/dev/";
//        String gdepPath = "/Volumes/data/Dropbox/PhD/work/platform/code/neji/resources/tools/gdep";

//        String inputFolder = "resources/corpus/bionlp2013/dev/";
//        String outputFile = "resources/corpus/bionlp2013/dev.gz";

//        String inputFolder = "resources/corpus/bionlp2013/dev/";
//        String outputFile = "resources/corpus/bionlp2013/dev.gz";

//        int numThreads = 1;
//
//        String inputFolder = "/Users/david/Downloads/test/input/";
//        String outputFile = "/Users/david/Downloads/test/documents.gz";


        Documents documents = DocumentsLoader.load(inputFolderPath, gdepFolderPath, NUM_THREADS);

        try {
            documents.write(new GZIPOutputStream(new FileOutputStream(outputFilePath)));
        } catch (IOException e) {
            logger.error("ERROR: ", e);
        }

    }
}
