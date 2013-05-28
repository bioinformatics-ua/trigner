package pt.ua.tm.trigner.cli.train;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import cc.mallet.util.MalletLogger;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.logger.LoggingOutputStream;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.convert.DocumentsLoader;
import pt.ua.tm.trigner.model.Documents2InstancesConverter;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.model.ModelFeaturePipeline;
import pt.ua.tm.trigner.model.ProcessingFeaturePipeline;
import pt.ua.tm.trigner.configuration.ModelConfiguration;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/23/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Project {
    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./train.sh project ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition" +
            "\nTRAIN EVENT";
    private static final String USAGE = "-i <file|folder> "
            + "-o <folder> "
            + "-d <folder> "
            + "-c <folder> "
            + "-pc <project-configuration> "
            + "[-t <threads>]"
            + "[-v]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-folder -o output-folder -c configuration-folder -d dictionary-folder -pc configuration.json\n"
            + "2: "
            + TOOLPREFIX + "-i input-file.gz -o output-folder -c configuration-folder -d dictionary-folder -pc configuration.json\n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";

    private static Logger logger = LoggerFactory.getLogger(Event.class);

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

        options.addOption("i", "input", true, "Input folder with text, a1 and a2 files or the file resulting from the \"convert.sh\" script.");
        options.addOption("o", "output", true, "Output folder to store trained models.");
        options.addOption("c", "configuration", true, "Folder with model configuration files.");
        options.addOption("d", "dictionary", true, "Folder with dictionary files.");
        options.addOption("g", "gdep", true, "Folder that contains GDep.");
        options.addOption("pc", "project-configuration", true, "Annotate configuration JSON file.");

        options.addOption("t", "threads", true,
                "Number of threads. By default, if more than one core is available, it is half the number of cores.");

        options.addOption("v", "verbose", false, "Verbose mode.");

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
        String inputPath = null;
        if (commandLine.hasOption('i')) {
            inputPath = commandLine.getOptionValue('i');
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(inputPath);
        boolean inputIsDirectory = test.isDirectory();
        if (inputIsDirectory && (!test.isDirectory() || !test.canRead())) {
            logger.error("The specified path is not a folder or is not readable: {}", test.getAbsolutePath());
            return;
        }
        inputPath = test.getAbsolutePath();
        inputPath += File.separator;


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
            logger.error("The specified path is not a directory or writable: {}", test.getAbsolutePath());
            return;
        }
        outputFolderPath = test.getAbsolutePath();
        outputFolderPath += File.separator;


        // Get model configuration file
        String modelConfigurationFolderPath;
        if (commandLine.hasOption('c')) {
            modelConfigurationFolderPath = commandLine.getOptionValue('c');
        } else {
            printHelp(options, "Please specify the model configuration folder.");
            return;
        }
        test = new File(modelConfigurationFolderPath);
        if (!test.isDirectory() || !test.canRead()) {
            logger.error("The specified path is not a directory or readable: {}", test.getAbsolutePath());
            return;
        }
        modelConfigurationFolderPath = test.getAbsolutePath();
        modelConfigurationFolderPath += File.separator;

        // Get dictionary file
        String dictionaryFolderPath;
        if (commandLine.hasOption('d')) {
            dictionaryFolderPath = commandLine.getOptionValue('d');
        } else {
            printHelp(options, "Please specify the dictionary folder.");
            return;
        }
        test = new File(dictionaryFolderPath);
        if (!test.isDirectory() || !test.canRead()) {
            logger.error("The specified path is not a directory or readable: {}", test.getAbsolutePath());
            return;
        }
        dictionaryFolderPath = test.getAbsolutePath();
        dictionaryFolderPath += File.separator;


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

        // Get verbose mode
        boolean verbose = false;
        if (commandLine.hasOption('v')) {
            verbose = true;
        }
        Constants.verbose = verbose;
        if (Constants.verbose) {
            MalletLogger.getGlobal().setLevel(Level.INFO);
            // Redirect sout
            LoggingOutputStream los = new LoggingOutputStream(LoggerFactory.getLogger("stdout"), false);
            System.setOut(new PrintStream(los, true));

            // Redirect serr
            los = new LoggingOutputStream(LoggerFactory.getLogger("sterr"), true);
            System.setErr(new PrintStream(los, true));

            // Redirect JUL to SLF4
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } else {
            LogManager.getLogManager().reset();
        }

        // Load input documents
        Documents trainDocuments;
        try {
            if (inputIsDirectory) {
                logger.info("Converting input files from folder: {}", inputPath);
                trainDocuments = DocumentsLoader.load(inputPath, gdepFolderPath, NUM_THREADS);
            } else {
                logger.info("Loading train documents from compressed file: {}", inputPath);
                trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(inputPath)));
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("There was a problem loading input data: " + projectConfigurationFilePath, ex);
            return;
        }
        logger.info("Number of input documents: {}", trainDocuments.size());

        String[] featuresToKeep = new String[]{"POS", "LEMMA"};

        for (String event : Global.projectConfiguration.getEvents()) {
            logger.info("Training model for " + event + "...");
            File modelConfigurationFile = new File(modelConfigurationFolderPath, event + ".config");
            File dictionaryFile = new File(dictionaryFolderPath, event + ".txt");

            ModelConfiguration modelConfiguration = new ModelConfiguration();
            // Load model configuration
            try {

                modelConfiguration.load(new FileInputStream(modelConfigurationFile));
            } catch (IOException e) {
                logger.error("There was a problem loading the model configuration file: " + modelConfigurationFile.getAbsolutePath(), e);
                return;
            }

            // Pre-processing features
            ProcessingFeaturePipeline.get(modelConfiguration).run(trainDocuments);

            // Model features
            Pipe pipe = ModelFeaturePipeline.get(modelConfiguration, dictionaryFile.getAbsolutePath());

            // Get Annotate instances
            InstanceList train = Documents2InstancesConverter.getInstanceList(trainDocuments, pipe, event);

            // Train model

            Model model = new Model(modelConfiguration);
            model.train(train);

            // Write model
            File outputFile = new File(outputFolderPath, event + ".gz");
            try {
                model.write(new GZIPOutputStream(new FileOutputStream(outputFile.getAbsolutePath())));
            } catch (GimliException | IOException e) {
                logger.error("There was a problem writing the model to the file: " + outputFile.getAbsolutePath(), e);
                return;
            }

            // Remove features from documents
            trainDocuments.cleanFeatures(featuresToKeep);

            // Suggest garbage collection
            System.gc();
        }
        logger.info("Done.");
    }
}
