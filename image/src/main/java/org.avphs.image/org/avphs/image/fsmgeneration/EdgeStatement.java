package org.avphs.image.fsmgeneration;

import org.avphs.image.ImageProcessing;

import java.util.*;

public class EdgeStatement {
    private String source;
    private String destination;
    
    private ArrayList<ImageProcessing.PosterColor> colors;
    private HashSet<String> labels;
    private HashMap<String, String> attributes;
    
    public EdgeStatement(String source, String destination) {
        this.source = source;
        this.destination = destination;

        this.colors = new ArrayList<>();
        this.labels = new HashSet<>();
        this.attributes = new HashMap<>();
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public ArrayList<ImageProcessing.PosterColor> getColors() {
        return colors;
    }

    public HashSet<String> getLabels() {
        return labels;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void addColor(ImageProcessing.PosterColor color) {
        colors.add(color);
    }

    public void addLabel(String label) {
        labels.add(label);
    }
    
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    public boolean isSameEdge(EdgeStatement other) {
        return (source.equals(other.getSource()) && destination.equals(other.getDestination()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(source);
        builder.append("\"");
        builder.append(" -> ");
        builder.append("\"");
        builder.append(destination);
        builder.append("\"");
        
        if (!colors.isEmpty()) {
            StringJoiner joiner = new StringJoiner(":", " [color = \"", "\"]");
            StringJoiner tooltip = new StringJoiner(", ", " [tooltip = \"", "\"]");
            for (ImageProcessing.PosterColor color : colors) {
                joiner.add(Digraph.colorNameMap[color.getCode()]);
                tooltip.add(color.toString());
            }
            builder.append(joiner.toString());
            builder.append(tooltip.toString());
        }
        
        if (!labels.isEmpty()) {
            StringJoiner joiner = new StringJoiner("\\n", " [label = \"", "\"]");
            for (String label : labels) {
                joiner.add(label);
            }
            builder.append(joiner.toString());
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            builder.append(" ");
            builder.append("[").append(entry.getKey()).append(" = ").append(entry.getValue()).append("]");
        }
        
        return builder.toString();
    }
}
