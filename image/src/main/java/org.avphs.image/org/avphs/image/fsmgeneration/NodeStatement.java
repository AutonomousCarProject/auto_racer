package org.avphs.image.fsmgeneration;

import java.util.HashMap;
import java.util.Map;

public class NodeStatement {
    private String nodeName;
    private HashMap<String, String> attributes;
    
    public NodeStatement(String nodeName) {
        this.nodeName = nodeName;
        
        this.attributes = new HashMap<>();
    }

    public String getNodeName() {
        return nodeName;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"")
                .append(nodeName)
        .append("\"");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            builder.append(" [");
            builder.append(entry.getKey()).append(" = ").append(entry.getValue()).append("]");
        }
        
        return builder.toString();
    }
}
