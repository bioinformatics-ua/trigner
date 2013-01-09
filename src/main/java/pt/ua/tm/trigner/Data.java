package pt.ua.tm.trigner;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import pt.ua.tm.gimli.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 04/01/13
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public class Data {

    public static InstanceList readDirectory(File directory, Pipe pipe) {
        return readDirectories(new File[]{directory}, pipe);
    }

    public static InstanceList readDirectories(File[] directories, Pipe pipe) {

        // Construct a file iterator, starting with the
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the
        //   filename to produce a class label. In this case, I've
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
                new FileIterator(directories,
                        new FileUtil.Filter(new String[]{"conll"}),
                        FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        int instanceCounter = 0;
        // Now process each instance provided by the iterator.
        for (File file : iterator.getFileArray()) {
            String text = getTextFromFile(file);
            String[] lineGroups = getLineGroups(text);

            for (String lineGroup : lineGroups) {
                instances.addThruPipe(new Instance(lineGroup, "", instanceCounter++, ""));
            }

        }


//        instances.addThruPipe(iterator);

        return instances;
    }

    private static String getTextFromFile(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("There was a problem reading the file: " + file.getName(), e);
        }
    }

    private static String[] getLineGroups(String text) {

        List<String> lineGroups = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        String[] lines = text.split("\\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) { //Empty Line
                sb.append("\n");
                lineGroups.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(line);
                sb.append("\n");
            }
        }

        return lineGroups.toArray(new String[]{});
    }
}
