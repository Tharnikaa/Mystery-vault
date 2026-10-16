package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ConnectionEngineScreen extends JPanel {

    private static class Node {
        String label;
        int x, y;
        int radius;
        Color color;
        String imageKey;
        boolean isCenter;

        Node(String label, int x, int y, int radius, Color color, String imageKey, boolean isCenter) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.imageKey = imageKey;
            this.isCenter = isCenter;
        }
    }

    private static class Link {
        int from, to;
        Color color;
        boolean isGoldGlow;

        Link(int from, int to, Color color, boolean isGoldGlow) {
            this.from = from;
            this.to = to;
            this.color = color;
            this.isGoldGlow = isGoldGlow;
        }
    }

    private final List<Node> nodes = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();

    public ConnectionEngineScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initGraph();
        initUI();
    }

    private void initGraph() {
        // Node 0: VICTIM (Center)
        nodes.add(new Node("VICTIM", 270, 220, 26, Theme.TEXT_PRIMARY, null, true));

        // Node 1: ARJUN (Top)
        nodes.add(new Node("ARJUN", 270, 70, 22, Theme.TEXT_SECONDARY, "suspect_arjun.jpg", false));

        // Node 2: MEERA (Top-Right)
        nodes.add(new Node("MEERA", 420, 110, 24, Theme.GOLD_PRIMARY, "suspect_meera.png", false));

        // Node 3: ROHAN (Right)
        nodes.add(new Node("ROHAN", 440, 260, 22, Theme.TEXT_SECONDARY, "suspect_rohan.png", false));

        // Node 4: E04 (Bottom-Right)
        nodes.add(new Node("E04", 390, 370, 20, Theme.TEAL_ACCENT, null, false));

        // Node 5: TIMELINE (Bottom)
        nodes.add(new Node("TIMELINE", 270, 390, 20, Theme.TEXT_SECONDARY, null, false));

        // Node 6: E03 (Bottom-Left)
        nodes.add(new Node("E03", 150, 360, 20, Theme.TEAL_ACCENT, null, false));

        // Node 7: E01 (Left)
        nodes.add(new Node("E01", 100, 220, 20, Theme.GOLD_PRIMARY, null, false));

        // Links
        links.add(new Link(0, 1, Theme.BORDER_CARD, false));
        links.add(new Link(0, 2, Theme.GOLD_PRIMARY, true)); // Strong link to Meera
        links.add(new Link(0, 3, Theme.BORDER_CARD, false));
        links.add(new Link(0, 5, Theme.BORDER_CARD, false));
        links.add(new Link(0, 7, Theme.GOLD_PRIMARY, true)); // Strong link to E01 Wine Glass
        links.add(new Link(2, 7, Theme.GOLD_PRIMARY, true)); // Meera to Wine Glass
        links.add(new Link(1, 2, Theme.BORDER_CARD, false));
        links.add(new Link(0, 4, Theme.BORDER_CARD, false));
        links.add(new Link(0, 6, Theme.BORDER_CARD, false));
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("9. CASE CONNECTION ENGINE");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Center / Left: Interactive Graph Node Canvas
        JPanel graphCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                // Draw background grid lines subtle
                g2.setColor(new Color(25, 33, 48, 80));
                for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);

                // Draw Links
                for (Link link : links) {
                    Node from = nodes.get(link.from);
                    Node to = nodes.get(link.to);

                    if (link.isGoldGlow) {
                        g2.setColor(new Color(212, 163, 89, 70));
                        g2.setStroke(new BasicStroke(4.0f));
                        g2.drawLine(from.x, from.y, to.x, to.y);

                        g2.setColor(Theme.GOLD_PRIMARY);
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.drawLine(from.x, from.y, to.x, to.y);
                    } else {
                        g2.setColor(link.color);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(from.x, from.y, to.x, to.y);
                    }
                }

                // Draw Nodes
                for (Node node : nodes) {
                    int r = node.radius;

                    if (node.isCenter) {
                        // Center Victim Node
                        g2.setColor(new Color(255, 255, 255, 40));
                        g2.fill(new Ellipse2D.Float(node.x - r - 6, node.y - r - 6, (r + 6) * 2, (r + 6) * 2));

                        g2.setColor(Color.WHITE);
                        g2.fill(new Ellipse2D.Float(node.x - r, node.y - r, r * 2, r * 2));

                        // Silhouette icon
                        g2.setColor(Theme.CARD_BG_SOLID);
                        g2.fillOval(node.x - 7, node.y - 12, 14, 14);
                        g2.fillArc(node.x - 14, node.y + 1, 28, 20, 0, 180);
                    } else {
                        // Image or avatar node
                        if (node.imageKey != null) {
                            BufferedImage img = AssetManager.getImage(node.imageKey);
                            BufferedImage circ = AssetManager.getCircularImage(img, r * 2, node.color, 2);
                            g2.drawImage(circ, node.x - r, node.y - r, null);
                        } else {
                            g2.setColor(Theme.CARD_BG);
                            g2.fill(new Ellipse2D.Float(node.x - r, node.y - r, r * 2, r * 2));
                            g2.setColor(node.color);
                            g2.setStroke(new BasicStroke(2.0f));
                            g2.draw(new Ellipse2D.Float(node.x - r, node.y - r, r * 2, r * 2));
                        }
                    }

                    // Label below node
                    g2.setFont(Theme.bold(11));
                    g2.setColor(node.color);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(node.label, node.x - fm.stringWidth(node.label) / 2, node.y + r + 15);
                }

                g2.dispose();
            }
        };
        graphCanvas.setOpaque(false);
        mainSplit.add(graphCanvas, BorderLayout.CENTER);

        // Right Sidebar: CONNECTIONS FOUND Card
        GlassPanel rightCard = new GlassPanel(new BorderLayout(0, 16));
        rightCard.setPreferredSize(new Dimension(320, 520));
        rightCard.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));

        JLabel titleLbl = new JLabel("CONNECTIONS FOUND");
        titleLbl.setFont(Theme.titleFont(16));
        titleLbl.setForeground(Theme.TEXT_GOLD);

        JPanel itemsBox = new JPanel();
        itemsBox.setLayout(new BoxLayout(itemsBox, BoxLayout.Y_AXIS));
        itemsBox.setOpaque(false);

        itemsBox.add(createConnectionRow("Shared Location – Restaurant", Theme.TEXT_PRIMARY));
        itemsBox.add(Box.createRigidArea(new Dimension(0, 14)));
        itemsBox.add(createConnectionRow("Time Overlap – 08:40 – 08:50 PM", Theme.TEAL_ACCENT));
        itemsBox.add(Box.createRigidArea(new Dimension(0, 14)));
        itemsBox.add(createConnectionRow("Evidence Link – E01 -> Meera", Theme.GOLD_PRIMARY));
        itemsBox.add(Box.createRigidArea(new Dimension(0, 14)));
        itemsBox.add(createConnectionRow("Statement Conflict – 1", Theme.RED_CONFLICT));

        CustomButton btnRun = new CustomButton("RUN ANALYSIS", CustomButton.ButtonStyle.GOLD);
        btnRun.setPreferredSize(new Dimension(280, 42));
        btnRun.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_ANALYSIS));

        rightCard.add(titleLbl, BorderLayout.NORTH);
        rightCard.add(itemsBox, BorderLayout.CENTER);
        rightCard.add(btnRun, BorderLayout.SOUTH);

        mainSplit.add(rightCard, BorderLayout.EAST);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createConnectionRow(String text, Color accentColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel bullet = new JLabel("•");
        bullet.setFont(Theme.bold(16));
        bullet.setForeground(accentColor);

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(Theme.regular(12));
        textLbl.setForeground(Theme.TEXT_PRIMARY);

        row.add(bullet);
        row.add(textLbl);
        return row;
    }
}
