package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockInterrogationViewPort;
import com.mysteryvault.ui.swing.mock.MockSuspectViewPort;
import com.mysteryvault.ui.swing.navigation.RefreshableScreen;
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
import java.util.Collections;
import java.util.List;

public class InterrogationPanel extends JPanel implements RefreshableScreen {

    private final InterrogationViewPort interrogationPort;
    private final SuspectViewPort suspectPort;
    private final List<ChatMessageViewModel> chatMessages = new ArrayList<>();

    private JPanel chatListPanel;
    private JTextField inputField;
    private JScrollPane chatScrollPane;
    private JComboBox<String> questionCombo;
    private JLabel nameLbl;
    private JLabel roleLbl;
    private JLabel trustLbl;
    private StatBar trustBar;
    private JLabel suspLbl;
    private StatBar suspBar;
    private JPanel avatarPanel;
    private SuspectViewModel currentSuspect;

    public InterrogationPanel(InterrogationViewPort interrogationPort, SuspectViewPort suspectPort) {
        this.interrogationPort = interrogationPort != null ? interrogationPort : new MockInterrogationViewPort();
        this.suspectPort = suspectPort != null ? suspectPort : new MockSuspectViewPort();

        List<SuspectViewModel> suspects = this.suspectPort.getSuspectsForCase("CASE_101");
        if (!suspects.isEmpty()) {
            this.currentSuspect = suspects.get(0);
        } else {
            this.currentSuspect = this.suspectPort.getSuspectById("SUS_101_3");
            if (this.currentSuspect == null) {
                this.currentSuspect = new SuspectViewModel("SUS_101_3", "Marcus Vance", "Head of Security", "Ex-Military Officer", 38,
                        "Prime Suspect", "Monitoring CCTV booth", "Scene", "None", 75, 25, "suspect_rohan.png", true);
            }
        }

        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        this.chatMessages.addAll(this.interrogationPort.getDialogueHistory(currentSuspect.getId()));
        initUI();
    }

    public InterrogationPanel() {
        this(new MockInterrogationViewPort(), new MockSuspectViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        List<SuspectViewModel> suspects = suspectPort.getSuspectsForCase(caseId);
        if (currentSuspect == null && !suspects.isEmpty()) {
            selectSuspect(suspects.get(0).getId());
        } else if (currentSuspect != null) {
            boolean found = false;
            for (SuspectViewModel s : suspects) {
                if (s.getId().equalsIgnoreCase(currentSuspect.getId())) {
                    found = true;
                    selectSuspect(s.getId());
                    break;
                }
            }
            if (!found && !suspects.isEmpty()) {
                selectSuspect(suspects.get(0).getId());
            }
        }
    }

    public void selectSuspect(String suspectId) {
        if (suspectId == null) return;
        SuspectViewModel s = suspectPort.getSuspectById(suspectId);
        if (s == null) {
            List<SuspectViewModel> list = suspectPort.getSuspectsForCase(null);
            for (SuspectViewModel vm : list) {
                if (vm.getId().equalsIgnoreCase(suspectId) || vm.getName().equalsIgnoreCase(suspectId)) {
                    s = vm;
                    break;
                }
            }
        }
        if (s != null) {
            this.currentSuspect = s;
            updateSuspectDisplay();
        }
    }

    public SuspectViewModel getCurrentSuspect() {
        return currentSuspect;
    }

    public List<ChatMessageViewModel> getChatMessages() {
        return Collections.unmodifiableList(chatMessages);
    }

    private void updateSuspectDisplay() {
        if (currentSuspect == null) return;

        if (nameLbl != null) nameLbl.setText(currentSuspect.getName());
        if (roleLbl != null) roleLbl.setText(currentSuspect.getRole() + " • " + currentSuspect.getOccupation());
        if (trustLbl != null) trustLbl.setText("Trust: " + currentSuspect.getTrust() + "%");
        if (trustBar != null) trustBar.setProgress(currentSuspect.getTrust());
        if (suspLbl != null) suspLbl.setText("Suspicion: " + currentSuspect.getSuspicion() + "%");
        if (suspBar != null) suspBar.setProgress(currentSuspect.getSuspicion());

        if (avatarPanel != null) {
            avatarPanel.repaint();
        }

        // Reload available questions
        if (questionCombo != null) {
            questionCombo.removeAllItems();
            List<String> questions = interrogationPort.getAvailableQuestions(currentSuspect.getId());
            if (questions.isEmpty()) {
                questionCombo.addItem("Where were you during the incident?");
                questionCombo.addItem("Can you corroborate your alibi?");
            } else {
                for (String q : questions) {
                    questionCombo.addItem(q);
                }
            }
        }

        // Reload history
        chatMessages.clear();
        chatMessages.addAll(interrogationPort.getDialogueHistory(currentSuspect.getId()));
        rebuildChatMessages();
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("7. INTERROGATION");
        add(header, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(24, 0));
        mainSplit.setOpaque(false);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // LEFT PROFILE CARD
        GlassPanel leftProfile = new GlassPanel();
        leftProfile.setLayout(new BoxLayout(leftProfile, BoxLayout.Y_AXIS));
        leftProfile.setPreferredSize(new Dimension(280, 520));
        leftProfile.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));
        leftProfile.setBorderColor(Theme.BORDER_GOLD);

