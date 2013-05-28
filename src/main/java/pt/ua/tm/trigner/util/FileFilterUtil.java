package pt.ua.tm.trigner.util;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/24/13
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileFilterUtil {

    public static FileFilter newFileFilter(String wildcardFilter) {
        List<String> wildcards = new ArrayList<>();

        if (StringUtils.isNotBlank(wildcardFilter)) {
            wildcards.add(wildcardFilter);
        }

        if (wildcards.isEmpty()) {
            wildcards.add("*");
        }

        return new AndFileFilter(new WildcardFileFilter(wildcards), HiddenFileFilter.VISIBLE);
    }
}
