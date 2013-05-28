package pt.ua.tm.trigner.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.evaluation.CompleteEvaluator;
import pt.ua.tm.trigner.util.FileFilterUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class Evaluate {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./evaluate.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition" +
            "\nEVALUATE";
    private static final String USAGE = "-g <folder> -gf <gold-filter>"
            + "-s <folder> -sf <silver-filter>"
            + "-pc <project-configuration> ";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-g gold -s silver -pc configuration.json\n"
            + "2: "
            + TOOLPREFIX + "-g gold -gf \"*.a2\" -s silver -sf \"*.a1\" -pc configuration.json\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";

    private static Logger logger = LoggerFactory.getLogger(Evaluate.class);

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

        options.addOption("g", "gold", true, "Folder with corpus files.");
        options.addOption("gf", "gold-filter", true, "Wildcard to filter files in gold folder.");

        options.addOption("s", "silver", true, "Folder to save the annotated corpus files.");
        options.addOption("sf", "silver-filter", true, "Wildcard to filter files in silver folder.");

        options.addOption("pc", "project-configuration", true, "Annotate configuration JSON file.");

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

        // Gold folder
        String goldFolderPath = null;
        if (commandLine.hasOption('g')) {
            goldFolderPath = commandLine.getOptionValue('g');
        } else {
            printHelp(options, "Please specify the input corpus folder.");
            return;
        }
        test = new File(goldFolderPath);
        if (!test.isDirectory() || !test.canRead()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        goldFolderPath = test.getAbsolutePath();
        goldFolderPath += File.separator;

        String goldFolderWildcard = "";
        if (commandLine.hasOption("gf")) {
            goldFolderWildcard = commandLine.getOptionValue("gf");
        }

        // Silver folder
        String silverFolderPath;
        if (commandLine.hasOption('s')) {
            silverFolderPath = commandLine.getOptionValue('s');
        } else {
            printHelp(options, "Please specify the output corpus folder.");
            return;
        }
        test = new File(silverFolderPath);
        if (!test.isDirectory() || !test.canWrite()) {
            logger.error("The specified path is not a folder or is not writable: {}", test.getAbsolutePath());
            return;
        }
        silverFolderPath = test.getAbsolutePath();
        silverFolderPath += File.separator;

        String silverFolderWildcard = "";
        if (commandLine.hasOption("sf")) {
            silverFolderWildcard = commandLine.getOptionValue("sf");
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

        // Get gold files
        File goldFolder = new File(goldFolderPath);
        FileFilter goldFileFilter = FileFilterUtil.newFileFilter(goldFolderWildcard);
        File[] goldFiles = goldFolder.listFiles(goldFileFilter);

        // Get silver files
        File silverFolder = new File(silverFolderPath);
        FileFilter silverFileFilter = FileFilterUtil.newFileFilter(silverFolderWildcard);
        File[] silverFiles = silverFolder.listFiles(silverFileFilter);

        if (goldFiles.length != silverFiles.length) {
            throw new RuntimeException("Folders are not compatible.");
        }

        CompleteEvaluator evaluator = new CompleteEvaluator();
        for (int i = 0; i < goldFiles.length; i++) {
            File goldFile = goldFiles[i];
            File silverFile = silverFiles[i];

            try (
                    FileInputStream goldFIS = new FileInputStream(goldFile);
                    FileInputStream silverFIS = new FileInputStream(silverFile)
            ) {
                evaluator.evaluate(goldFIS, silverFIS);
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the files.", ex);
            }
        }

        evaluator.print();
    }
}
