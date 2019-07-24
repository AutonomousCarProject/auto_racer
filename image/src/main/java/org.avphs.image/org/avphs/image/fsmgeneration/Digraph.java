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
    
    private ArrayList<EdgeStatement> entries;
    
    public Digraph() {
        entries = new ArrayList<>();
    }
    
    public void addEntry(EdgeStatement edgeStatement) {
        for (EdgeStatement entry : entries) {
            if (entry.isSameEdge(edgeStatement)) {
                entry.getColors().addAll(edgeStatement.getColors());
                entry.getLabels().addAll(edgeStatement.getLabels());
                entry.getAttributes().putAll(edgeStatement.getAttributes());
                return;
            }
        }
        entries.add(edgeStatement);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        for (EdgeStatement entry : entries) {
            builder.append(entry.toString());
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
}
