package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockInterrogationViewPort;
import com.mysteryvault.ui.swing.mock.MockSuspectViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.InterrogationViewPort;
import com.mysteryvault.ui.swing.ports.SuspectViewPort;
import com.mysteryvault.ui.swing.theme.AssetManager;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.ChatMessageViewModel;
import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class InterrogationPanel extends JPanel {

    private final InterrogationViewPort interrogationPort;
    private final SuspectViewPort suspectPort;
    private final List<ChatMessageViewModel> chatMessages = new ArrayList<>();

    private JPanel chatListPanel;
    private JTextField inputField;
    private JScrollPane chatScrollPane;
    private SuspectViewModel currentSuspect;

    public InterrogationPanel(InterrogationViewPort interrogationPort, SuspectViewPort suspectPort) {
        this.interrogationPort = interrogationPort != null ? interrogationPort : new MockInterrogationViewPort();
        this.suspectPort = suspectPort != null ? suspectPort : new MockSuspectViewPort();
        this.currentSuspect = this.suspectPort.getSuspectById("MEERA");

        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        this.chatMessages.addAll(this.interrogationPort.getDialogueHistory(currentSuspect.getId()));
        initUI();
    }

    public InterrogationPanel() {
        this(new MockInterrogationViewPort(), new MockSuspectViewPort());
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("7. INTERROGATION");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        GlassPanel leftProfile = new GlassPanel();
        leftProfile.setLayout(new BoxLayout(leftProfile, BoxLayout.Y_AXIS));
        leftProfile.setPreferredSize(new Dimension(260, 520));
        leftProfile.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

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

                BufferedImage img = AssetManager.getImage(currentSuspect.getImageKey());
                BufferedImage rounded = AssetManager.getRoundedImage(img, size, size, 12, Theme.BORDER_GOLD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(currentSuspect.getName());
        nameLbl.setFont(Theme.titleFont(18));
        nameLbl.setForeground(Theme.TEXT_GOLD);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel trustBox = new JPanel(new BorderLayout(0, 4));
        trustBox.setOpaque(false);
        trustBox.setMaximumSize(new Dimension(200, 36));
        trustBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel trustLbl = new JLabel("Trust: " + currentSuspect.getTrust() + "%");
        trustLbl.setFont(Theme.bold(11));
        trustLbl.setForeground(Theme.TEAL_ACCENT);

        StatBar trustBar = new StatBar(currentSuspect.getTrust(), Theme.TEAL_ACCENT);
        trustBar.setPreferredSize(new Dimension(180, 7));

        trustBox.add(trustLbl, BorderLayout.NORTH);
        trustBox.add(trustBar, BorderLayout.SOUTH);

        JPanel suspBox = new JPanel(new BorderLayout(0, 4));
        suspBox.setOpaque(false);
        suspBox.setMaximumSize(new Dimension(200, 36));
        suspBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suspLbl = new JLabel("Suspicion: " + currentSuspect.getSuspicion() + "%");
        suspLbl.setFont(Theme.bold(11));
        suspLbl.setForeground(Theme.RED_CONFLICT);

        StatBar suspBar = new StatBar(currentSuspect.getSuspicion(), Theme.RED_CONFLICT);
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

        GlassPanel rightChatPanel = new GlassPanel(new BorderLayout(0, 12));
        rightChatPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));
        chatListPanel.setOpaque(false);

        chatScrollPane = new JScrollPane(chatListPanel);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setBorder(null);

        rebuildChatMessages();
        rightChatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

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

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionRow.setOpaque(false);

        CustomButton btnEvidence = new CustomButton("SHOW EVIDENCE", CustomButton.ButtonStyle.DARK);
        btnEvidence.addActionListener(e -> {
            addMessage(true, "Detective", "[Presents E01: Wine Glass with toxic traces]");
            ChatMessageViewModel reply = interrogationPort.showEvidence(currentSuspect.getName(), "E01");
            addMessage(false, reply.getSender(), reply.getMessage());
        });

        CustomButton btnChallenge = new CustomButton("CHALLENGE STATEMENT", CustomButton.ButtonStyle.DARK);
        btnChallenge.addActionListener(e -> {
            addMessage(true, "Detective", "Security footage shows you at the table at 8:45 PM, not with Arjun!");
            ChatMessageViewModel reply = interrogationPort.challengeStatement(currentSuspect.getName(), "C1");
            addMessage(false, reply.getSender(), reply.getMessage());
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

        Timer t = new Timer(500, e -> {
            ChatMessageViewModel reply = interrogationPort.askQuestion(currentSuspect.getName(), text);
            addMessage(false, reply.getSender(), reply.getMessage());
        });
        t.setRepeats(false);
        t.start();
    }

    private void addMessage(boolean isDetective, String sender, String message) {
        chatMessages.add(new ChatMessageViewModel(isDetective, sender, message));
        rebuildChatMessages();
    }

    private void rebuildChatMessages() {
        chatListPanel.removeAll();

        for (ChatMessageViewModel msg : chatMessages) {
            JPanel bubbleRow = new JPanel(new FlowLayout(msg.isDetective() ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 6));
            bubbleRow.setOpaque(false);

            JPanel bubble = new JPanel(new BorderLayout(8, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.applyQualityHints(g2);

                    int w = getWidth();
                    int h = getHeight();
                    Color bg = msg.isDetective() ? new Color(28, 38, 56) : new Color(20, 60, 65);
                    Color border = msg.isDetective() ? Theme.BORDER_CARD : Theme.BORDER_TEAL;

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

            JLabel senderLbl = new JLabel(msg.getSender());
            senderLbl.setFont(Theme.bold(11));
            senderLbl.setForeground(msg.isDetective() ? Theme.TEXT_GOLD : Theme.TEAL_ACCENT);

            JLabel textLbl = new JLabel("<html><p style='width: 280px;'>" + msg.getMessage() + "</p></html>");
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
