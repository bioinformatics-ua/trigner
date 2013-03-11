package pt.ua.tm.trigner.evaluation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.configuration.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class CompleteEvaluator {

    private static final Pattern triggerPattern = Pattern.compile("T[0-9]+");
    private static final String TRIGGER_ENTITY = "Trigger";
    private static Logger logger = LoggerFactory.getLogger(CompleteEvaluator.class);
    private Map<String, Evaluation> evaluations;

    public CompleteEvaluator() {
        this.evaluations = new HashMap<>();
    }

    public void evaluate(final InputStream goldA1InputStream, final InputStream silverA1Stream, final boolean justTriggerEvaluation) {
        TriggerList goldList = getTriggerListFromInputStream(goldA1InputStream);
        TriggerList silverList = getTriggerListFromInputStream(silverA1Stream);

        evaluate(goldList, silverList, justTriggerEvaluation);
    }

    public void evaluate(TriggerList goldList, TriggerList silverList, final boolean justTriggerEvaluation) {

        if (justTriggerEvaluation) {
            goldList = getTriggerListForTriggerEvaluation(goldList);
            silverList = getTriggerListForTriggerEvaluation(silverList);
        }

        //Silver on Gold
        for (Trigger gold : goldList) {
            if (!silverList.contains(gold)) {
                String entity = gold.getEntity();
                Evaluation evaluation = getEvaluation(entity);
                evaluation.addFN(); // fn++;
                evaluations.put(entity, evaluation);
            }
        }

        // Gold on Silver
        for (Trigger silver : silverList) {
            String entity = silver.getEntity();
            Evaluation evaluation = getEvaluation(entity);

            if (!goldList.contains(silver)) {
                evaluation.addFP(); // fp++;

            } else {
                evaluation.addTP(); // tp++;
            }
            evaluations.put(entity, evaluation);
        }
    }

    private TriggerList getTriggerListForTriggerEvaluation(TriggerList triggerList) {
        TriggerList newTriggerList = new TriggerList();

        for (Trigger trigger : triggerList) {
            trigger.setEntity(TRIGGER_ENTITY);
            if (!newTriggerList.contains(trigger)) {
                newTriggerList.add(trigger);
            }
        }
        return newTriggerList;
    }

    public void reset() {
        this.evaluations = new HashMap<>();
    }

    public Evaluation getOverall() {
        int overallTP = 0, overallFP = 0, overallFN = 0;

        for (String entity : evaluations.keySet()) {
            Evaluation evaluation = evaluations.get(entity);
            overallTP += evaluation.getTP();
            overallFP += evaluation.getFP();
            overallFN += evaluation.getFN();
        }

        // Overall evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setTP(overallTP);
        evaluation.setFP(overallFP);
        evaluation.setFN(overallFN);

        return evaluation;
    }

    public Evaluation getGroup(final String[] labels) {
        List<String> labelsList = Arrays.asList(labels);

        int overallTP = 0, overallFP = 0, overallFN = 0;

        for (String entity : evaluations.keySet()) {

            if (!labelsList.contains(entity)) {
                continue;
            }

            Evaluation evaluation = evaluations.get(entity);
            overallTP += evaluation.getTP();
            overallFP += evaluation.getFP();
            overallFN += evaluation.getFN();
        }

        // Overall evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setTP(overallTP);
        evaluation.setFP(overallFP);
        evaluation.setFN(overallFN);

        return evaluation;
    }

    public void print() {
        for (String entity : evaluations.keySet()) {
            Evaluation evaluation = evaluations.get(entity);
            printEvaluation(entity, evaluation);
        }

        logger.info("");

        // Event total
        Evaluation evaluation = getGroup(new String[]{"Gene_expression", "Transcription", "Protein_catabolism", "Phosphorylation", "Localization", "Binding"});
        printEvaluation("EVT-TOTAL", evaluation);

        // Regulation total
        evaluation = getGroup(new String[]{"Regulation", "Positive_regulation", "Negative_regulation"});
        printEvaluation("REG-TOTAL", evaluation);

        // Overall evaluation
        evaluation = getOverall();
        printEvaluation("overall", evaluation);
    }

    private void printEvaluation(final String entity, final Evaluation evaluation) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");

        logger.info("{}:\tTP:{}\tFP:{}\tFN:{}\t\tP:{}%\tR:{}%\tF1:{}%", new Object[]{
                StringUtils.leftPad(entity.toUpperCase(), 30, " "),
                evaluation.getTP(), evaluation.getFP(), evaluation.getFN(),
                decimalFormat.format(evaluation.getPrecision()), decimalFormat.format(evaluation.getRecall()), decimalFormat.format(evaluation.getF1())
        });

    }

    private TriggerList getTriggerListFromInputStream(final InputStream inputStream) {
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

//                if (Constants.entitiesPattern.matcher(entity).matches()){
//                    continue;
//                }
                if (Configuration.getConceptsPattern().matcher(entity).matches()) {
                    continue;
                }
//                if (entity.equals("Entity") || entity.equals("Protein")) {
//                    continue;
//                }

                int start = Integer.parseInt(fields[1]);
                int end = Integer.parseInt(fields[2]);

                triggerList.add(new Trigger(start, end, entity));
            }
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem reading the input stream.", ex);
        }


        return triggerList;
    }

    private Evaluation getEvaluation(final String entity) {
        if (evaluations.containsKey(entity)) {
            return evaluations.get(entity);
        } else {
            return new Evaluation();
        }
    }

}
