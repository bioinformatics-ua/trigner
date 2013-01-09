package pt.ua.tm.trigner;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/01/13
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        String trainFolderPath = "resources/corpus/bionlp2011/train/conll/";
        String devFolderPath = "resources/corpus/bionlp2011/dev/conll/";
        String modelPath = "resources/models/bionlp2011.gz";


        Model model = new Model();
        Pipe pipe = model.getFeaturePipe();

        // Train
        logger.info("Train model...");
        InstanceList train = Data.readDirectory(new File(trainFolderPath), pipe);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        model.train(train);

        stopWatch.stop();


        // Save model
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(modelPath))) {
            model.write(gzipOutputStream);
        } catch (IOException e) {
            logger.error("There was a problem writing the model to the file: " + modelPath, e);
        }

        // Dev
        logger.info("");
        logger.info("Test model...");
        InstanceList dev = Data.readDirectory(new File(devFolderPath), pipe);
        model.test(dev);

        logger.info("Elapsed time: {}", stopWatch.toString());
    }
}
