package pt.ua.tm.trigner.cli;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 25/01/13
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class Dictionary {

    private static Logger logger = LoggerFactory.getLogger(Dictionary.class);

    public static void main(final String... args) {

        String folderPath = "/Users/david/Downloads/merged_a2/";
        String dictionariesPath = "resources/dictionaries/ge/";

        Map<String, HashSet<String>> map = new HashMap<>();

        Pattern termPattern = Pattern.compile("T[0-9]+");
        File[] files = new File(folderPath).listFiles(new FileUtil.Filter(new String[]{"a2"}));

        // Load
        for (File file : files) {
            try (
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");

                    if (!termPattern.matcher(parts[0]).matches()) {
                        continue;
                    }

                    String group = parts[1].split("\\s+")[0];


//                    // Only events and terms to predict
//                    if (!Constants.eventsPattern.matcher(group).matches() && !Constants.entitiesPattern.matcher(group).matches()) {
//                        continue;
//                    }


                    String trigger = parts[2].toLowerCase();

                    if (trigger.length() < 3){
                        continue;
                    }

                    HashSet<String> triggers;
                    if (map.containsKey(group)) {
                        triggers = map.get(group);
                        triggers.add(trigger);
                    } else {
                        triggers = new HashSet<>();
                        triggers.add(trigger);
                    }

                    map.put(group, triggers);

                }
            } catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the file: " + file.getAbsolutePath(), ex);
            }
        }

        String outputPriority = dictionariesPath + "annotation/_priority";
        FileOutputStream fosPriority;
        try {
            fosPriority = new FileOutputStream(outputPriority);
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem writing the file: " + outputPriority);
        }


        // Write
        for (String group : map.keySet()) {

            logger.info("{}", group);

            String outputTXT = dictionariesPath + "training/" + group + ".txt";
            String outputTSV = dictionariesPath + "annotation/" + group + ".tsv";


            try (
                    FileOutputStream fosTXT = new FileOutputStream(outputTXT);
                    FileOutputStream fosTSV = new FileOutputStream(outputTSV);

            ) {
                // Priority
                fosPriority.write(group.getBytes());
                fosPriority.write(".tsv".getBytes());
                fosPriority.write("\n".getBytes());


                HashSet<String> triggersSet = map.get(group);
                List<String> triggers = Arrays.asList(triggersSet.toArray(new String[]{}));
                Collections.sort(triggers);

                fosTXT.write(StringUtils.join(triggers, "\n").getBytes());

                fosTSV.write(":::".getBytes());
                fosTSV.write(group.getBytes());
                fosTSV.write("\t".getBytes());
                fosTSV.write(StringUtils.join(triggers, "|").getBytes());

            } catch (IOException ex) {
                throw new RuntimeException("There was a problem writing the file: " + outputTXT);
            }
        }

        try {
            fosPriority.close();
        } catch (IOException ex) {
            throw new RuntimeException("There was a problem closing the file: " + outputPriority);
        }
    }
}