        avatarPanel = new JPanel() {
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

                String imgKey = currentSuspect != null ? currentSuspect.getImageKey() : "suspect_arjun.jpg";
                BufferedImage img = AssetManager.getImage(imgKey);
                BufferedImage rounded = AssetManager.getRoundedImage(img, size, size, 12, Theme.BORDER_GOLD);
                g2.drawImage(rounded, x, y, null);

                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLbl = new JLabel(currentSuspect != null ? currentSuspect.getName() : "Suspect");
        nameLbl.setFont(Theme.titleFont(18));
        nameLbl.setForeground(Theme.TEXT_GOLD);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        roleLbl = new JLabel(currentSuspect != null ? (currentSuspect.getRole() + " • " + currentSuspect.getOccupation()) : "");
        roleLbl.setFont(Theme.regular(11));
        roleLbl.setForeground(Theme.TEXT_MUTED);
        roleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel trustBox = new JPanel(new BorderLayout(0, 4));
        trustBox.setOpaque(false);
        trustBox.setMaximumSize(new Dimension(220, 36));
        trustBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        int trustVal = currentSuspect != null ? currentSuspect.getTrust() : 50;
        trustLbl = new JLabel("Trust: " + trustVal + "%");
        trustLbl.setFont(Theme.bold(11));
        trustLbl.setForeground(Theme.TEAL_ACCENT);

        trustBar = new StatBar(trustVal, Theme.TEAL_ACCENT);
        trustBar.setPreferredSize(new Dimension(200, 7));

        trustBox.add(trustLbl, BorderLayout.NORTH);
        trustBox.add(trustBar, BorderLayout.SOUTH);

        JPanel suspBox = new JPanel(new BorderLayout(0, 4));
        suspBox.setOpaque(false);
        suspBox.setMaximumSize(new Dimension(220, 36));
        suspBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        int suspVal = currentSuspect != null ? currentSuspect.getSuspicion() : 50;
        suspLbl = new JLabel("Suspicion: " + suspVal + "%");
        suspLbl.setFont(Theme.bold(11));
        suspLbl.setForeground(Theme.RED_CONFLICT);

        suspBar = new StatBar(suspVal, Theme.RED_CONFLICT);
        suspBar.setPreferredSize(new Dimension(200, 7));

        suspBox.add(suspLbl, BorderLayout.NORTH);
        suspBox.add(suspBar, BorderLayout.SOUTH);

        leftProfile.add(avatarPanel);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 10)));
        leftProfile.add(nameLbl);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 4)));
        leftProfile.add(roleLbl);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 20)));
        leftProfile.add(trustBox);
        leftProfile.add(Box.createRigidArea(new Dimension(0, 14)));
        leftProfile.add(suspBox);

        mainSplit.add(leftProfile, BorderLayout.WEST);

        // RIGHT CHAT PANEL
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

        // BOTTOM CONTROLS
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

        // 1. Available Question Selector Row
        JPanel questionSelectRow = new JPanel(new BorderLayout(10, 0));
        questionSelectRow.setOpaque(false);
        questionSelectRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        questionCombo = new JComboBox<>();
        questionCombo.setFont(Theme.regular(12));
        questionCombo.setBackground(new Color(25, 34, 50));
        questionCombo.setForeground(Theme.TEXT_PRIMARY);
        questionCombo.setFocusable(false);
        questionCombo.setPreferredSize(new Dimension(500, 34));

        List<String> questions = interrogationPort.getAvailableQuestions(currentSuspect != null ? currentSuspect.getId() : null);
        if (questions.isEmpty()) {
            questionCombo.addItem("Where were you during the incident?");
            questionCombo.addItem("Can you corroborate your alibi?");
        } else {
            for (String q : questions) {
                questionCombo.addItem(q);
            }
        }

        CustomButton btnAskQuestion = new CustomButton("ASK TOPIC", CustomButton.ButtonStyle.GOLD);
        btnAskQuestion.setPreferredSize(new Dimension(110, 34));
        btnAskQuestion.addActionListener(e -> askSelectedQuestion());

        questionSelectRow.add(questionCombo, BorderLayout.CENTER);
        questionSelectRow.add(btnAskQuestion, BorderLayout.EAST);

        // 2. Custom Text Inquiry Input Row
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

        CustomButton btnSend = new CustomButton("INQUIRE", CustomButton.ButtonStyle.DARK);
        btnSend.setPreferredSize(new Dimension(110, 36));
        btnSend.addActionListener(e -> sendMessage());

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);

        // 3. Action Buttons Row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionRow.setOpaque(false);

        CustomButton btnEvidence = new CustomButton("CONFRONT EVIDENCE", CustomButton.ButtonStyle.DARK);
        btnEvidence.addActionListener(e -> {
            addMessage(true, "Detective", "[Presents case evidence for interrogation analysis]");
            ChatMessageViewModel reply = interrogationPort.showEvidence(currentSuspect.getId(), "EVD_101_SEC_LOG");
            addMessage(false, currentSuspect.getName(), reply.getMessage());
        });

        CustomButton btnChallenge = new CustomButton("CHALLENGE STATEMENT", CustomButton.ButtonStyle.DARK);
        btnChallenge.addActionListener(e -> {
            addMessage(true, "Detective", "Your recorded timeline directly conflicts with physical and biometric access logs!");
            ChatMessageViewModel reply = interrogationPort.challengeStatement(currentSuspect.getId(), "CONTR_101_1");
            addMessage(false, currentSuspect.getName(), reply.getMessage());
        });

        CustomButton btnExit = new CustomButton("RETURN TO HUB", CustomButton.ButtonStyle.DANGER);
        btnExit.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.INVESTIGATION_HUB));

        actionRow.add(btnEvidence);
        actionRow.add(btnChallenge);
        actionRow.add(btnExit);

        bottomContainer.add(questionSelectRow);
        bottomContainer.add(inputRow);
        bottomContainer.add(actionRow);

        rightChatPanel.add(bottomContainer, BorderLayout.SOUTH);

        mainSplit.add(rightChatPanel, BorderLayout.CENTER);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void askSelectedQuestion() {
        if (questionCombo == null) return;
        Object item = questionCombo.getSelectedItem();
        if (item == null) return;
        String q = item.toString().trim();
        if (q.isEmpty()) return;

        addMessage(true, "Detective", q);
        ChatMessageViewModel reply = interrogationPort.askQuestion(currentSuspect.getId(), q);
        addMessage(false, currentSuspect.getName(), reply.getMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addMessage(true, "Detective", text);
        inputField.setText("");

        ChatMessageViewModel reply = interrogationPort.askQuestion(currentSuspect.getId(), text);
        addMessage(false, currentSuspect.getName(), reply.getMessage());
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

            JLabel textLbl = new JLabel("<html><p style='width: 320px;'>" + msg.getMessage() + "</p></html>");
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
