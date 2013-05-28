package pt.ua.tm.trigner.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.trigner.global.Global;
import pt.ua.tm.trigner.documents.Documents;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 02/03/13
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
public class Documents2InstancesConverter {

    public static InstanceList getInstanceList(final Documents documents, final Pipe pipe) {
        return getInstanceList(documents, pipe, null);
    }

    public static InstanceList getInstanceList(final Documents documents, final Pipe pipe, final String targetLabel) {
        InstanceList instances = new InstanceList(pipe);

        int counter = 0;
        for (Corpus corpus : documents) {
            for (Sentence sentence : corpus) {
                instances.addThruPipe(new Instance(getSentenceData(sentence, targetLabel), "", counter++, ""));
            }
        }

        return instances;
    }

    public static String getSentenceData(final Sentence sentence) {
        return getSentenceData(sentence, null);
    }

    public static String getSentenceData(final Sentence sentence, final String targetLabel) {
        StringBuilder sb = new StringBuilder();
        List<AnnotationID> annotations = sentence.getTreeAnnotations(Tree.TreeTraversalOrderEnum.PRE_ORDER, true);

        for (Token token : sentence.getTokens()) {
            // TEXT
            sb.append(token.getText());
            sb.append("\t");

            // Text feature
            sb.append("WORD=");
            sb.append(token.getText());
            sb.append("\t");

            // Pre-processed features
            Multimap<String, String> featuresMap = token.getFeaturesMap();
            for (String key : featuresMap.keySet()) {
                Collection<String> values = featuresMap.get(key);

                for (String value : values) {
                    sb.append(key);
                    if (!value.equals("")) {
                        sb.append("=");
                        sb.append(value);
                    }
                    sb.append("\t");
                }
            }

            // Label
            Set<String> labels = getTokenLabels(annotations, token);
//            sb.append("LABEL=");
            sb.append(getLabel(labels, targetLabel));
            sb.append("\n");
        }

        return sb.toString();
    }

    // Get concept annotations
    public static Set<String> getTokenLabels(final List<AnnotationID> annotations, final Token token) {
        final Set<String> semGroups = new HashSet<>();

        for (final AnnotationID annotation : annotations) {
            // Check if current node refers to our token
            if (token.getIndex() >= annotation.getStartIndex()
                    && token.getIndex() <= annotation.getEndIndex()) {

                for (final Identifier id : annotation.getIDs()) {
                    String group = id.getGroup();
                    if (Global.projectConfiguration.getEvents().contains(group)) {
                        semGroups.add(group);
                    }
                }
            }
        }
        return semGroups;
    }

    private static String getLabel(final Set<String> labels, final String targetLabel) {
        if (labels.isEmpty()) {
            return "O";
        } else {
            if (targetLabel == null) {
                return StringUtils.join(labels, ',');
            } else {
                if (labels.contains(targetLabel)) {
                    return targetLabel;
                } else {
                    return "O";
                }
            }
        }
    }
}
