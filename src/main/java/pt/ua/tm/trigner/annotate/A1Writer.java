/*
 * Copyright (c) 2012 David Campos, University of Aveiro.
 *
 * Neji is a framework for modular biomedical concept recognition made easy, fast and accessible.
 *
 * This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * This project is a free software, you are free to copy, distribute, change and transmit it. However, you may not use
 * it for commercial purposes.
 *
 * It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.trigner.annotate;

import monq.jfa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.AnnotationID;
import pt.ua.tm.gimli.corpus.AnnotationID.AnnotationType;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Identifier;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.tree.Tree.TreeTraversalOrderEnum;
import pt.ua.tm.gimli.tree.TreeNode;
import pt.ua.tm.neji.core.module.BaseWriter;
import pt.ua.tm.neji.exception.NejiException;
import pt.ua.tm.trigner.global.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Writer that provides information following the A1 format.
 *
 * @author David Campos (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class A1Writer extends BaseWriter {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(A1Writer.class);
    private Corpus corpus;
    private int sentenceCounter;
    private int offset;
    private StringBuilder content;
    private int processedAnnotations;
    private AbstractFaAction eof = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {
            yytext.replace(0, yytext.length(), content.toString().trim());
            runner.collect = false;
        }
    };
    private AbstractFaAction end_sentence = new AbstractFaAction() {
        @Override
        public void invoke(StringBuffer yytext, int start, DfaRun runner) {

            // Get start and end of sentence
            int startSentence = yytext.indexOf("<s id=");
            int endSentence = yytext.lastIndexOf("</s>") + 4;

            int realStart = yytext.indexOf(">", startSentence) + 1;
            int realEnd = endSentence - 4;

            // Get sentence with XML tags
            String sentence = yytext.substring(realStart, realEnd);

            // Get respective sentence from corpus
            Sentence s = corpus.getSentence(sentenceCounter);
            //Disambiguator.disambiguate(s, 1, true, true);

            yytext.replace(startSentence, endSentence, sentence);

            // Get final start and end of sentence
            int startChar = offset + start;
            int endChar = startChar + sentence.length();

            // Generate sentence on stand-off format
            StringBuilder sb = new StringBuilder();

            // Add annotations text to StringBuilder
            processedAnnotations += getAnnotationsText(s, sentence, sb, processedAnnotations, startChar);

            // Add sentence standoff format to final content
            content.append(sb.toString());

            // Remove processed input from input
            yytext.replace(0, endSentence, "");

            // Change state variables
            sentenceCounter++;

            offset = endChar;
            runner.collect = true;
        }
    };

    public A1Writer(final Corpus corpus) throws NejiException {
        super();
        this.corpus = corpus;
        this.sentenceCounter = 0;
        this.content = new StringBuilder();
        this.offset = 0;
        this.processedAnnotations = 0;

        try {
            Nfa nfa = new Nfa(Nfa.NOTHING);
            nfa.or(Xml.GoofedElement("s"), end_sentence);
            setNFA(nfa, DfaRun.UNMATCHED_COPY, eof);
        } catch (ReSyntaxException ex) {
            throw new NejiException(ex);
        }
    }

    private int getAnnotationsText(Sentence s, String sentenceText, StringBuilder sb, int startCounting, int offset) {

        List<TreeNode<AnnotationID>> nodes = s.getTree().build(TreeTraversalOrderEnum.PRE_ORDER);

        int processed = 0;
        for (TreeNode<AnnotationID> node : nodes) {

            AnnotationID data = node.getData();

            // Skip intersection parents and root node without IDs
            if (data.getType().equals(AnnotationType.INTERSECTION) ||
                    (node.equals(s.getTree().getRoot()) && node.getData().getIDs().isEmpty())) {
                continue;
            }

            int startAnnotationInSentence = s.getToken(data.getStartIndex()).getStartSource();
            int endAnnotationInSentence = s.getToken(data.getEndIndex()).getEndSource() + 1;

            int startChar = offset + startAnnotationInSentence;
            int endChar = offset + endAnnotationInSentence;

            List<String> groups = new ArrayList<>();
            for (Identifier id : data.getIDs()) {
                String group = id.getGroup();

                if (Global.projectConfiguration.getConcepts().contains(group)) {
                    continue;
                }

                if (!groups.contains(group)) {
                    groups.add(group);
                }
            }

            for (String group : groups) {

                // Term
                String termPrefix = "T" + (startCounting + processed);
                sb.append(termPrefix);

                sb.append("\t");
                sb.append(group);
                sb.append(" ");
                sb.append(startChar);
                sb.append(" ");
                sb.append(endChar);

                sb.append("\t");
                sb.append(sentenceText.substring(startAnnotationInSentence, endAnnotationInSentence).trim());
                sb.append("\n");

                // Annotator notes
//                String prefix = "#" + (startCounting + processed);
//                sb.append(prefix);
//                sb.append("\tIdentifiers ");
//                sb.append(termPrefix);
//                sb.append("\t");
//                // Get ids for that group
//                sb.append(getIDsStringByGroup(data, group));
//
//                sb.append("\n");

                processed++;
            }
        }
        return processed;
    }

    private String getIDsStringByGroup(final AnnotationID annotation, final String group) {
        StringBuilder sb = new StringBuilder();

        for (Identifier id : annotation.getIDs()) {
            if (id.getGroup().equals(group)) {
                sb.append(id.toString());
                sb.append("|");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


}
