package pt.ua.tm.trigner.configuration;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */
public class Configuration {

    public static String[] triggers = new String[]{"Gene_expression", "Transcription", "Protein_catabolism",
            "Phosphorylation", "Localization", "Binding", "Regulation", "Positive_regulation", "Negative_regulation",
            "Entity"};
    private static String[] concepts = new String[]{"Protein"};

    public static String[] getConcepts() {
        return concepts;
    }

    public static void setConcepts(String[] concepts) {
        Configuration.concepts = concepts;
    }

    public static Pattern getConceptsPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(StringUtils.join(concepts, '|'));
        sb.append(")");

        return Pattern.compile(sb.toString());
    }

    public static List<String> getConceptsList() {
        return Arrays.asList(concepts);
    }

    public static String[] getTriggers() {
        return triggers;
    }

    public static void setTriggers(String[] triggers) {
        Configuration.triggers = triggers;
    }

    public static Pattern getTriggersPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(StringUtils.join(triggers, '|'));
        sb.append(")");

        return Pattern.compile(sb.toString());
    }

    public static List<String> getTriggersList() {
        return Arrays.asList(triggers);
    }
}
