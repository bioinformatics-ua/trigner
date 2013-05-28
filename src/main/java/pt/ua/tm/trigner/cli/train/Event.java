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
 * Date: 08/04/13
 * Time: 22:45
 * To change this template use File | Settings | File Templates.
 */
public class Event {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./train.sh event ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition" +
            "\nTRAIN EVENT";
    private static final String USAGE = "-i <file|folder> "
            + "-o <file> "
            + "-c <file> "
            + "-d <file> "
            + "-pc <project-configuration> "
            + "-e <event> "
            + "[-t <threads>]"
            + "[-v]";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-folder -o output-file.gz -c configuration-file -d dictionary-file -pc configuration.json -e Gene_expression\n"
            + "2: "
            + TOOLPREFIX + "-i input-file.gz -o output-file.gz -c configuration-file -d dictionary-file -pc configuration.json -e Transcription\n\n";
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
        options.addOption("o", "output", true, "Compressed file to store the trained model.");
        options.addOption("c", "configuration", true, "Model configuration file.");
        options.addOption("d", "dictionary", true, "Dictionary file.");
        options.addOption("g", "gdep", true, "Folder that contains GDep.");
        options.addOption("pc", "project-configuration", true, "Annotate configuration JSON file.");
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


        // Get output file
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


        // Get model configuration file
        String modelConfigurationFilePath;
        if (commandLine.hasOption('c')) {
            modelConfigurationFilePath = commandLine.getOptionValue('c');
        } else {
            printHelp(options, "Please specify the model configuration file.");
            return;
        }
        test = new File(modelConfigurationFilePath);
        if (!test.getParentFile().canWrite()) {
            logger.error("The specified path is not writable: {}", test.getAbsolutePath());
            return;
        }
        modelConfigurationFilePath = test.getAbsolutePath();
        modelConfigurationFilePath += File.separator;

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

        // Get project configuration file path
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



//        boolean verbose = false;
//        if (args.length > 1) {
//            if (args[1].equals("-v")) {
//                verbose = true;
//            }
//        }
//
//        String label = args[0];
//
//        if (!verbose) {
//            LogManager.getLogManager().reset();
//        } else {
//            SLF4JBridgeHandler.removeHandlersForRootLogger();
//            SLF4JBridgeHandler.install();
//        }

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


//        String trainDocumentsFilePath = "resources/corpus/bionlp2009/train.gz";
//        String devDocumentsFilePath = "resources/corpus/bionlp2009/dev.gz";
//        String dictionaryPath = "resources/dictionaries/ge/training/" + label + ".txt";
//        String modelConfigurationFolder = "resources/models/bionlp2009/train_final/";
//        String outputModelFolder = "resources/models/bionlp2009/train_final/";
//
//        String modelConfigurationPath = modelConfigurationFolder + label + ".config";
//
//        Documents trainDocuments, devDocuments;
//
//        try {
//            trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(trainDocumentsFilePath)));
//            devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devDocumentsFilePath)));
//        } catch (IOException | ClassNotFoundException ex) {
//            logger.error("ERROR:", ex);
//            throw new RuntimeException("There was a problem loading input data.", ex);
//        }
//
//        logger.info("Annotate size: {}", trainDocuments.size());
//        logger.info("Dev size: {}", devDocuments.size());


        // Get Model Configuration
        ModelConfiguration modelConfiguration = new ModelConfiguration();
        try {
            modelConfiguration.load(new InputStreamReader(new FileInputStream(modelConfigurationFilePath)));
        } catch (IOException ex) {
            logger.error("There was a problem loading the configuration file: " + modelConfigurationFilePath, ex);
            return;
        }

        // Pre-processing features
        ProcessingFeaturePipeline.get(modelConfiguration).run(trainDocuments);

        // Model features
        Pipe pipe = ModelFeaturePipeline.get(modelConfiguration, dictionaryFilePath);

        // Get Annotate instances
        InstanceList train = Documents2InstancesConverter.getInstanceList(trainDocuments, pipe, event);

        // Train model
        logger.info("Training model...");
        Model model = new Model(modelConfiguration);
        model.train(train);

        // Write model
        try {
            model.write(new GZIPOutputStream(new FileOutputStream(outputFilePath)));
        } catch (GimliException | IOException e) {
            logger.error("There was a problem writing the model to the file: " + outputFilePath, e);
            return;
        }
        logger.info("Done.");
    }
}
