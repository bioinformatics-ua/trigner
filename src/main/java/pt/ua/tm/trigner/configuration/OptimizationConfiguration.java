package pt.ua.tm.trigner.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pt.ua.tm.trigner.shared.Types;

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
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptimizationConfiguration implements Configuration {
    private List<Types.Feature> features;
    private List<ModelConfiguration.ContextType> contexts;
    private List<int[]> ngrams;
    private List<Integer> hops;
    private List<Types.VertexFeatureType> vertex;
    private List<Integer> orders;

    public OptimizationConfiguration(List<Types.Feature> features, List<ModelConfiguration.ContextType> contexts,
                                     List<int[]> ngrams, List<Integer> hops,
                                     List<Types.VertexFeatureType> vertex, List<Integer> orders) {
        this.features = features;
        this.contexts = contexts;
        this.ngrams = ngrams;
        this.hops = hops;
        this.vertex = vertex;
        this.orders = orders;
    }

    public OptimizationConfiguration() {
        this.features = new ArrayList<>();
        this.contexts = new ArrayList<>();
        this.ngrams = new ArrayList<>();
        this.hops = new ArrayList<>();
        this.vertex = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public List<Types.Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Types.Feature> features) {
        this.features = features;
    }

    public List<ModelConfiguration.ContextType> getContexts() {
        return contexts;
    }

    public void setContexts(List<ModelConfiguration.ContextType> contexts) {
        this.contexts = contexts;
    }

    public List<int[]> getNgrams() {
        return ngrams;
    }

    public void setNgrams(List<int[]> ngrams) {
        this.ngrams = ngrams;
    }

    public List<Integer> getHops() {
        return hops;
    }

    public void setHops(List<Integer> hops) {
        this.hops = hops;
    }

    public List<Types.VertexFeatureType> getVertex() {
        return vertex;
    }

    public void setVertex(List<Types.VertexFeatureType> vertex) {
        this.vertex = vertex;
    }

    public List<Integer> getOrders() {
        return orders;
    }

    public void setOrders(List<Integer> orders) {
        this.orders = orders;
    }

    @Override
    public void read(final InputStream inputStream) throws IOException {
        // Load data
        InputStreamReader isr = new InputStreamReader(inputStream);
        Gson gson = new GsonBuilder().create();
        OptimizationConfiguration optimizationConfiguration = gson.fromJson(isr, OptimizationConfiguration.class);

        // Close stream
        inputStream.close();

        // Set
        this.setFeatures(optimizationConfiguration.getFeatures());
        this.setContexts(optimizationConfiguration.getContexts());
        this.setNgrams(optimizationConfiguration.getNgrams());
        this.setHops(optimizationConfiguration.getHops());
        this.setVertex(optimizationConfiguration.getVertex());
        this.setOrders(optimizationConfiguration.getOrders());
    }

    @Override
    public void write(final OutputStream outputStream) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        outputStream.write(gson.toJson(this).getBytes());
        outputStream.close();
    }

}
