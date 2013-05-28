package pt.ua.tm.trigner.util;

import pt.ua.tm.gimli.corpus.Chunk;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.trigner.shared.Types;
import pt.ua.tm.trigner.shared.Types.VertexFeatureType;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */
public class TokenFeatureUtil {
    public static String getFeature(final Token token, final Types.VertexFeatureType feature) {
        switch (feature) {
            case WORD:
                return token.getText();
            case LEMMA:
                return token.getFeature("LEMMA").get(0);
            case POS:
                return token.getFeature("POS").get(0);
            case CHUNK:
                Chunk chunk = token.getSentence().getChunks().getTokenChunk(token);
                return chunk.getTag().toString();
            default:
                return null;
        }
    }
}
