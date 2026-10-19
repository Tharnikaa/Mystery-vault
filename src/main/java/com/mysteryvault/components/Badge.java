package com.mysteryvault.components;

import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Badge extends JComponent {

    public enum BadgeType {
        SOLVED,
        IN_PROGRESS,
        LOCKED,
        COMPLETE,
        CONFLICT
    }

    private String text;
    private BadgeType type;

    public Badge(String text, BadgeType type) {
        this.text = text;
        this.type = type;
        setFont(Theme.bold(10));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 16;
        int h = fm.getHeight() + 6;
        return new Dimension(w, Math.max(20, h));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        Color bg, border, fg;

        switch (type) {
            case SOLVED:
            case COMPLETE:
                bg = new Color(20, 65, 45, 230);
                border = Theme.GREEN_SOLVED;
                fg = Theme.GREEN_SOLVED;
                break;
            case IN_PROGRESS:
                bg = new Color(75, 52, 20, 230);
                border = Theme.GOLD_PRIMARY;
                fg = Theme.GOLD_LIGHT;
                break;
            case CONFLICT:
                bg = Theme.RED_CONFLICT_BG;
                border = Theme.RED_CONFLICT;
                fg = Theme.RED_CONFLICT;
                break;
            case LOCKED:
            default:
                bg = new Color(24, 30, 42, 220);
                border = Theme.BORDER_CARD;
                fg = Theme.TEXT_MUTED;
                break;
        }

        RoundRectangle2D round = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8);
        g2.setColor(bg);
        g2.fill(round);

        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(round);

        g2.setColor(fg);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
