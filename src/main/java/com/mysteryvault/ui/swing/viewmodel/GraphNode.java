package com.mysteryvault.ui.swing.viewmodel;

import java.awt.Color;

/**
 * Module 4 presentation node for CustomGraphCanvas rendering.
 */
public class GraphNode {
    private final String id;
    private final String label;
    private int x;
    private int y;
    private final int radius;
    private final Color color;
    private final String imageKey;
    private final boolean isCenter;
    private final String nodeType; // SUSPECT, EVIDENCE, CLUE, VICTIM, TIMELINE

    public GraphNode(String id, String label, int x, int y, int radius, Color color, String imageKey, boolean isCenter, String nodeType) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        this.imageKey = imageKey;
        this.isCenter = isCenter;
        this.nodeType = nodeType;
    }

    public GraphNode(String id, String label, int x, int y, int radius, Color color, String imageKey, boolean isCenter) {
        this(id, label, x, y, radius, color, imageKey, isCenter, isCenter ? "VICTIM" : "SUSPECT");
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getRadius() { return radius; }
    public Color getColor() { return color; }
    public String getImageKey() { return imageKey; }
    public boolean isCenter() { return isCenter; }
    public String getNodeType() { return nodeType; }
}
