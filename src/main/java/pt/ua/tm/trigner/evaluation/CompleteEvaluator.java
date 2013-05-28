package pt.ua.tm.trigner.evaluation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.configuration.EventGroup;
import pt.ua.tm.trigner.global.Global;

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

    private static Logger logger = LoggerFactory.getLogger(CompleteEvaluator.class);
    private Map<String, Evaluation> evaluations;

    public CompleteEvaluator() {
        this.evaluations = new HashMap<>();
    }

    public void evaluate(final InputStream goldA1InputStream, final InputStream silverA1Stream) {
        TriggerList goldList = getTriggerListFromInputStream(goldA1InputStream);
        TriggerList silverList = getTriggerListFromInputStream(silverA1Stream);

        evaluate(goldList, silverList);
    }

    public void evaluate(TriggerList goldList, TriggerList silverList) {
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
        // Individual
        for (String event : Global.projectConfiguration.getEvents()) {
            Evaluation evaluation = evaluations.get(event);
            printEvaluation(event, evaluation);
        }
        logger.info("");

        // Groups
        for (EventGroup group : Global.projectConfiguration.getGroups()) {
            Evaluation evaluation = getGroup(group.getEvents().toArray(new String[]{}));
            printEvaluation(group.getName(), evaluation);
        }
        logger.info("");

        // Overall
        Evaluation evaluation = getOverall();
        printEvaluation("Overall", evaluation);
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

                if (Global.projectConfiguration.getConcepts().contains(entity)) {
                    continue;
                }
                if (!Global.projectConfiguration.getEvents().contains(entity)) {
                    continue;
                }

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
