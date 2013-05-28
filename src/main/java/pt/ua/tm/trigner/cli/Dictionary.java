package pt.ua.tm.trigner.cli;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.util.FileFilterUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 25/01/13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class Dictionary {

    /**
     * Help message.
     */
    private static final String TOOLPREFIX = "./dictionary.sh ";

    private static final String HEADER = "\nTrigner: biomedical event trigger recognition (DICTIONARY)\n";
    private static final String USAGE = "-i <folder> -if <silver-filter>"
            + "-o <folder> "
            + "-pc <project-configuration> ";
    private static final String EXAMPLES = "\nUsage examples:\n"
            + "1: "
            + TOOLPREFIX + "-i input-folder -o output-folder \n\n";
    private static final String FOOTER = "For more instructions, please visit http://bioinformatics.ua.pt/trigner";

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

    private static Logger logger = LoggerFactory.getLogger(Dictionary.class);

    public static void main(final String... args) {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        options.addOption("i", "input", true, "Folder with corpus files.");
        options.addOption("if", "input-filter", true, "Wildcard to filter files from input folder.");
        options.addOption("o", "output", true, "Folder to save the annotated corpus files.");
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

        String inputFolderWildcard = "";
        if (commandLine.hasOption("if")) {
            inputFolderWildcard = commandLine.getOptionValue("if");
        }

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

        Map<String, HashSet<String>> map = new HashMap<>();

        Pattern termPattern = Pattern.compile("T[0-9]+");


        File goldFolder = new File(inputFolderPath);
        FileFilter goldFileFilter = FileFilterUtil.newFileFilter(inputFolderWildcard);
        File[] files = goldFolder.listFiles(goldFileFilter);

        //File[] files = new File(folderPath).listFiles(new FileUtil.Filter(new String[]{"a2"}));

        // Load entries
        for (File file : files) {
            try (
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");

                    if (!termPattern.matcher(parts[0]).matches()) {
                        continue;
                    }

                    String group = parts[1].split("\\s+")[0];
                    if (!Global.projectConfiguration.getEvents().contains(group)) {
                        continue;
                    }

                    String trigger = parts[2].toLowerCase();
                    if (trigger.length() < 3) {
                        continue;
                    }

                    HashSet<String> triggers;
                    if (map.containsKey(group)) {
                        triggers = map.get(group);
                        triggers.add(trigger);
                    } else {
                        triggers = new HashSet<>();
                        triggers.add(trigger);
                    }
                    map.put(group, triggers);
                }
            } catch (IOException ex) {
                logger.error("There was a problem reading the file: " + file.getAbsolutePath(), ex);
                return;
            }
        }

        // Create training and annotation folders
        File trainFolder = new File(outputFolderPath, "train");
        trainFolder.mkdir();

        File annotateFolder = new File(outputFolderPath, "annotate");
        annotateFolder.mkdir();

        // Create _priority file in annotate
        File priorityFile = new File(annotateFolder, "_priority");

        FileOutputStream fosPriority;
        try {
            fosPriority = new FileOutputStream(priorityFile);
        } catch (IOException ex) {
            logger.error("There was a problem writing the file: " + priorityFile.getAbsolutePath());
            return;
        }


        // Write
        for (String group : map.keySet()) {

            logger.info("{}", group);

            File trainDictionaryFile = new File(trainFolder, group + ".txt");
            File annotateDictionaryFile = new File(annotateFolder, group + ".tsv");

            try (
                    FileOutputStream fosTXT = new FileOutputStream(trainDictionaryFile);
                    FileOutputStream fosTSV = new FileOutputStream(annotateDictionaryFile);

            ) {
                // Priority
                fosPriority.write(group.getBytes());
                fosPriority.write(".tsv".getBytes());
                fosPriority.write("\n".getBytes());


                HashSet<String> triggersSet = map.get(group);
                List<String> triggers = Arrays.asList(triggersSet.toArray(new String[]{}));
                Collections.sort(triggers);

                fosTXT.write(StringUtils.join(triggers, "\n").getBytes());

                fosTSV.write(":::".getBytes());
                fosTSV.write(group.getBytes());
                fosTSV.write("\t".getBytes());
                fosTSV.write(StringUtils.join(triggers, "|").getBytes());

            } catch (IOException ex) {
                throw new RuntimeException("There was a problem writing the dictionary files.");
            }
        }

        try {
            fosPriority.close();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem closing the file: " + priorityFile.getAbsolutePath());
        }
    }
}
