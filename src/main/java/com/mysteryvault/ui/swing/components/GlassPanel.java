package com.mysteryvault.ui.swing.components;

import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GlassPanel extends JPanel {

    private int cornerRadius = 14;
    private Color backgroundColor = Theme.CARD_BG;
    private Color borderColor = Theme.BORDER_CARD;
    private boolean drawBorder = true;
    private boolean selected = false;

    public GlassPanel() {
        setOpaque(false);
    }

    public GlassPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);

        g2.setColor(backgroundColor);
        g2.fill(shape);

        if (drawBorder) {
            if (selected) {
                g2.setColor(Theme.BORDER_GOLD);
                g2.setStroke(new BasicStroke(2.0f));
            } else {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.draw(shape);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
