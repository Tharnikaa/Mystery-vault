package com.mysteryvault.ui.swing.components;

import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ScreenHeader extends JPanel {

    private final String title;
    private final boolean showBackButton;
    private final CustomButton backBtn;

    public ScreenHeader(String title) {
        this(title, true);
    }

    public ScreenHeader(String title, boolean showBackButton) {
        this.title = title;
        this.showBackButton = showBackButton;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        backBtn = new CustomButton("<", CustomButton.ButtonStyle.ICON_BACK);
        backBtn.setPreferredSize(new Dimension(34, 34));
        backBtn.addActionListener(e -> ScreenManager.getInstance().goBack());

        if (showBackButton) {
            leftPanel.add(backBtn);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.bold(17));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        leftPanel.add(titleLabel);

        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int dotSize = 10;
                int dotX = getWidth() - 20;
                int dotY = (getHeight() - dotSize) / 2;

                g2.setColor(Theme.TEAL_GLOW);
                g2.fill(new Ellipse2D.Float(dotX - 3, dotY - 3, dotSize + 6, dotSize + 6));

                g2.setColor(Theme.TEAL_ACCENT);
                g2.fill(new Ellipse2D.Float(dotX, dotY, dotSize, dotSize));

                g2.dispose();
            }
        };
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(50, 34));
        add(rightPanel, BorderLayout.EAST);
    }

    public String getTitle() {
        return title;
    }
}
