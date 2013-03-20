package pt.ua.tm.trigner.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.model.Model;
import pt.ua.tm.trigner.model.configuration.ModelConfiguration;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/03/13
 * Time: 17:36
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {

        boolean verbose = false;
        if (args.length > 0){
            if (args[0].equals("-v")){
                verbose = true;
            }
        }

        String trainDocumentsFilePath = "resources/corpus/bionlp2009/train/documents.gz";
//        String devDocumentsFilePath = "resources/corpus/bion lp2009/dev/documents.gz";
        String outputFolder = "resources/models/bionlp2009/";


        // Disable output from Mallet and GDepTranslator
        if (!verbose) {
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }));
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }));
        }

        Documents documents, trainDocuments, devDocuments;

        try {
            documents = Documents.read(new GZIPInputStream(new FileInputStream(trainDocumentsFilePath)));
//            devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devDocumentsFilePath)));
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("ERROR:", ex);
            return;
        }

        Documents[] docs = documents.splitInOrder(new double[]{0.8, 0.2});


        trainDocuments = docs[0];
        devDocuments = docs[1];


        logger.info("Train size: {}", trainDocuments.size());
        logger.info("Dev size: {}", devDocuments.size());

        // Add pre-processing features
//        Features.add(new Documents[]{trainDocuments, devDocuments});

        // Get best models for each trigger
        Map<String, Model> models = Optimization.run(trainDocuments, devDocuments);

        // Store models
        writeModels(models, outputFolder);

    }

    private static void writeModels(final Map<String, Model> models, final String outputFolder) {
        for (String label : models.keySet()) {
            Model model = models.get(label);
            ModelConfiguration mc = model.getModelConfiguration();

            // Write properties
            writeProperties(outputFolder, label);

            // Save best model
            String name = outputFolder + label + ".gz";
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(name))) {
                model.write(gzipOutputStream);
            } catch (IOException | GimliException e) {
                logger.error("There was a problem writing the model to the file: " + name, e);
                return;
            }

            // Save best config
            name = outputFolder + label + ".config";
            try (FileOutputStream fos = new FileOutputStream(name)) {
                mc.store(fos, label);
            } catch (IOException e) {
                logger.error("There was a problem writing the model configuration to the file: " + name, e);
                return;
            }
        }

        // Write priority file
        writePriority(models, outputFolder);

    }

    private static void writePriority(final Map<String, Model> models, final String outputFolder) {
        String propertiesPath = outputFolder + "_priority";
        try (FileOutputStream fos = new FileOutputStream(propertiesPath)) {

            StringBuilder sb = new StringBuilder();

            for (String label : models.keySet()) {
                sb.append(label);
                sb.append(".properties");
                sb.append("\n");
            }
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem writing the properties file: " + propertiesPath, ex);
        }
    }

    private static void writeProperties(final String outputFolder, final String label) {
        String propertiesPath = outputFolder + label + ".properties";
        try (FileOutputStream fos = new FileOutputStream(propertiesPath)) {

            StringBuilder sb = new StringBuilder();
            sb.append("file=");
            sb.append(label);
            sb.append(".gz");
            sb.append("\n");

            sb.append("config=");
            sb.append(label);
            sb.append(".config");
            sb.append("\n");

            sb.append("parsing=");
            sb.append("FW");
            sb.append("\n");

            sb.append("group=");
            sb.append(label);

            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem writing the properties file: " + propertiesPath, ex);
        }
    }
}
