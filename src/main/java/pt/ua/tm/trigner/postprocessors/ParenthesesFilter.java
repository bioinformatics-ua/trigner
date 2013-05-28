package pt.ua.tm.trigner.postprocessors;

import monq.jfa.*;
import pt.ua.tm.gimli.corpus.*;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.global.Global;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/16/13
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParenthesesFilter extends BaseLoader {
    private int sentence = 0;

    public ParenthesesFilter(final Corpus corpus) throws NejiException {
        super(corpus);
        this.sentence = 0;

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
                        int numParentheses = getNumberOfParentheses(annotation);

                        if ((numParentheses % 2) != 0) {
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

    private static final Pattern parenthesesPattern = Pattern.compile("[\\[\\]\\(\\)\\{\\}]");

    private int getNumberOfParentheses(final AnnotationID annotation) {
        int numParentheses = 0;
        for (int i = annotation.getStartIndex(); i <= annotation.getEndIndex(); i++) {
            Token token = annotation.getSentence().getToken(i);
            String tokenText = token.getText();

            Matcher matcher = parenthesesPattern.matcher(tokenText);
            if (matcher.find()) {
                numParentheses++;
            }
        }
        return numParentheses;
    }


}
