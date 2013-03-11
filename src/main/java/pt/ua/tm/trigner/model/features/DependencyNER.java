package pt.ua.tm.trigner.model.features;

import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.trigner.documents.Documents;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class DependencyNER {

    public static void add(Documents documents) {
        for (Corpus corpus : documents) {
            for (Sentence sentence : corpus) {
                for (Token token : sentence.getTokens()) {
                    for (int j = 0; j < token.sizeFeatures(); j++) {
                        String feature = token.getFeature(j);
                        String[] parts = feature.split("=");

                        String key = parts[0];

                        if (key.equals("SUB") ||
                                key.equals("OBJ") ||
                                key.equals("NMOD_OF") ||
                                key.equals("NMOD_BY") ||
                                key.equals("VMOD_OF") ||
                                key.equals("VMOD_BY")) {

                            String value;
                            if (parts.length == 1) {
                                value = "=";
                            } else {
                                value = parts[1];
                            }
                            token.putFeature(key, value);
                        }
                    }
                }
            }
        }
    }
}
