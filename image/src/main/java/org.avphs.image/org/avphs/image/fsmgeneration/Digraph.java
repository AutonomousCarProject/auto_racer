package org.avphs.image.fsmgeneration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * class for making the nice FSM graph images (with GraphVis)
 */
public class Digraph {

    /**
     * mapping from the {@link org.avphs.image.ImageProcessing.PosterColor} codes to
     * GraphVis-compatible color names
     */
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

    /**
     * a statement in the GraphVis dot format that specifies an 'edge' (an arrow in our case)
     */
    private ArrayList<EdgeStatement> edgeStatements;
    /**
     * a statement in the GraphVis dot format that specifies attributes of a 'node' (a state in our case).
     * for example, the shape or color of the node.
     */
    private ArrayList<NodeStatement> nodeStatements;
    
    public Digraph() {
        edgeStatements = new ArrayList<>();
        nodeStatements = new ArrayList<>();
    }

    /**
     * add an edge statement to the graph.
     * <br/>
     * if an edge between the same nodes already exists in {@code this.edgeStatements},
     * then the colors, labels, and other attributes of {@code edgeStatement} are merged
     * into the existing edge.
     * 
     * @param edgeStatement the edge statement to add
     */
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

    /**
     * add a node statement to the graph
     * <br/>
     * if a node statement for the same node already exists in {@code this.edgeStatements},
     * the two statements are merged
     * 
     * @param nodeStatement the node statement to add
     */
    public void addNodeStatement(NodeStatement nodeStatement) {
        for (NodeStatement statement : nodeStatements) {
            if (statement.getNodeName().equals(nodeStatement.getNodeName())) {
                statement.getAttributes().putAll(nodeStatement.getAttributes());
                return;
            }
        }
        nodeStatements.add(nodeStatement);
    }

    /**
     * makes a string in the GraphVis dot format to represent this graph
     * 
     * @return string representation of the digraph
     */
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

    /**
     * runs GraphVis to generate a .svg image file of the graph.
     * <br/>
     * note: this requires you to have GraphVis downloaded and installed,
     * and the path to the executable will probably be different for you.
     * 
     * @param name file name and name added to the top of the image
     * @param path path for saving the file
     */
    public void outputImage(String name, String path) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                "C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe",  // change this path to point to your dot.exe
                "-Glabelloc=t",  // put graph labels at the top
                "-Glabel=\"" + name + "\"",  // add graph label
                "-Epenwidth=4",  // make edges thicker
                "-Nshape=circle",  // make nodes circles
                "-Tsvg",  // make .svg
                "-o" + path + "\\" + name + ".svg"  // output path
        );

        try {
            Process process = processBuilder.start();
            // can throw text at the process like this (pretty cool imo)
            // this worked first try btw
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(this.toString().getBytes());
            // not sure if needed but it can't hurt
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
