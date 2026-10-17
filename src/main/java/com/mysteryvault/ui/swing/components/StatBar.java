package com.mysteryvault.ui.swing.components;

import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatBar extends JComponent {

    private int progress = 0; // 0 to 100
    private Color fillColor = Theme.GOLD_PRIMARY;
    private Color trackColor = new Color(25, 34, 48);
    private boolean showText = false;
    private String labelSuffix = "%";

    public StatBar(int progress, Color fillColor) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.fillColor = fillColor;
        setPreferredSize(new Dimension(140, 10));
    }

    public StatBar(int progress) {
        this(progress, Theme.GOLD_PRIMARY);
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        repaint();
    }

    public int getProgress() {
        return progress;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
        repaint();
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();
        int barH = showText ? 8 : h;
        int barY = (h - barH) / 2;

        RoundRectangle2D track = new RoundRectangle2D.Float(0, barY, w, barH, barH, barH);
        g2.setColor(trackColor);
        g2.fill(track);

        if (progress > 0) {
            float fillW = (w * progress) / 100.0f;
            RoundRectangle2D fill = new RoundRectangle2D.Float(0, barY, fillW, barH, barH, barH);
            g2.setColor(fillColor);
            g2.fill(fill);
        }

        if (showText) {
            g2.setFont(Theme.bold(10));
            g2.setColor(Theme.TEXT_SECONDARY);
            String str = progress + labelSuffix;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(str, w - fm.stringWidth(str), barY - 2);
        }

        g2.dispose();
    }
}
