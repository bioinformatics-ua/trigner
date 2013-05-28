package pt.ua.tm.trigner.cli.optimize;

import cc.mallet.util.MalletLogger;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.logger.LoggingOutputStream;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.convert.DocumentsLoader;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.optimization.Optimization;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 07/04/13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class Event {
    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./optimize.sh event ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition (OPTIMIZE)";
    private static final String USAGE = "-t <file|folder> "
            + "[-d <file|folder>] "
            + "-pc <project-configuration> "
            + "-oc <optimization-configuration> "
            + "-e <event> "
            + "-g <folder> "
            + "[-t <threads>]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -d folder -pc configuration.json -t 6\n"
            + "2: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -m folder -pc configuration.json -t 4\n"
            + "3: "
            + TOOLPREFIX + "-i input -o output -c concept -g gdep -d folder1 -m folder2 -pc configuration.json -t 4\n\n";
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

        options.addOption("train", "train", true, "Train documents folder with text, a1 and a2 files or the file resulting from the \"convert.sh\" script.");
        options.addOption("dev", "development", true, "Development documents folder with text, a1 and a2 files or the file resulting from the \"convert.sh\" script.");

        options.addOption("o", "output", true, "Output file to store the optimized model configuration.");

        options.addOption("d", "dictionary", true, "Dictionary file.");
        options.addOption("g", "gdep", true, "Folder that contains GDep.");
        options.addOption("pc", "project-configuration", true, "WritePriority configuration JSON file.");
        options.addOption("oc", "optimize-configuration", true, "Optimization configuration JSON file.");
        options.addOption("e", "event", true, "Target training event.");

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

        // Get train
        String trainPath = null;
        if (commandLine.hasOption("train")) {
            trainPath = commandLine.getOptionValue("train");
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(trainPath);
        boolean trainIsDirectory = test.isDirectory();
        if (trainIsDirectory && (!test.isDirectory() || !test.canRead())) {
            logger.error("The specified path is not a folder or is not readable: {}", test.getAbsolutePath());
            return;
        }
        trainPath = test.getAbsolutePath();
        trainPath += File.separator;

        // Get train
        String devPath = null;
        if (commandLine.hasOption("dev")) {
            devPath = commandLine.getOptionValue("dev");
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(devPath);
        boolean developmentIsDirectory = test.isDirectory();
        if (developmentIsDirectory && (!test.isDirectory() || !test.canRead())) {
            logger.error("The specified path is not a folder or is not readable: {}", test.getAbsolutePath());
            return;
        }
        devPath = test.getAbsolutePath();
        devPath += File.separator;


        // Get output file
        String outputFilePath;
        if (commandLine.hasOption('o')) {
            outputFilePath = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output file to store the optimized model configuration.");
            return;
        }
        test = new File(outputFilePath);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        outputFilePath = test.getAbsolutePath();
        outputFilePath += File.separator;

        // Get dictionary file
        String dictionaryFilePath;
        if (commandLine.hasOption('d')) {
            dictionaryFilePath = commandLine.getOptionValue('d');
        } else {
            printHelp(options, "Please specify the dictionary file.");
            return;
        }
        test = new File(dictionaryFilePath);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        dictionaryFilePath = test.getAbsolutePath();
        dictionaryFilePath += File.separator;


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

        // Get optimize configuration file path
        String optimizationConfigurationFilePath;
        if (commandLine.hasOption("oc")) {
            optimizationConfigurationFilePath = commandLine.getOptionValue("oc");
        } else {
            printHelp(options, "Please specify the optimize configuration file.");
            return;
        }

        // Set optimize configuration
        try {
            Global.optimizationConfiguration.read(new FileInputStream(optimizationConfigurationFilePath));
        } catch (IOException e) {
            logger.error("There was a problem loading the optimize configuration JSON file: " + optimizationConfigurationFilePath, e);
            return;
        }

        // Get target event
        String event;
        if (commandLine.hasOption("e")) {
            event = commandLine.getOptionValue("e");
        } else {
            printHelp(options, "Please specify the target event.");
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

        // Load train documents
        Documents trainDocuments;
        try {
            if (trainIsDirectory) {
                logger.info("Converting train files from folder: {}", trainPath);
                trainDocuments = DocumentsLoader.load(trainPath, gdepFolderPath, NUM_THREADS);
            } else {
                logger.info("Loading train documents from compressed file: {}", trainPath);
                trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(trainPath)));
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("There was a problem loading train data: " + trainPath, ex);
            return;
        }
        logger.info("Number of train documents: {}", trainDocuments.size());

        Documents devDocuments;
        try {
            if (trainIsDirectory) {
                logger.info("Converting development files from folder: {}", devPath);
                devDocuments = DocumentsLoader.load(devPath, gdepFolderPath, NUM_THREADS);
            } else {
                logger.info("Loading development documents from compressed file: {}", devPath);
                devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devPath)));
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("There was a problem loading development data: " + devPath, ex);
            return;
        }
        logger.info("Number of development documents: {}", devDocuments.size());

        // Run optimize
        ModelConfiguration bestModelConfiguration = Optimization.run(trainDocuments, devDocuments, dictionaryFilePath, event);

        // Write obtained model configuration
        String comments = "Event: " + event;
        try {
            bestModelConfiguration.store(new FileOutputStream(outputFilePath), comments);
        } catch (IOException ex) {
            logger.error("There was a problem writing the model configuration file in the file: " + outputFilePath, ex);
            return;
        }
    }
}
