package pt.ua.tm.trigner.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/22/13
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventGroup {
    private String name;
    private List<String> events;

    public EventGroup(String name, List<String> events) {
        this.name = name;
        this.events = events;
    }

    public EventGroup(String name) {
        this.name = name;
        this.events = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventGroup that = (EventGroup) o;

        if (events != null ? !events.equals(that.events) : that.events != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (events != null ? events.hashCode() : 0);
        return result;
    }
}
