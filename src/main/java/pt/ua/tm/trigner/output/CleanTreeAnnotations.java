package pt.ua.tm.trigner.output;

import monq.jfa.*;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.neji.core.module.BaseLoader;
import pt.ua.tm.neji.exception.NejiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 17/01/13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class CleanTreeAnnotations extends BaseLoader {

    private int sentence;
    private String[] groups;

    public CleanTreeAnnotations(Corpus corpus) throws NejiException {
        this(corpus, null);
    }

    public CleanTreeAnnotations(Corpus corpus, String[] groups) throws NejiException {
        super(corpus);

        this.sentence = 0;
        this.groups = groups;

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

            if (groups == null) { // Remove all annotations from tree
                s.cleanAnnotationsTree();
            } else { // Remove only specific group annotations
                List<TreeNode<AnnotationID>> nodes = s.getTree().build(Tree.TreeTraversalOrderEnum.PRE_ORDER);

                for (TreeNode<AnnotationID> node : nodes) {
                    if (node.equals(s.getTree().getRoot())) {
                        continue;
                    }

                    AnnotationID annotation = node.getData();
                    for (String group : groups) {
                        if (annotation.areIDsFromTheSameGroup(group)) {
                            TreeNode<AnnotationID> parent = node.getParent();
                            parent.getChildren().remove(node);
                        } else {
                            List<Identifier> toRemove = new ArrayList<>();
                            for (Identifier identifier : annotation.getIDs()) {
                                if (identifier.getGroup().equals(group)) {
                                    toRemove.add(identifier);
                                }
                            }
                            annotation.getIDs().removeAll(toRemove);
                        }
                    }
                }
            }


            sentence++;
        }
    };
}
