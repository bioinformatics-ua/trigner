package pt.ua.tm.trigner.model.features.shortestpath;

import martin.common.Tuple;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Chunk;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.features.corpus.pipeline.FeatureExtractor;
import pt.ua.tm.trigner.util.ShortestPathUtil;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 18/03/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class SPChunkDistance implements FeatureExtractor {

    private String prefix;

    public SPChunkDistance(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void extract(Sentence sentence) {

        for (Token token : sentence) {

            Tuple<AnnotationID, Integer> closest = ShortestPathUtil.getClosestConcept(sentence, token);

            if (closest == null) {
                token.putFeature(prefix, new Integer(0).toString());
                continue;
            }

            Token closestToken = getClosestToken(token, closest.getA());

            Chunk chunk1 = sentence.getChunks().getTokenChunk(token);
            Chunk chunk2 = sentence.getChunks().getTokenChunk(closestToken);

            int distance = Math.abs(chunk1.getIndex() - chunk2.getIndex());
            token.putFeature(prefix, new Integer(distance).toString());
        }
    }

    private Token getClosestToken(final Token token, final AnnotationID concept) {
        if (token.getIndex() < concept.getStartIndex()) {
            return token.getSentence().getToken(concept.getStartIndex());
        } else {
            return token.getSentence().getToken(concept.getEndIndex());
        }

    }
}
