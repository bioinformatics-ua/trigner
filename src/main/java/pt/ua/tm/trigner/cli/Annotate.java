package pt.ua.tm.trigner.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.annotate.Context;
import pt.ua.tm.trigner.annotate.FolderBatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 20/12/12
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
public class Annotate {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./annotate.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition" +
            "\nANNOTATE";
    private static final String USAGE = "-i <folder> "
            + "-o <folder> "
            + "-c <folder> "
            + "-g <folder> "
            + "[-d <folder>] [-m <folder>] "
            + "-pc <project-configuration> "
            + "[-t <threads>]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -d folder -pc configuration.json -t 6\n"
            + "2: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -m folder -pc configuration.json -t 4\n"
            + "3: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -d folder1 -m folder2 -pc configuration.json -t 4\n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";

    private static Logger logger = LoggerFactory.getLogger(Annotate.class);

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

        options.addOption("i", "input", true, "Folder with corpus files.");
        options.addOption("o", "output", true, "Folder to save the annotated corpus files.");
        options.addOption("c", "concept", true, "Folder that contains A1 files with concept annotations.");

        Option o = new Option("m", "models", true, "Folder that contains the ML models.");
        o.setArgs(Integer.MAX_VALUE);
        options.addOption(o);

        options.addOption("d", "dictionaires", true, "Folder that contains the dictionaries.");

        options.addOption("g", "gdep", true, "Folder that contains GDep.");

        options.addOption("t", "threads", true,
                "Number of threads. By default, if more than one core is available, it is half the number of cores.");
        options.addOption("pc", "project-configuration", true, "Annotate configuration JSON file..");

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
        String outputFolderPath;
        if (commandLine.hasOption('o')) {
            outputFolderPath = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }
        test = new File(outputFolderPath);
        if (!test.isDirectory() || !test.canWrite()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFolderPath = test.getAbsolutePath();
        outputFolderPath += File.separator;


        // Get concept folder
        String conceptFolderPath;
        if (commandLine.hasOption('c')) {
            conceptFolderPath = commandLine.getOptionValue('c');
        } else {
            printHelp(options, "Please specify the concept folder.");
            return;
        }
        test = new File(conceptFolderPath);
        if (!test.isDirectory() || !test.canWrite()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        conceptFolderPath = test.getAbsolutePath();
        conceptFolderPath += File.separator;


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


        // Get models
        String modelsFolderPath = null;
        if (commandLine.hasOption('m')) {
            modelsFolderPath = commandLine.getOptionValue('m');

            test = new File(modelsFolderPath);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
                return;
            }
            modelsFolderPath = test.getAbsolutePath();
            modelsFolderPath += File.separator;
        }

        // Get dictionaries folder
        String dictionariesFolderPath = null;
        if (commandLine.hasOption('d')) {
            dictionariesFolderPath = commandLine.getOptionValue('d');

            test = new File(dictionariesFolderPath);
            if (!test.isDirectory() || !test.canRead()) {
                logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
                return;
            }
            dictionariesFolderPath = test.getAbsolutePath();
            dictionariesFolderPath += File.separator;
        }

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

        // Get project configuration file path
        String projectConfigurationFilePath;
        if (commandLine.hasOption("pc")) {
            projectConfigurationFilePath = commandLine.getOptionValue("pc");
        } else {
            printHelp(options, "Please specify the project configuration file.");
            return;
        }

        // Set project configuration
        try {
            Global.projectConfiguration.read(new FileInputStream(projectConfigurationFilePath));
        } catch (IOException e) {
            logger.error("There was a problem loading the project configuration JSON file: " + projectConfigurationFilePath, e);
            return;
        }


        GDepParser.ParserLevel parserLevel;
        if (modelsFolderPath != null) {
            parserLevel = GDepParser.ParserLevel.DEPENDENCY;
        } else {
            parserLevel = GDepParser.ParserLevel.TOKENIZATION;
        }

        Context context = new Context(modelsFolderPath, dictionariesFolderPath, gdepFolderPath, parserLevel);
        FolderBatch batch = new FolderBatch(inputFolderPath, outputFolderPath, conceptFolderPath, NUM_THREADS);
        try {
            batch.run(context);
        } catch (NejiException e) {
            logger.error("ERROR: ", e);
        }
    }
}
