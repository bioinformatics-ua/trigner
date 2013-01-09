package pt.ua.tm.trigner;

import monq.jfa.AbstractFaAction;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReSyntaxException;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.exception.NejiException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 20/12/12
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class CoNLLCustomWriter extends BaseWriter {

    private Corpus corpus;

    public CoNLLCustomWriter(final Corpus corpus) throws NejiException {
        super();
        this.corpus = corpus;

        Nfa nfa = new Nfa(Nfa.NOTHING);
        setNFA(nfa, DfaRun.UNMATCHED_DROP, eof);
    }

    private AbstractFaAction eof = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            List<TreeNode<AnnotationID>> annotationNodes;
            StringBuilder sb = new StringBuilder();

            for (Sentence s : corpus) {
                annotationNodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);
                for (int i = 0; i < s.size(); i++) {
                    Token t = s.getToken(i);

                    // TEXT
                    sb.append(t.getText());
                    sb.append("\t");

                    if (t.getFeature("LEMMA") != null) {
                        sb.append(t.getFeature("LEMMA"));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");

                    if (t.getFeature("CHUNK") != null) {
                        sb.append(t.getFeature("CHUNK"));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");

                    if (t.getFeature("POS") != null) {
                        sb.append(t.getFeature("POS"));
                    } else {
                        sb.append("_");
                    }
                    sb.append("\t");


                    // FEATS
                    sb.append(getAnnotationsAsFeatures(s.getTree(), annotationNodes, t));
//                    sb.append("\t");

//                    if (t.getFeature("DEP_TOK") != null) {
//                        sb.append(t.getFeature("DEP_TOK"));
//                    } else {
//                        sb.append("_");
//                    }
//                    sb.append("\t");
//
//                    if (t.getFeature("DEP_TAG") != null) {
//                        sb.append(t.getFeature("DEP_TAG"));
//                    } else {
//                        sb.append("_");
//                    }
//                    sb.append("\t");

                    sb.append("\n");
                }
                sb.append("\n");
            }


            yytext.replace(0, yytext.length(), sb.toString());
            runner.collect = false;
        }
    };

    // Get concept annotations
    private static String getAnnotationsAsFeatures(final Tree<AnnotationID> tree,
                                                   final List<TreeNode<AnnotationID>> annotationNodes, final Token token) {
        final Set<String> semGroups = new HashSet<>();

        for (final TreeNode<AnnotationID> node : annotationNodes) {
            // Skip the root node (whole sentence)
            if (node.equals(tree.getRoot())) {
                continue;
            }

            // Check if current node refers to our token
            if (token.getIndex() >= node.getData().getStartIndex()
                    && token.getIndex() <= node.getData().getEndIndex()) {

                for (final Identifier id : node.getData().getIDs()) {
                    semGroups.add(id.getGroup());
                }
            }
        }

        // Build Semantic Groups string (separated by ;)
        final StringBuilder sb = new StringBuilder();
        for (final String group : semGroups) {
            sb.append(group);
            sb.append(";");
        }

        if (sb.length() == 0) {
            sb.append("O");
        } else {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }


}
