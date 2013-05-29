/*
 * Copyright (c) 2012 David Campos, University of Aveiro and Erasmus Medical Center.
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
package pt.ua.tm.trigner.documents;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;

import java.io.*;
import java.util.*;

/**
 * @author david
 */
public class Documents extends ArrayList<Corpus> implements Iterable<Corpus>, Serializable {

    /**
     * {@link org.slf4j.Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Documents.class);

    public Documents() {
        super();
    }

    public Documents(Collection c) {
        super(c);
    }

    public static Documents read(InputStream input) throws IOException, ClassNotFoundException {
        ObjectInputStream obj = new ObjectInputStream(input);
        Documents d = (Documents) obj.readObject();
        input.close();
        return d;
    }

    public Corpus getByID(String id) {
        for (Corpus c : this) {
            if (c.getIdentifier().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public Documents[] splitInOrder(double[] ratios) {
        return splitInOrder(ratios, this);
    }

    private Documents[] splitInOrder(double[] ratios, Documents documents) {
        double sum = 0.0;
        for (double d : ratios) {
            sum += d;
        }

//        if (sum != 1.0) {
//            throw new RuntimeException("The provided ratios do not sum to 1.0. Sum = " + sum);
//        }

        int[] numbers = new int[ratios.length];

        int total = 0;
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = total + (int) (ratios[i] * documents.size());
            total = numbers[i];
        }

        if (total < documents.size()) {
            numbers[numbers.length - 1] += documents.size() - total;
        }

        return splitInOrder(numbers, documents);

    }

    public Documents[] splitInOrder(int[] numbers) {
        return splitInOrder(numbers, this);
    }

    private Documents[] splitInOrder(int[] numbers, Documents documents) {
        int last = numbers[numbers.length - 1];
        if (last != documents.size()) {
            throw new RuntimeException("The provided sizes do not sum to the documents size.");
        }

        Documents[] docs = new Documents[numbers.length];
        int start = 0;
        for (int i = 0; i < numbers.length; i++) {
            int end = numbers[i];

            Documents d = new Documents();
            d.addAll(documents.subList(start, end));
            docs[i] = d;

            start = end;
        }
        return docs;
    }

    public Documents[] splitRandom(double[] ratios) {
        Documents shuffle = (Documents) this.clone();
        Collections.shuffle(shuffle, new Random(new Date().getTime()));
        return splitInOrder(ratios, shuffle);
    }

    public Documents[] splitRandom(int[] numbers) {
        Documents shuffle = (Documents) this.clone();
        Collections.shuffle(shuffle, new Random(new Date().getTime()));

        return splitInOrder(numbers, shuffle);
    }

    public void cleanFeatures(final String[] except) {
        List<String> exceptList = Arrays.asList(except);
        for (Corpus corpus : this) {
            for (Sentence sentence : corpus) {
                for (Token token : sentence) {
                    Multimap<String, String> current = token.getFeaturesMap();
//                    Multimap<String, String> next = HashMultimap.create();

                    List<String> toRemove = new ArrayList<>();
                    for (String key : current.keySet()) {
                        if (!exceptList.contains(key)) {
                            toRemove.add(key);
                        }
                    }

                    for (String keyToRemove : toRemove) {
                        current.removeAll(keyToRemove);
                    }

                    // Set new map
//                    current = null;
//                    token.setFeaturesMap(next);
                }
            }
        }
    }

    @Override
    public Iterator<Corpus> iterator() {
        return super.iterator();
    }

    public void write(OutputStream output) throws IOException {
        ObjectOutputStream obj = new ObjectOutputStream(output);
        obj.writeObject(this);
        obj.close();
    }
}
