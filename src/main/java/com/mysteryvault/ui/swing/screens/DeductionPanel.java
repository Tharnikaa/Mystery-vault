package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockDeductionViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.DeductionViewPort;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DeductionPanel extends JPanel {

    private final DeductionViewPort deductionPort;
    private final com.mysteryvault.ui.swing.ports.CaseViewPort casePort;

    private JRadioButton rbArjun;
    private JRadioButton rbMeera;
    private JRadioButton rbRohan;
    private JComboBox<String> comboMotive;
    private JCheckBox cbE01;
    private JCheckBox cbE03;
    private JCheckBox cbE05;

    public DeductionPanel(DeductionViewPort deductionPort, com.mysteryvault.ui.swing.ports.CaseViewPort casePort) {
        this.deductionPort = deductionPort != null ? deductionPort : new MockDeductionViewPort();
        this.casePort = casePort;
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
    }

    public DeductionPanel(DeductionViewPort deductionPort) {
        this(deductionPort, null);
    }

    public DeductionPanel() {
        this(new MockDeductionViewPort());
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("11. FINAL DEDUCTION");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel card = new GlassPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(740, 520));
        card.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JLabel titleLbl = new JLabel("FINAL DEDUCTION");
        titleLbl.setFont(Theme.titleFont(22));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("WHO IS RESPONSIBLE?");
        subLbl.setFont(Theme.bold(14));
        subLbl.setForeground(Theme.TEXT_GOLD);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formRow = new JPanel(new GridLayout(1, 2, 32, 0));
        formRow.setOpaque(false);
        formRow.setMaximumSize(new Dimension(660, 140));

        JPanel suspectBox = new JPanel();
        suspectBox.setLayout(new BoxLayout(suspectBox, BoxLayout.Y_AXIS));
        suspectBox.setOpaque(false);

        JLabel lblSuspects = new JLabel("Suspects:");
        lblSuspects.setFont(Theme.bold(13));
        lblSuspects.setForeground(Theme.TEXT_PRIMARY);
        suspectBox.add(lblSuspects);
        suspectBox.add(Box.createRigidArea(new Dimension(0, 10)));

        ButtonGroup group = new ButtonGroup();
        rbArjun = createStyledRadio("Arjun", false);
        rbMeera = createStyledRadio("Meera  (Trust: 45% • Suspicion: 72%)", true);
        rbRohan = createStyledRadio("Rohan", false);

        group.add(rbArjun);
        group.add(rbMeera);
        group.add(rbRohan);

        suspectBox.add(rbArjun);
        suspectBox.add(Box.createRigidArea(new Dimension(0, 6)));
        suspectBox.add(rbMeera);
        suspectBox.add(Box.createRigidArea(new Dimension(0, 6)));
        suspectBox.add(rbRohan);

        formRow.add(suspectBox);

        JPanel motiveBox = new JPanel();
        motiveBox.setLayout(new BoxLayout(motiveBox, BoxLayout.Y_AXIS));
        motiveBox.setOpaque(false);

        JLabel lblMotive = new JLabel("Motive:");
        lblMotive.setFont(Theme.bold(13));
        lblMotive.setForeground(Theme.TEXT_PRIMARY);
        motiveBox.add(lblMotive);
        motiveBox.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] motives = {
                "Financial dispute",
                "Corporate blackmail",
                "Personal vendetta",
                "Accidental poisoning"
        };
        comboMotive = new JComboBox<>(motives);
        comboMotive.setFont(Theme.regular(13));
        comboMotive.setForeground(Theme.TEXT_PRIMARY);
        comboMotive.setBackground(new Color(25, 34, 50));
        comboMotive.setMaximumSize(new Dimension(280, 36));
        comboMotive.setPreferredSize(new Dimension(280, 36));
        motiveBox.add(comboMotive);

        formRow.add(motiveBox);

        JPanel evidenceSection = new JPanel();
        evidenceSection.setLayout(new BoxLayout(evidenceSection, BoxLayout.Y_AXIS));
        evidenceSection.setOpaque(false);
        evidenceSection.setMaximumSize(new Dimension(660, 80));

        JLabel lblSupport = new JLabel("Supporting Evidence:");
        lblSupport.setFont(Theme.bold(13));
        lblSupport.setForeground(Theme.TEXT_PRIMARY);

        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        checkRow.setOpaque(false);

        cbE01 = createStyledCheckBox("E01 – Wine Glass", true);
        cbE03 = createStyledCheckBox("E03 – CCTV Footage", true);
        cbE05 = createStyledCheckBox("E05 – Lab Report", true);

        checkRow.add(cbE01);
        checkRow.add(cbE03);
        checkRow.add(cbE05);

        evidenceSection.add(lblSupport);
        evidenceSection.add(checkRow);

        CustomButton btnSubmit = new CustomButton("SUBMIT DEDUCTION", CustomButton.ButtonStyle.GOLD);
        btnSubmit.setPreferredSize(new Dimension(260, 44));
        btnSubmit.setMaximumSize(new Dimension(260, 44));
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmit.addActionListener(e -> {
            String accused = rbMeera.isSelected() ? "MEERA" : (rbArjun.isSelected() ? "ARJUN" : "ROHAN");
            String motive = (String) comboMotive.getSelectedItem();
            Set<String> evSet = new HashSet<>();
            if (cbE01.isSelected()) evSet.add("E01");
            if (cbE03.isSelected()) evSet.add("E03");
            if (cbE05.isSelected()) evSet.add("E05");

            String activeCaseId = (casePort != null && casePort.getActiveCase() != null)
                    ? casePort.getActiveCase().getId()
                    : "CASE_103";
            DeductionRequest request = new DeductionRequest(activeCaseId, accused, motive, evSet, "Deduction submitted by detective.");
            deductionPort.submitDeduction(request);

            ScreenManager.getInstance().showScreen(ScreenManager.CASE_RESULT);
        });

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(subLbl);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(formRow);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(evidenceSection);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(btnSubmit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(card, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JRadioButton createStyledRadio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setOpaque(false);
        rb.setFont(Theme.regular(13));
        rb.setForeground(Theme.TEXT_PRIMARY);
        rb.setFocusPainted(false);
        return rb;
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setFont(Theme.regular(13));
        cb.setForeground(Theme.TEXT_GOLD);
        cb.setFocusPainted(false);
        return cb;
    }
}
