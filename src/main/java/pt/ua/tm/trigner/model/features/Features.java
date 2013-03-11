package pt.ua.tm.trigner.model.features;

import pt.ua.tm.trigner.documents.Documents;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/03/13
 * Time: 18:27
 * To change this template use File | Settings | File Templates.
 */
public class Features {

    public static void add(Documents[] array) {
        for (Documents documents : array) {
            add(documents);
        }
    }

    public static void add(Documents documents) {
        // DependencyNER features from NER
        DependencyNER.add(documents);

        // Concept tags
        ConceptTags.add(documents);

        // Number of concepts
        NumberConcepts.add(documents);

        // DependencyNER Path
//        DependencyFeaturePath.add(documents, 2);
    }
}
