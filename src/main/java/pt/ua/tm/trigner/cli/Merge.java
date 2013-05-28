package pt.ua.tm.trigner.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.documents.Documents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 06/04/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
public class Merge {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./merge.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition (MERGE)";
    private static final String USAGE = "-i <files> "
            + "-o <file> ";

    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-file1.gz input-file2.gz -o output-file.gz \n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";

    private static Logger logger = LoggerFactory.getLogger(Split.class);

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

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        Option option = new Option("i", "input", true, "Input compressed files resulting from the \"convert.sh\" script.");
        option.setArgs(Integer.MAX_VALUE);
        options.addOption(option);

        options.addOption("o", "output", true, "Path of the compressed file to store the merged result.");

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
        String[] inputFilesPath = null;
        if (commandLine.hasOption('i')) {
            inputFilesPath = commandLine.getOptionValues('i');
        } else {
            printHelp(options, "Please specify the input files.");
            return;
        }

        for (int i = 0; i < inputFilesPath.length; i++) {
            String inputFilePath = inputFilesPath[i];
            test = new File(inputFilePath);
            if (!test.canRead()) {
                logger.error("The specified file path is not readable: {}", test.getAbsolutePath());
                return;
            }
            inputFilePath = test.getAbsolutePath();
            inputFilePath += File.separator;
            inputFilesPath[i] = inputFilePath;
        }


        // Get output file
        String outputFilePath;
        if (commandLine.hasOption("o")) {
            outputFilePath = commandLine.getOptionValue("o");
        } else {
            printHelp(options, "Please specify the output file.");
            return;
        }
        test = new File(outputFilePath);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFilePath = test.getAbsolutePath();
        outputFilePath += File.separator;


        Documents merged = new Documents();

        // Loading and merging
        for (String inputFilePath : inputFilesPath) {
            try {
                logger.info("Loading documents from input file: {}", inputFilePath);
                Documents documents = Documents.read(new GZIPInputStream(new FileInputStream(inputFilePath)));
                logger.info("Loaded {} documents.", documents.size());
                merged.addAll(documents);
            } catch (IOException | ClassNotFoundException ex) {
                logger.error("There was a problem loading the documents from the file: {}", inputFilePath);
                return;
            }
        }

        logger.info("Number of merged documents: {}", merged.size());

        // Writing
        try {
            logger.info("Writing merged result to output file: {}", outputFilePath);
            merged.write(new GZIPOutputStream(new FileOutputStream(outputFilePath)));
        } catch (IOException ex) {
            logger.error("There was a problem writing the merged documents to the file: {}", outputFilePath);
            return;
        }

        logger.info("Done.");
    }
}
