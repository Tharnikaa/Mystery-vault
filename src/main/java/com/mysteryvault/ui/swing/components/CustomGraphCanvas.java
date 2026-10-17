package com.mysteryvault.ui.swing.components;

import com.mysteryvault.model.Connection;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.GraphNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Module 4 — Custom Java 2D Clue Network Graph Canvas.
 *
 * Owned by Member 4.
 * Renders nodes and connections provided via setGraph(nodes, connections).
 * Supports mouse selection, highlight rendering, and clean separation from investigation logic.
 */
public class CustomGraphCanvas extends JComponent {

    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private GraphNode selectedNode = null;
    private Consumer<GraphNode> selectionListener = null;

    public CustomGraphCanvas() {
        setOpaque(false);
        initMouseListener();
    }

    private void initMouseListener() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GraphNode clicked = findNodeAt(e.getX(), e.getY());
                if (clicked != null) {
                    selectedNode = clicked;
                    repaint();
                    if (selectionListener != null) {
                        selectionListener.accept(clicked);
                    }
                }
            }
        };
        addMouseListener(ma);
    }

    public void setGraph(List<GraphNode> newNodes, List<Connection> newConnections) {
        this.nodes.clear();
        if (newNodes != null) {
            this.nodes.addAll(newNodes);
        }
        this.connections.clear();
        if (newConnections != null) {
            this.connections.addAll(newConnections);
        }
        if (!this.nodes.isEmpty()) {
            this.selectedNode = this.nodes.get(0);
        }
        repaint();
    }

    public void setNodeSelectionListener(Consumer<GraphNode> listener) {
        this.selectionListener = listener;
    }

    public void setSelectedNode(GraphNode node) {
        this.selectedNode = node;
        repaint();
    }

    public GraphNode getSelectedNode() {
        return selectedNode;
    }

    private GraphNode findNodeAt(int x, int y) {
        for (GraphNode node : nodes) {
            int dx = x - node.getX();
            int dy = y - node.getY();
            if (dx * dx + dy * dy <= (node.getRadius() + 6) * (node.getRadius() + 6)) {
                return node;
            }
        }
        return null;
    }

    private GraphNode findNodeById(String id) {
        if (id == null) return null;
        for (GraphNode n : nodes) {
            if (id.equalsIgnoreCase(n.getId())) {
                return n;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        // 1. Subtle background grid pattern
        g2.setColor(new Color(25, 33, 48, 80));
        for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);

        // 2. Draw Connections / Links
        for (Connection conn : connections) {
            GraphNode from = findNodeById(conn.getSourceId());
            GraphNode to = findNodeById(conn.getTargetId());
            if (from == null || to == null) continue;

            boolean isLinkedToSelected = selectedNode != null &&
                    (selectedNode.getId().equalsIgnoreCase(from.getId()) || selectedNode.getId().equalsIgnoreCase(to.getId()));

            if (conn.isStrongLead() || isLinkedToSelected) {
                // Outer glow
                g2.setColor(new Color(212, 163, 89, isLinkedToSelected ? 90 : 60));
                g2.setStroke(new BasicStroke(isLinkedToSelected ? 5.0f : 3.5f));
                g2.drawLine(from.getX(), from.getY(), to.getX(), to.getY());

                // Inner core
                g2.setColor(Theme.GOLD_PRIMARY);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawLine(from.getX(), from.getY(), to.getX(), to.getY());
            } else {
                g2.setColor(Theme.BORDER_CARD);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawLine(from.getX(), from.getY(), to.getX(), to.getY());
            }
        }

        // 3. Draw Nodes
        for (GraphNode node : nodes) {
            int r = node.getRadius();
            int nx = node.getX();
            int ny = node.getY();
            boolean isSelected = (selectedNode != null && selectedNode.getId().equalsIgnoreCase(node.getId()));

            // Selected node halo glow
            if (isSelected) {
                g2.setColor(new Color(212, 163, 89, 70));
                g2.fill(new Ellipse2D.Float(nx - r - 8, ny - r - 8, (r + 8) * 2, (r + 8) * 2));
            }

            if (node.isCenter()) {
                // Center Victim Node
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fill(new Ellipse2D.Float(nx - r - 5, ny - r - 5, (r + 5) * 2, (r + 5) * 2));

                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Float(nx - r, ny - r, r * 2, r * 2));

                // Silhouette icon
                g2.setColor(Theme.CARD_BG_SOLID);
                g2.fillOval(nx - 7, ny - 12, 14, 14);
                g2.fillArc(nx - 14, ny + 1, 28, 20, 0, 180);
            } else {
                // Suspect / Evidence / Clue node
                if (node.getImageKey() != null) {
                    BufferedImage img = AssetManager.getImage(node.getImageKey());
                    Color border = isSelected ? Theme.BORDER_GOLD : node.getColor();
                    BufferedImage circ = AssetManager.getCircularImage(img, r * 2, border, isSelected ? 3 : 2);
                    g2.drawImage(circ, nx - r, ny - r, null);
                } else {
                    g2.setColor(Theme.CARD_BG);
                    g2.fill(new Ellipse2D.Float(nx - r, ny - r, r * 2, r * 2));

                    Color border = isSelected ? Theme.BORDER_GOLD : node.getColor();
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.8f));
                    g2.draw(new Ellipse2D.Float(nx - r, ny - r, r * 2, r * 2));

                    // Small category indicator dot
                    g2.setColor(node.getColor());
                    g2.fillOval(nx - 4, ny - 4, 8, 8);
                }
            }

            // Node Label
            g2.setFont(Theme.bold(isSelected ? 12 : 11));
            g2.setColor(isSelected ? Theme.TEXT_GOLD : node.getColor());
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(node.getLabel());
            g2.drawString(node.getLabel(), nx - textW / 2, ny + r + 15);
        }

        g2.dispose();
    }
}
