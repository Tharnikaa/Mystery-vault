package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.CustomButton;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                BufferedImage deskImg = AssetManager.getImage("menu_desk.jpg");
                if (deskImg != null) {
                    g2.drawImage(deskImg, 0, 0, w, h, null);
                }

                GradientPaint gp = new GradientPaint(
                        w * 0.35f, 0, new Color(13, 17, 26, 140),
                        w * 0.75f, 0, new Color(11, 15, 23, 245)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Top right status indicator
                int dotSize = 10;
                int dotX = w - 30;
                int dotY = 22;
                g2.setColor(Theme.TEAL_GLOW);
                g2.fill(new Ellipse2D.Float(dotX - 3, dotY - 3, dotSize + 6, dotSize + 6));
                g2.setColor(Theme.TEAL_ACCENT);
                g2.fill(new Ellipse2D.Float(dotX, dotY, dotSize, dotSize));

                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        titlePanel.setOpaque(false);

        JLabel lblMystery = new JLabel("MYSTERY");
        lblMystery.setFont(Theme.titleFont(42));
        lblMystery.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblVault = new JLabel("VAULT");
        lblVault.setFont(Theme.titleFont(42));
        lblVault.setForeground(Theme.TEXT_GOLD);

        titlePanel.add(lblMystery);
        titlePanel.add(lblVault);

        JLabel lblSubtitle = new JLabel("— Uncover the Truth —");
        lblSubtitle.setFont(Theme.regular(15));
        lblSubtitle.setForeground(Theme.TEXT_GOLD);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomButton btnNewGame = new CustomButton("NEW GAME", CustomButton.ButtonStyle.GOLD);
        btnNewGame.setPreferredSize(new Dimension(220, 44));
        btnNewGame.setMaximumSize(new Dimension(220, 44));
        btnNewGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNewGame.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.DETECTIVE_PROFILE));

        CustomButton btnLoadGame = new CustomButton("LOAD GAME", CustomButton.ButtonStyle.DARK);
        btnLoadGame.setPreferredSize(new Dimension(220, 44));
        btnLoadGame.setMaximumSize(new Dimension(220, 44));
        btnLoadGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLoadGame.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_SELECTION));

        CustomButton btnSettings = new CustomButton("SETTINGS", CustomButton.ButtonStyle.DARK);
        btnSettings.setPreferredSize(new Dimension(220, 44));
        btnSettings.setMaximumSize(new Dimension(220, 44));
        btnSettings.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSettings.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Settings: Audio [ON]  •  Display [1440x900]  •  Difficulty [Detective]",
                "Mystery Vault Settings", JOptionPane.INFORMATION_MESSAGE));

        CustomButton btnQuit = new CustomButton("QUIT", CustomButton.ButtonStyle.DARK);
        btnQuit.setPreferredSize(new Dimension(220, 44));
        btnQuit.setMaximumSize(new Dimension(220, 44));
        btnQuit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnQuit.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit Mystery Vault?",
                    "Exit Game", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        menuContainer.add(titlePanel);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 6)));
        menuContainer.add(lblSubtitle);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 36)));
        menuContainer.add(btnNewGame);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        menuContainer.add(btnLoadGame);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        menuContainer.add(btnSettings);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 14)));
        menuContainer.add(btnQuit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(menuContainer, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }
}
