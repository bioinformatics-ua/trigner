package pt.ua.tm.trigner.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/22/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectConfiguration implements Configuration {
    private List<EventGroup> groups;
    private List<String> events;
    private List<String> concepts;

    public ProjectConfiguration() {
        this.groups = new ArrayList<>();
        this.concepts = new ArrayList<>();
        this.events = null;
    }

    public ProjectConfiguration(List<EventGroup> groups, List<String> concepts) {
        this.groups = groups;
        this.concepts = concepts;
    }

    public List<EventGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<EventGroup> groups) {
        this.groups = groups;
        this.events = getEventsList();
    }

    public List<String> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<String> concepts) {
        this.concepts = concepts;
    }

    public List<String> getEvents() {
        if (events == null){
            events = getEventsList();
        }
        return events;
    }

    private List<String> getEventsList() {
        List<String> events = new ArrayList<>();
        for (EventGroup group : groups) {
            events.addAll(group.getEvents());
        }
        return events;
    }

    @Override
    public void read(final InputStream inputStream) throws IOException {
        // Load data
        InputStreamReader isr = new InputStreamReader(inputStream);
        Gson gson = new GsonBuilder().create();
        ProjectConfiguration projectConfiguration = gson.fromJson(isr, ProjectConfiguration.class);

        // Close stream
        inputStream.close();

        // Set
        this.setGroups(projectConfiguration.getGroups());
        this.setConcepts(projectConfiguration.getConcepts());
    }

    @Override
    public void write(final OutputStream outputStream) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        outputStream.write(gson.toJson(this).getBytes());
        outputStream.close();
    }


}
