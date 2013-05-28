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
 * Date: 07/04/13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class Split {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./split.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition (SPLIT)";
    private static final String USAGE = "-i <file> "
            + "-r <ratio> "
            + "-o1 <file> "
            + "-o2 <file> ";

    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-file -r ratio -o1 output-file.gz -o2 output-file.gz \n\n";
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

        options.addOption("i", "input", true, "Input compressed file resulting from the \"convert.sh\" script.");
        options.addOption("r", "ratio", true, "Ratio of the first part.");
        options.addOption("o1", "output1", true, "Path of the compressed file to store the first part.");
        options.addOption("o2", "output2", true, "Path of the compressed file to store the second part.");

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
        String inputFilePath = null;
        if (commandLine.hasOption('i')) {
            inputFilePath = commandLine.getOptionValue('i');
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(inputFilePath);
        if (!test.canRead()) {
            logger.error("The specified path is not readable: {}", test.getAbsolutePath());
            return;
        }
        inputFilePath = test.getAbsolutePath();
        inputFilePath += File.separator;

        String ratioText = null;
        if (commandLine.hasOption('r')) {
            ratioText = commandLine.getOptionValue('r');
        } else {
            printHelp(options, "Please specify the ratio.");
            return;
        }
        double ratio = Double.parseDouble(ratioText);
        if (ratio >= 1.0 || ratio <= 0.0) {
            logger.error("The ratio must be between 0 and 1, exclusive.");
            return;
        }


        // Get output file
        String outputFilePath1;
        if (commandLine.hasOption("o1")) {
            outputFilePath1 = commandLine.getOptionValue("o1");
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }
        test = new File(outputFilePath1);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFilePath1 = test.getAbsolutePath();
        outputFilePath1 += File.separator;

        // Get output file
        String outputFilePath2;
        if (commandLine.hasOption("o2")) {
            outputFilePath2 = commandLine.getOptionValue("o2");
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }
        test = new File(outputFilePath2);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFilePath2 = test.getAbsolutePath();
        outputFilePath2 += File.separator;


        Documents documents;

        try {
            logger.info("Loading input documents from file: {}", inputFilePath);
            documents = Documents.read(new GZIPInputStream(new FileInputStream(inputFilePath)));
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("There was a problem loading the input documents: {}", inputFilePath, ex);
            return;
        }

        // Perform split
        logger.info("Splitting input documents...");
        Documents[] docs = documents.splitRandom(new double[]{ratio, 1.0 - ratio});

        logger.info("Number of documents of Part 1: {}", docs[0].size());
        logger.info("Number of documents of Part 2: {}", docs[1].size());

        // Write part1
        try {
            logger.info("Writing output part 1 to file: {}", outputFilePath1);
            docs[0].write(new GZIPOutputStream(new FileOutputStream(outputFilePath1)));
        } catch (IOException ex) {
            logger.error("There was a problem writing the output part 1 in the file: {}", outputFilePath1, ex);
            return;
        }

        // Write part2
        try {
            logger.info("Writing output part 2 to file: {}", outputFilePath2);
            docs[1].write(new GZIPOutputStream(new FileOutputStream(outputFilePath2)));
        } catch (IOException ex) {
            logger.error("There was a problem writing the output part 2 in the file: {}", outputFilePath2, ex);
            return;
        }

        logger.info("Done.");
    }
}
