package pt.ua.tm.trigner.evaluation;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class Trigger {
    private int start;
    private int end;
    private String entity;

    public Trigger(int start, int end, String entity) {
        this.start = start;
        this.end = end;
        this.entity = entity;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trigger trigger1 = (Trigger) o;

        if (end != trigger1.end) return false;
        if (start != trigger1.start) return false;
        if (!entity.equals(trigger1.entity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + entity.hashCode();
        return result;
    }
}
