package pt.ua.tm.trigner.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.util.FileUtil;
import pt.ua.tm.trigner.evaluation.CompleteEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class Evaluate {

    private static Logger logger = LoggerFactory.getLogger(Evaluate.class);

    public static void main(String... args) {
//        String goldFolderPath = "resources/corpus/bionlp2011/dev/";
//        String silverFolderPath = "resources/corpus/bionlp2011/dev/silver/dictionaries/";
//        String silverFolderPath = "resources/corpus/bionlp2011/dev/silver/ml/";

        String goldFolderPath = "resources/corpus/bionlp2009/dev/";
        String silverFolderPath = "resources/corpus/bionlp2009/dev_silver_ml_train_final/";
//        String silverFolderPath = "/Users/david/Downloads/dev_without_gold/";
//        String silverFolderPath = "/Users/david/Downloads/tees-ge09-devel/";

        boolean justTriggerEvaluation = false;

        File[] goldFiles = new File(goldFolderPath).listFiles(new FileUtil.Filter(new String[]{"a2"}));
        File[] silverFiles = new File(silverFolderPath).listFiles(new FileUtil.Filter(new String[]{"a1"}));

        if (goldFiles.length != silverFiles.length) {
            throw new RuntimeException("Folders are not compatible.");
        }

        CompleteEvaluator evaluator = new CompleteEvaluator();
        for (int i = 0; i < goldFiles.length; i++) {
            File goldFile = goldFiles[i];
            File silverFile = silverFiles[i];

//            logger.info("Gold: {}", goldFile.getName());
//            logger.info("Silver: {}\n", silverFile.getName());

            try (
                    FileInputStream goldFIS = new FileInputStream(goldFile);
                    FileInputStream silverFIS = new FileInputStream(silverFile)
            ) {
                evaluator.evaluate(goldFIS, silverFIS, justTriggerEvaluation);
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the files.", ex);
            }
        }

        evaluator.print();

    }
}
