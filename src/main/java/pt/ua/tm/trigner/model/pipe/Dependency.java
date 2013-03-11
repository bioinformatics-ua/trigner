package pt.ua.tm.trigner.model.pipe;

import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import cc.mallet.util.PropertyList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 05/03/13
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class Dependency {
    private int pos;
    private String tag;

    public Dependency(int pos, String tag) {
        this.pos = pos;
        this.tag = tag;
    }

    public static List<Dependency> getDependencies(TokenSequence sequence) {
        List<Dependency> dependencies = new ArrayList<>();

        for (Token token : sequence) {
            PropertyList.Iterator it = token.getFeatures().iterator();
            int pos = -1;
            String tag = null;
            while (it.hasNext()) {
                String feature = it.nextProperty().iterator().getKey();

                if (feature.contains("DEP_TOK=")) {
                    String value = feature.substring(feature.indexOf("=") + 1);
                    pos = Integer.parseInt(value);
                }
                if (feature.contains("DEP_TAG=")) {
                    String value = feature.substring(feature.indexOf("=") + 1);
                    tag = value;
                }
            }

            Dependency dependency = new Dependency(pos, tag);
            dependencies.add(dependency);
        }
        return dependencies;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dependency that = (Dependency) o;

        if (pos != that.pos) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pos;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
