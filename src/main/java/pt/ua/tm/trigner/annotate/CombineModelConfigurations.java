package pt.ua.tm.trigner.annotate;

import pt.ua.tm.trigner.model.transformer.IntegerTransformer;
import pt.ua.tm.trigner.configuration.ModelConfiguration;
import pt.ua.tm.trigner.shared.CustomHashSet;
import pt.ua.tm.trigner.shared.Types;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/8/13
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class CombineModelConfigurations {

    private enum PropertyType {
        Boolean,
        Integer,
        Array,
        Context,
        Type
    }

    public static ModelConfiguration combineModelConfigurations(final List<ModelConfiguration> modelConfigurations) {

        if (modelConfigurations.size() < 2) {
            throw new RuntimeException("Please provide 2 or more model configurations.");
        }

        Set<String> properties = modelConfigurations.get(0).stringPropertyNames();
        ModelConfiguration modelConfiguration = new ModelConfiguration();

        for (String property : properties) {

            PropertyType propertyType;
            if (property.equals("context")) {
                propertyType = PropertyType.Context;
            } else if (property.contains("_order")) {
                propertyType = PropertyType.Integer;
            } else if (property.contains("_length") || property.contains("_sizes")) {
                propertyType = PropertyType.Array;
            } else if (property.contains("_type")) {
                propertyType = PropertyType.Type;
            } else {
                propertyType = PropertyType.Boolean;
            }

            boolean useFeature = false;
            int intValue = Integer.MIN_VALUE;
            Set<Object> types = new HashSet<>();
            Set<Object> contexts = new HashSet<>();
            Set<Integer> arrays = new HashSet<>();

            for (ModelConfiguration mc : modelConfigurations) {
                String value = mc.getProperty(property);

                switch (propertyType) {

                    case Boolean:
                        if (Boolean.parseBoolean(value)) {
                            useFeature = true;
                        }
                        break;
                    case Integer:
                        if (Integer.parseInt(value) > intValue) {
                            intValue = Integer.parseInt(value);
                        }
                        break;
                    case Array:
                        Set<Integer> set = new CustomHashSet<>(value, new IntegerTransformer());
                        for (int a : set) {
                            arrays.add(a);
                        }
                        break;
                    case Context:
                        contexts.add(ModelConfiguration.ContextType.valueOf(value));
                        break;
                    case Type:
                        types.add(Types.VertexFeatureType.valueOf(value));
                        break;
                }
            }

            // Set union on unified Model OldConfiguration
            switch (propertyType) {
                case Boolean:
                    modelConfiguration.setProperty(property, Boolean.toString(useFeature));
                    break;
                case Integer:
                    modelConfiguration.setProperty(property, Integer.toString(intValue));
                    break;
                case Array:
                    // Sort array
                    List<Integer> larrays = new ArrayList<>();
                    larrays.addAll(arrays);
                    Collections.sort(larrays);
                    // Convert to objects
                    List<Object> lobjs = new ArrayList<>();
                    lobjs.addAll(larrays);
                    modelConfiguration.setProperty(property, getArrayString(lobjs));
                    break;
                case Context:
                    modelConfiguration.setProperty(property, getArrayString(contexts));
                    break;
                case Type:
                    modelConfiguration.setProperty(property, getArrayString(types));
                    break;
            }
        }


        return modelConfiguration;
    }


    private static String getArrayString(Set<Object> objects) {
        List<Object> larrays = new ArrayList<>();
        larrays.addAll(objects);
        return getArrayString(larrays);
    }

    private static String getArrayString(List<Object> objects) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object.toString());
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}
