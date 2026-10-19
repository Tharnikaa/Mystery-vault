package com.mysteryvault.screens;

import com.mysteryvault.components.*;
import com.mysteryvault.navigation.ScreenManager;
import com.mysteryvault.theme.AssetManager;
import com.mysteryvault.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class InterrogationScreen extends JPanel {

    private static class ChatMessage {
        boolean isDetective;
        String sender;
        String message;

        ChatMessage(boolean isDetective, String sender, String message) {
            this.isDetective = isDetective;
            this.sender = sender;
            this.message = message;
        }
    }

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private JPanel chatListPanel;
    private JTextField inputField;
    private JScrollPane chatScrollPane;

    public InterrogationScreen() {
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initChatData();
        initUI();
    }

    private void initChatData() {
        chatMessages.add(new ChatMessage(true, "Detective", "Where were you at 8:45 PM?"));
        chatMessages.add(new ChatMessage(false, "Meera", "I was speaking with Arjun near the balcony."));
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("7. INTERROGATION");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Left Panel: Suspect Profile & Vitals
        GlassPanel leftProfile = new GlassPanel();
        leftProfile.setLayout(new BoxLayout(leftProfile, BoxLayout.Y_AXIS));
        leftProfile.setPreferredSize(new Dimension(260, 520));
        leftProfile.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        // Suspect Portrait
        JPanel avatarPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(110, 110);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(110, 110);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int size = 100;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                BufferedImage img = AssetManager.getImage("suspect_meera.png");
                BufferedImage rounded = AssetManager.getRoundedImage(img, size, size, 12, Theme.BORDER_GOLD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel("MEERA");
        nameLbl.setFont(Theme.titleFont(18));
        nameLbl.setForeground(Theme.TEXT_GOLD);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Trust Meter
        JPanel trustBox = new JPanel(new BorderLayout(0, 4));
        trustBox.setOpaque(false);
        trustBox.setMaximumSize(new Dimension(200, 36));
        trustBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel trustLbl = new JLabel("Trust: 45%");
        trustLbl.setFont(Theme.bold(11));
        trustLbl.setForeground(Theme.TEAL_ACCENT);

        StatBar trustBar = new StatBar(45, Theme.TEAL_ACCENT);
        trustBar.setPreferredSize(new Dimension(180, 7));

        trustBox.add(trustLbl, BorderLayout.NORTH);
        trustBox.add(trustBar, BorderLayout.SOUTH);

        // Suspicion Meter
        JPanel suspBox = new JPanel(new BorderLayout(0, 4));
        suspBox.setOpaque(false);
        suspBox.setMaximumSize(new Dimension(200, 36));
        suspBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suspLbl = new JLabel("Suspicion: 72%");
        suspLbl.setFont(Theme.bold(11));
        suspLbl.setForeground(Theme.RED_CONFLICT);

        StatBar suspBar = new StatBar(72, Theme.RED_CONFLICT);
        suspBar.setPreferredSize(new Dimension(180, 7));

        suspBox.add(suspLbl, BorderLayout.NORTH);
        suspBox.add(suspBar, BorderLayout.SOUTH);

        leftProfile.add(avatarPanel);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 12)));
        leftProfile.add(nameLbl);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 24)));
        leftProfile.add(trustBox);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 16)));
        leftProfile.add(suspBox);

        mainSplit.add(leftProfile, BorderLayout.WEST);

        // Right Panel: Dialogue / Chat Log and Input
        GlassPanel rightChatPanel = new GlassPanel(new BorderLayout(0, 12));
        rightChatPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Chat messages scroll pane
        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setOpaque(false);

        chatScrollPane = new JScrollPane(chatListPanel);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setBorder(null);

        rebuildChatMessages();

        rightChatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Input & Actions container
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);

        inputField = new JTextField();
        inputField.setFont(Theme.regular(13));
        inputField.setForeground(Theme.TEXT_PRIMARY);
        inputField.setBackground(new Color(25, 34, 50));
        inputField.setCaretColor(Theme.TEXT_GOLD);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputField.addActionListener(e -> sendMessage());

        CustomButton btnSend = new CustomButton("SEND", CustomButton.ButtonStyle.GOLD);
        btnSend.setPreferredSize(new Dimension(90, 38));
        btnSend.addActionListener(e -> sendMessage());

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);

        // Quick Action Buttons Row: SHOW EVIDENCE, CHALLENGE STATEMENT, EXIT
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionRow.setOpaque(false);

        CustomButton btnEvidence = new CustomButton("SHOW EVIDENCE", CustomButton.ButtonStyle.DARK);
        btnEvidence.addActionListener(e -> {
            addMessage(true, "Detective", "[Presents E01: Wine Glass with toxic traces]");
            addMessage(false, "Meera", "...I had no idea what was in his glass. Someone else poured it!");
        });

        CustomButton btnChallenge = new CustomButton("CHALLENGE STATEMENT", CustomButton.ButtonStyle.DARK);
        btnChallenge.addActionListener(e -> {
            addMessage(true, "Detective", "Security footage shows you at the table at 8:45 PM, not with Arjun!");
            addMessage(false, "Meera", "Wait... I was only there for a few seconds to retrieve my purse!");
        });

        CustomButton btnExit = new CustomButton("EXIT", CustomButton.ButtonStyle.DANGER);
        btnExit.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_HUB));

        actionRow.add(btnEvidence);
        actionRow.add(btnChallenge);
        actionRow.add(btnExit);

        bottomContainer.add(inputRow);
        bottomContainer.add(actionRow);

        rightChatPanel.add(bottomContainer, BorderLayout.SOUTH);

        mainSplit.add(rightChatPanel, BorderLayout.CENTER);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addMessage(true, "Detective", text);
        inputField.setText("");

        // Auto suspect response simulation
        Timer t = new Timer(600, e -> {
            String reply;
            if (text.toLowerCase().contains("poison") || text.toLowerCase().contains("glass")) {
                reply = "I had nothing to do with any poison! Check the bartender's records.";
            } else if (text.toLowerCase().contains("arjun")) {
                reply = "Arjun and I were discussing the quarterly accounts, that is all.";
            } else {
                reply = "I have already told you everything I know about tonight's events.";
            }
            addMessage(false, "Meera", reply);
        });
        t.setRepeats(false);
        t.start();
    }

    private void addMessage(boolean isDetective, String sender, String message) {
        chatMessages.add(new ChatMessage(isDetective, sender, message));
        rebuildChatMessages();
    }

    private void rebuildChatMessages() {
        chatListPanel.removeAll();

        for (ChatMessage msg : chatMessages) {
            JPanel bubbleRow = new JPanel(new FlowLayout(msg.isDetective ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 6));
            bubbleRow.setOpaque(false);

            JPanel bubble = new JPanel(new BorderLayout(8, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.applyQualityHints(g2);

                    int w = getWidth();
                    int h = getHeight();
                    Color bg = msg.isDetective ? new Color(28, 38, 56) : new Color(20, 60, 65);
                    Color border = msg.isDetective ? Theme.BORDER_CARD : Theme.BORDER_TEAL;

                    RoundRectangle2D round = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14);
                    g2.setColor(bg);
                    g2.fill(round);
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(round);

                    g2.dispose();
                }
            };
            bubble.setOpaque(false);
            bubble.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

            JLabel senderLbl = new JLabel(msg.sender);
            senderLbl.setFont(Theme.bold(11));
            senderLbl.setForeground(msg.isDetective ? Theme.TEXT_GOLD : Theme.TEAL_ACCENT);

            JLabel textLbl = new JLabel("<html><p style='width: 280px;'>" + msg.message + "</p></html>");
            textLbl.setFont(Theme.regular(13));
            textLbl.setForeground(Theme.TEXT_PRIMARY);

            bubble.add(senderLbl, BorderLayout.NORTH);
            bubble.add(textLbl, BorderLayout.CENTER);

            bubbleRow.add(bubble);
            chatListPanel.add(bubbleRow);
        }

        chatListPanel.revalidate();
        chatListPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
