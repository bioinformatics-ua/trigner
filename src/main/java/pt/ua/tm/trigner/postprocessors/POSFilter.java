package pt.ua.tm.trigner.postprocessors;

import monq.jfa.*;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.global.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/14/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class POSFilter extends BaseLoader {
    private int sentence = 0;
    private String[] tags;

    public POSFilter(final Corpus corpus, final String[] tags) throws NejiException {
        super(corpus);
        this.sentence = 0;
        this.tags = tags;

        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.ETag("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            Sentence s = getCorpus().getSentence(sentence);
            List<TreeNode<AnnotationID>> nodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);

            for (TreeNode<AnnotationID> node : nodes) {
                AnnotationID annotation = node.getData();
                for (Identifier identifier : annotation.getIDs()) {
                    if (Global.projectConfiguration.getEvents().contains(identifier.getGroup())) {

                        List<String> posTags = getPOSTags(annotation);
                        if (!containAnyTag(posTags, tags)) {
                            TreeNode<AnnotationID> parent = node.getParent();
                            int childIndex = parent.getChildren().indexOf(node);
                            parent.removeChildAt(childIndex);
                        }
                        break;
                    }
                }
            }
            sentence++;

        }
    };


    private boolean containAnyTag(final List<String> posTags, final String[] wantedTags) {
        for (String tag : wantedTags) {
            if (posTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPOSTags(final AnnotationID annotation) {
        List<String> posTags = new ArrayList<>();
        for (int i = annotation.getStartIndex(); i <= annotation.getEndIndex(); i++) {
            Token token = annotation.getSentence().getToken(i);
            posTags.add(token.getFeature("POS").get(0));
        }
        return posTags;
    }
}
