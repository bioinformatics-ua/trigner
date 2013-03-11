package pt.ua.tm.trigner.tmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.documents.Documents;
import pt.ua.tm.trigner.evaluation.CompleteEvaluator;
import pt.ua.tm.trigner.evaluation.Trigger;
import pt.ua.tm.trigner.evaluation.TriggerList;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
public class Validate {

    private static final Pattern triggerPattern = Pattern.compile("T[0-9]+");
    private static Logger logger = LoggerFactory.getLogger(Validate.class);

    public static void main(String... args) {
        String documentsFilePath = "resources/corpus/bionlp2009/train/documents.gz";
        String goldFolder = "resources/corpus/bionlp2009/train/gold/";

        Documents documents;

        try {
            documents = Documents.read(new GZIPInputStream(new FileInputStream(documentsFilePath)));
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("ERROR:", ex);
            return;
        }

        CompleteEvaluator evaluator = new CompleteEvaluator();

        for (Corpus corpus : documents) {
            String id = corpus.getIdentifier();

            TriggerList a1, a2;
            try {
                a1 = getTriggerList(new FileInputStream(goldFolder + id + ".a1"));
                a2 = getTriggerList(new FileInputStream(goldFolder + id + ".a2"));
            } catch (IOException ex) {
                logger.error("ERROR:", ex);
                return;
            }


            TriggerList gold = new TriggerList();
            gold.addAll(a1);
            gold.addAll(a2);

            TriggerList silver = getTriggerList(corpus);

            evaluator.evaluate(gold, silver, false);
        }

        evaluator.print();

    }

    private static TriggerList getTriggerList(InputStream inputStream) {
        TriggerList triggerList = new TriggerList();

        try (InputStreamReader isr = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (!triggerPattern.matcher(parts[0]).matches()) {
                    continue;
                }

                String[] fields = parts[1].split("\\s+");
                String entity = fields[0];

                int start = Integer.parseInt(fields[1]);
                int end = Integer.parseInt(fields[2]);

                triggerList.add(new Trigger(start, end, entity));
            }
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem reading the input stream.", ex);
        }
        return triggerList;
    }

    private static TriggerList getTriggerList(Corpus corpus) {
        TriggerList triggerList = new TriggerList();
        for (Sentence sentence : corpus) {
            List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);

            for (AnnotationID annotation : annotations) {
                for (Identifier identifier : annotation.getIDs()) {

                    int start = sentence.getStartSource() + sentence.getToken(annotation.getStartIndex()).getStartSource();
                    int end = sentence.getStartSource() + sentence.getToken(annotation.getEndIndex()).getEndSource()+1;

                    Trigger trigger = new Trigger(start, end, identifier.getGroup());
                    triggerList.add(trigger);
                }
            }
        }

        return triggerList;
    }
}
