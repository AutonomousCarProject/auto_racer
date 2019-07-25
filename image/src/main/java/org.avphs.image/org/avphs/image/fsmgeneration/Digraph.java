package org.avphs.image.fsmgeneration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Digraph {
    public static final String[] colorNameMap = {
            "red",
            "green",
            "blue",
            "cyan",
            "magenta",
            "yellow",
            "black",
            "gray20",
            "gray40",
            "gray60",
            "gray80",
            "gray90"
    };
    
    private ArrayList<EdgeStatement> edgeStatements;
    private ArrayList<NodeStatement> nodeStatements;
    
    public Digraph() {
        edgeStatements = new ArrayList<>();
        nodeStatements = new ArrayList<>();
    }
    
    public void addEntry(EdgeStatement edgeStatement) {
        for (EdgeStatement entry : edgeStatements) {
            if (entry.isSameEdge(edgeStatement)) {
                entry.getColors().addAll(edgeStatement.getColors());
                entry.getLabels().addAll(edgeStatement.getLabels());
                entry.getAttributes().putAll(edgeStatement.getAttributes());
                return;
            }
        }
        edgeStatements.add(edgeStatement);
    }
    
    public void addNodeStatement(NodeStatement nodeStatement) {
        for (NodeStatement statement : nodeStatements) {
            if (statement.getNodeName().equals(nodeStatement.getNodeName())) {
                statement.getAttributes().putAll(nodeStatement.getAttributes());
                return;
            }
        }
        nodeStatements.add(nodeStatement);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        for (EdgeStatement entry : edgeStatements) {
            builder.append(entry.toString());
            builder.append("\n");
        }
        for (NodeStatement nodeStatement : nodeStatements) {
            builder.append(nodeStatement.toString());
            builder.append("\n");
        }
        builder.append("}");
        
        return builder.toString();
    }

    public void outputImage(String name) {
        outputImage(name, System.getProperty("user.dir"));
    }

    public void outputImage(String name, String path) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                "C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe",
                "-Glabelloc=t",
                "-Glabel=\"" + name + "\"",
                "-Epenwidth=4",
                "-Nshape=circle",
                "-Tsvg",
                "-o" + path + "\\" + name + ".svg"
        );

        try {
            Process process = processBuilder.start();
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(this.toString().getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String quote(String s) {
        return "\"" + s + "\"";
    }
}
