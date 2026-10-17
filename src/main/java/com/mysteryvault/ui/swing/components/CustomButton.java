package com.mysteryvault.ui.swing.components;

import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {

    public enum ButtonStyle {
        GOLD,
        DARK,
        OUTLINE_GOLD,
        DANGER,
        ICON_BACK
    }

    private ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 10;

    public CustomButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        init();
    }

    public CustomButton(String text) {
        this(text, ButtonStyle.GOLD);
    }

    private void init() {
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateFontAndSize();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private void updateFontAndSize() {
        if (style == ButtonStyle.ICON_BACK) {
            setFont(Theme.bold(16));
            setForeground(Theme.TEXT_PRIMARY);
            setPreferredSize(new Dimension(38, 38));
        } else if (style == ButtonStyle.GOLD) {
            setFont(Theme.bold(13));
            setForeground(Theme.TEXT_DARK);
            setPreferredSize(new Dimension(140, 38));
        } else {
            setFont(Theme.bold(12));
            setForeground(Theme.TEXT_PRIMARY);
            setPreferredSize(new Dimension(130, 36));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = (style == ButtonStyle.ICON_BACK) ? h : cornerRadius;
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);

        Color topColor, bottomColor, borderColor;

        switch (style) {
            case GOLD:
                if (isPressed) {
                    topColor = Theme.GOLD_DARK;
                    bottomColor = Theme.GOLD_PRIMARY;
                } else if (isHovered) {
                    topColor = Theme.GOLD_LIGHT;
                    bottomColor = Theme.GOLD_PRIMARY;
                } else {
                    topColor = Theme.GOLD_PRIMARY;
                    bottomColor = Theme.GOLD_DARK;
                }
                borderColor = Theme.GOLD_LIGHT;
                setForeground(Theme.TEXT_DARK);
                break;

            case OUTLINE_GOLD:
                if (isHovered) {
                    topColor = new Color(212, 163, 89, 45);
                    bottomColor = new Color(212, 163, 89, 65);
                } else {
                    topColor = new Color(19, 25, 38, 200);
                    bottomColor = new Color(19, 25, 38, 200);
                }
                borderColor = isHovered ? Theme.GOLD_LIGHT : Theme.GOLD_PRIMARY;
                setForeground(Theme.TEXT_GOLD);
                break;

            case DANGER:
                if (isHovered) {
                    topColor = new Color(180, 50, 50);
                    bottomColor = new Color(140, 30, 30);
                } else {
                    topColor = new Color(120, 35, 40);
                    bottomColor = new Color(80, 20, 25);
                }
                borderColor = Theme.RED_CONFLICT;
                setForeground(Theme.TEXT_PRIMARY);
                break;

            case ICON_BACK:
                if (isHovered) {
                    topColor = Theme.BTN_DARK_HOVER;
                    bottomColor = Theme.BTN_DARK_BG;
                    borderColor = Theme.BORDER_GOLD;
                } else {
                    topColor = Theme.CARD_BG_SOLID;
                    bottomColor = Theme.CARD_INNER;
                    borderColor = Theme.BORDER_CARD;
                }
                setForeground(isHovered ? Theme.TEXT_GOLD : Theme.TEXT_PRIMARY);
                break;

            case DARK:
            default:
                if (isPressed) {
                    topColor = Theme.CARD_INNER;
                    bottomColor = Theme.BTN_DARK_BG;
                } else if (isHovered) {
                    topColor = Theme.BTN_DARK_HOVER;
                    bottomColor = Theme.BTN_DARK_BG;
                } else {
                    topColor = Theme.BTN_DARK_BG;
                    bottomColor = Theme.CARD_INNER;
                }
                borderColor = isHovered ? Theme.BORDER_GOLD : Theme.BTN_DARK_BORDER;
                setForeground(Theme.TEXT_PRIMARY);
                break;
        }

        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
        g2.setPaint(gp);
        g2.fill(shape);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, arc, arc));

        FontMetrics fm = g2.getFontMetrics(getFont());
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
