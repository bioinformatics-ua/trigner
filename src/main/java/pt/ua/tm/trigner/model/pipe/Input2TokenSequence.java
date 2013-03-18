package pt.ua.tm.trigner.model.pipe;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.*;
import pt.ua.tm.gimli.config.ModelConfig;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/01/13
 * Time: 09:11
 * To change this template use File | Settings | File Templates.
 */
public class Input2TokenSequence extends Pipe {

    private ModelConfig config;

    public Input2TokenSequence(final ModelConfig config) {
        super(null, new LabelAlphabet());
        this.config = config;
    }

    /**
     * Extract the data and features from input data.
     *
     * @param carrier Raw input data.
     * @return Processed instance with correct data and features.
     */
    @Override
    public Instance pipe(Instance carrier) {

        String sentenceLines = (String) carrier.getData();

        String[] tokens = sentenceLines.split("\n");
        TokenSequence data = new TokenSequence(tokens.length);
        LabelSequence target = new LabelSequence((LabelAlphabet) getTargetAlphabet(), tokens.length);
        StringBuffer source = new StringBuffer();

        String text, label;

        ArrayList<Token> newTokens = new ArrayList<>();
        ArrayList<String> newLabels = new ArrayList<>();

        for (String t : tokens) {
            String[] features = t.split("\t");

            // Token
            text = features[0];
            Token token = new Token(text);

            for (int i = 1; i < features.length - 1; i++) {
                String feature = features[i];
                String[] parts = feature.split("=");

                String key = parts[0];

                boolean addFeature = true;

                if (!config.isToken() && key.equals("WORD")) {
                    addFeature = false;
                }
                if (!config.isLemma() && key.equals("LEMMA")) {
                    addFeature = false;
                }
                if (!config.isPos() && key.equals("POS")) {
                    addFeature = false;
                }

                if (addFeature) {
                    token.setFeatureValue(feature, 1.0);
                }
            }

            label = features[features.length - 1];

            newTokens.add(token);
            newLabels.add(label);

            source.append(text);
            source.append(" ");
        }

        // Add Tokens to Data
        for (Token t : newTokens) {
            StringBuilder sb = new StringBuilder(t.getText());
            t.setText(sb.toString());
            data.add(t);
        }

        // Add labels to Target
        for (String l : newLabels)
            target.add(l);

        carrier.setData(data);
        carrier.setTarget(target);
        carrier.setSource(source);

        return carrier;
    }
}
