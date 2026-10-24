package com.mysteryvault.ui.swing.theme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Theme {

    // Primary Backgrounds
    public static final Color BG_DARK = new Color(13, 17, 26);
    public static final Color BG_OVERLAY = new Color(9, 12, 19, 210);
    public static final Color CARD_BG = new Color(19, 25, 38, 235);
    public static final Color CARD_BG_HOVER = new Color(26, 35, 52, 245);
    public static final Color CARD_BG_SOLID = new Color(19, 25, 38);
    public static final Color CARD_INNER = new Color(13, 18, 28, 220);

    // Borders
    public static final Color BORDER_SUBTLE = new Color(42, 54, 76);
    public static final Color BORDER_CARD = new Color(48, 64, 90);
    public static final Color BORDER_GOLD = new Color(212, 163, 89);
    public static final Color BORDER_TEAL = new Color(25, 195, 184);

    // Accent Colors
    public static final Color GOLD_PRIMARY = new Color(212, 163, 89);
    public static final Color GOLD_LIGHT = new Color(240, 196, 125);
    public static final Color GOLD_DARK = new Color(165, 120, 52);
    public static final Color TEAL_ACCENT = new Color(25, 195, 184);
    public static final Color TEAL_GLOW = new Color(25, 195, 184, 90);
    public static final Color RED_CONFLICT = new Color(225, 75, 75);
    public static final Color RED_CONFLICT_BG = new Color(60, 20, 24, 210);
    public static final Color GREEN_SOLVED = new Color(38, 185, 128);
    public static final Color ORANGE_WARNING = new Color(235, 130, 45);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(242, 245, 248);
    public static final Color TEXT_SECONDARY = new Color(165, 180, 200);
    public static final Color TEXT_MUTED = new Color(105, 122, 145);
    public static final Color TEXT_GOLD = new Color(229, 184, 105);
    public static final Color TEXT_DARK = new Color(18, 18, 18);

    // Button Colors
    public static final Color BTN_DARK_BG = new Color(24, 32, 48);
    public static final Color BTN_DARK_HOVER = new Color(36, 48, 70);
    public static final Color BTN_DARK_BORDER = new Color(55, 72, 100);

    // Fonts
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, (int) size);
    }

    public static Font titleFont(float size) {
        return new Font(FONT_FAMILY, Font.BOLD, (int) size);
    }

    public static Font bold(float size) {
        return new Font(FONT_FAMILY, Font.BOLD, (int) size);
    }

    public static Font regular(float size) {
        return new Font(FONT_FAMILY, Font.PLAIN, (int) size);
    }

    // High Quality Graphics Hints
    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    // Drawing Helpers
    public static void drawRoundedCard(Graphics2D g2, int x, int y, int w, int h, int arc, Color bg, Color border) {
        RoundRectangle2D round = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        if (bg != null) {
            g2.setColor(bg);
            g2.fill(round);
        }
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(round);
        }
    }

    public static void drawGoldGradientCard(Graphics2D g2, int x, int y, int w, int h, int arc, boolean isSelected) {
        RoundRectangle2D round = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
        g2.setColor(CARD_BG);
        g2.fill(round);

        if (isSelected) {
            g2.setColor(new Color(212, 163, 89, 40));
            g2.fill(round);
            g2.setColor(BORDER_GOLD);
            g2.setStroke(new BasicStroke(1.8f));
        } else {
            g2.setColor(BORDER_CARD);
            g2.setStroke(new BasicStroke(1.2f));
        }
        g2.draw(round);
    }
}
