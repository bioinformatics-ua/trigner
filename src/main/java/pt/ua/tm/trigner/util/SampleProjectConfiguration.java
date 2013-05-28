package pt.ua.tm.trigner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.trigner.configuration.EventGroup;
import pt.ua.tm.trigner.configuration.ProjectConfiguration;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/22/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleProjectConfiguration {

    private static Logger logger = LoggerFactory.getLogger(SampleProjectConfiguration.class);

    public static ProjectConfiguration getSampleProjectConfiguration() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();

        // Concepts
        projectConfiguration.getConcepts().add("Protein");

        // Event groups
        EventGroup eventGroup = new EventGroup("EVT-TOTAL");
        eventGroup.getEvents().add("Gene_expression");
        eventGroup.getEvents().add("Transcription");
        eventGroup.getEvents().add("Protein_catabolism");
        eventGroup.getEvents().add("Phosphorylation");
        eventGroup.getEvents().add("Localization");
        eventGroup.getEvents().add("Binding");
        projectConfiguration.getGroups().add(eventGroup);

        eventGroup = new EventGroup("REG-TOTAL");
        eventGroup.getEvents().add("Regulation");
        eventGroup.getEvents().add("Positive_regulation");
        eventGroup.getEvents().add("Negative_regulation");
        projectConfiguration.getGroups().add(eventGroup);

        return projectConfiguration;
    }

    public static void main(String... args) {
        String filePath = "projectConfiguration.json";
        ProjectConfiguration projectConfiguration = getSampleProjectConfiguration();
        try {
            projectConfiguration.write(new FileOutputStream(filePath));
        } catch (IOException e) {
            logger.info("There was a problem storing the project configuration in the file {}.", filePath);
            return;
        }

    }
}
