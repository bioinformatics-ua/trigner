package pt.ua.tm.trigner.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.trigner.documents.Documents;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        String trainDocumentsFilePath = "resources/corpus/bionlp2009/train/documents.gz";
        String devDocumentsFilePath = "resources/corpus/bionlp2009/dev/documents.gz";
        String outputFolder = "resources/models/new/bionlp2009/";

        Documents trainDocuments, devDocuments;

        try {
            trainDocuments = Documents.read(new GZIPInputStream(new FileInputStream(trainDocumentsFilePath)));
            devDocuments = Documents.read(new GZIPInputStream(new FileInputStream(devDocumentsFilePath)));
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("ERROR:", ex);
            return;
        }

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
            ModelConfig config = model.getConfig();

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
                config.write(fos);
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
