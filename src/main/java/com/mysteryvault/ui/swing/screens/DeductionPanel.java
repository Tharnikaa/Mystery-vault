package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.components.CustomButton;
import com.mysteryvault.ui.swing.components.GlassPanel;
import com.mysteryvault.ui.swing.components.ScreenHeader;
import com.mysteryvault.ui.swing.mock.MockDeductionViewPort;
import com.mysteryvault.ui.swing.navigation.RefreshableScreen;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.DeductionViewPort;
import com.mysteryvault.ui.swing.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DeductionPanel extends JPanel implements RefreshableScreen {

    private final DeductionViewPort deductionPort;
    private String currentCaseId = "CASE_101";

    private JPanel suspectBox;
    private ButtonGroup suspectGroup;
    private final Map<JRadioButton, String> suspectRadioIds = new LinkedHashMap<>();

    private JComboBox<String> comboMotive;
    private JPanel checkRow;
    private final Map<JCheckBox, String> evidenceCheckIds = new LinkedHashMap<>();

    public DeductionPanel(DeductionViewPort deductionPort) {
        this.deductionPort = deductionPort != null ? deductionPort : new MockDeductionViewPort(new CaseSolverEngine());
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        initUI();
        refreshScreen(currentCaseId);
    }

    public DeductionPanel() {
        this(new MockDeductionViewPort(new CaseSolverEngine()));
    }

    @Override
    public void refreshScreen(String caseId) {
        this.currentCaseId = caseId != null ? caseId : "CASE_101";

        // 1. Populate suspects
        if (suspectBox != null) {
            suspectBox.removeAll();
            suspectRadioIds.clear();
            suspectGroup = new ButtonGroup();

            JLabel lblSuspects = new JLabel("Suspects:");
            lblSuspects.setFont(Theme.bold(13));
            lblSuspects.setForeground(Theme.TEXT_PRIMARY);
            suspectBox.add(lblSuspects);
            suspectBox.add(Box.createRigidArea(new Dimension(0, 10)));

            List<String[]> suspects = getSuspectOptions(this.currentCaseId);
            boolean first = true;
            for (String[] s : suspects) {
                JRadioButton rb = createStyledRadio(s[1], first);
                suspectGroup.add(rb);
                suspectRadioIds.put(rb, s[0]);
                suspectBox.add(rb);
                suspectBox.add(Box.createRigidArea(new Dimension(0, 6)));
                first = false;
            }
            suspectBox.revalidate();
            suspectBox.repaint();
        }

        // 2. Populate motives
        if (comboMotive != null) {
            comboMotive.removeAllItems();
            for (String m : getMotiveOptions(this.currentCaseId)) {
                comboMotive.addItem(m);
            }
        }

        // 3. Populate evidence
        if (checkRow != null) {
            checkRow.removeAll();
            evidenceCheckIds.clear();

            List<String[]> evList = getEvidenceOptions(this.currentCaseId);
            for (String[] ev : evList) {
                JCheckBox cb = createStyledCheckBox(ev[1], true);
                evidenceCheckIds.put(cb, ev[0]);
                checkRow.add(cb);
            }
            checkRow.revalidate();
            checkRow.repaint();
        }
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
        formRow.setMaximumSize(new Dimension(660, 160));

        suspectBox = new JPanel();
        suspectBox.setLayout(new BoxLayout(suspectBox, BoxLayout.Y_AXIS));
        suspectBox.setOpaque(false);

        formRow.add(suspectBox);

        JPanel motiveBox = new JPanel();
        motiveBox.setLayout(new BoxLayout(motiveBox, BoxLayout.Y_AXIS));
        motiveBox.setOpaque(false);

        JLabel lblMotive = new JLabel("Motive:");
        lblMotive.setFont(Theme.bold(13));
        lblMotive.setForeground(Theme.TEXT_PRIMARY);
        motiveBox.add(lblMotive);
        motiveBox.add(Box.createRigidArea(new Dimension(0, 10)));

        comboMotive = new JComboBox<>();
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
        evidenceSection.setMaximumSize(new Dimension(660, 90));

        JLabel lblSupport = new JLabel("Supporting Evidence:");
        lblSupport.setFont(Theme.bold(13));
        lblSupport.setForeground(Theme.TEXT_PRIMARY);

        checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        checkRow.setOpaque(false);

        evidenceSection.add(lblSupport);
        evidenceSection.add(checkRow);

        CustomButton btnSubmit = new CustomButton("SUBMIT DEDUCTION", CustomButton.ButtonStyle.GOLD);
        btnSubmit.setPreferredSize(new Dimension(260, 44));
        btnSubmit.setMaximumSize(new Dimension(260, 44));
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmit.addActionListener(e -> {
            String accused = null;
            for (Map.Entry<JRadioButton, String> entry : suspectRadioIds.entrySet()) {
                if (entry.getKey().isSelected()) {
                    accused = entry.getValue();
                    break;
                }
            }
            if (accused == null) accused = "UNKNOWN";

            String motive = (String) comboMotive.getSelectedItem();
            Set<String> evSet = new HashSet<>();
            for (Map.Entry<JCheckBox, String> entry : evidenceCheckIds.entrySet()) {
                if (entry.getKey().isSelected()) {
                    evSet.add(entry.getValue());
                }
            }

            DeductionRequest request = new DeductionRequest(currentCaseId, accused, motive, evSet, "Deduction submitted by detective.");
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

    private List<String[]> getSuspectOptions(String caseId) {
        List<String[]> list = new ArrayList<>();
        switch (caseId.toUpperCase()) {
            case "CASE_101":
                list.add(new String[]{"SUS_101_3", "Marcus Vance (Head of Security)"});
                list.add(new String[]{"SUS_101_1", "Julian Drake (Museum Curator)"});
                list.add(new String[]{"SUS_101_2", "Evelyn Sterling (Gem Collector)"});
                list.add(new String[]{"SUS_101_4", "Lila Moreau (Lighting Tech)"});
                break;
            case "CASE_102":
                list.add(new String[]{"SUS_102_1", "Dr. Harrison Vance (Senior Professor)"});
                list.add(new String[]{"SUS_102_2", "Elena Rostova (Visiting Scholar)"});
                list.add(new String[]{"SUS_102_3", "Simon Finch (Graduate Assistant)"});
                list.add(new String[]{"SUS_102_5", "Tobias Reed (Head Archivist)"});
                break;
            case "CASE_103":
                list.add(new String[]{"SUS_103_3", "Vivienne Sinclair (Business Partner)"});
                list.add(new String[]{"SUS_103_1", "Damian Croft (Estranged Son)"});
                list.add(new String[]{"SUS_103_2", "Dr. Eleanor Vance (Physician)"});
                list.add(new String[]{"SUS_103_4", "Gerald Finch (Head Butler)"});
                break;
            case "CASE_104":
                list.add(new String[]{"SUS_104_2", "Kira Patel (Lead Firmware Dev)"});
                list.add(new String[]{"SUS_104_1", "Dr. Aris Thorne (Chief Scientist)"});
                list.add(new String[]{"SUS_104_3", "Nolan Vance (Safety Officer)"});
                list.add(new String[]{"SUS_104_5", "Victor Sterling (CEO)"});
                break;
            default:
                list.add(new String[]{"MEERA", "Meera (Business Partner)"});
                list.add(new String[]{"ARJUN", "Arjun (Associate)"});
                list.add(new String[]{"ROHAN", "Rohan (Technician)"});
                break;
        }
        return list;
    }

    private List<String> getMotiveOptions(String caseId) {
        List<String> list = new ArrayList<>();
        switch (caseId.toUpperCase()) {
            case "CASE_101":
                list.add("Financial desperation to pay off loan shark syndicates using a black-market diamond broker");
                list.add("Curatorial insurance fraud");
                list.add("Collector prestige obsession");
                list.add("Accidental damage concealment");
                break;
            case "CASE_102":
                list.add("Academic fraud concealment: Marginal notes in the cipher proved Vance plagiarized his magnum opus");
                list.add("Private antiquities sale");
                list.add("Ideological destruction");
                list.add("Student rivalry dispute");
                break;
            case "CASE_103":
                list.add("Preventing exposure of £1.2M company embezzlement which Lord Croft was scheduled to audit the next morning");
                list.add("Inheritance testament dispute");
                list.add("Medical malpractice coverup");
                list.add("Servant grievance vendetta");
                break;
            case "CASE_104":
                list.add("Corporate espionage: Selling proprietary core telemetry and physical prototype to rival syndicate TitanCorp");
                list.add("Intellectual property dispute");
                list.add("Venture capital manipulation");
                list.add("Safety regulation sabotage");
                break;
            default:
                list.add("Financial dispute");
                list.add("Corporate blackmail");
                list.add("Personal vendetta");
                break;
        }
        return list;
    }

    private List<String[]> getEvidenceOptions(String caseId) {
        List<String[]> list = new ArrayList<>();
        switch (caseId.toUpperCase()) {
            case "CASE_101":
                list.add(new String[]{"EVD_101_SEC_LOG", "Vault RFID Access Log"});
                list.add(new String[]{"EVD_101_GLASS_CUTTER", "Engraved Glass Cutter"});
                list.add(new String[]{"EVD_101_CIRCUIT_BOX", "Tampered Breaker Switch"});
                break;
            case "CASE_102":
                list.add(new String[]{"EVD_102_SPECTACLE_FIBER", "Green Tweed Fabric Fiber"});
                list.add(new String[]{"EVD_102_BURNED_PAGE", "Scorched Vellum Fragment"});
                list.add(new String[]{"EVD_102_LECTURE_LOG", "Hall C Badge Terminal"});
                break;
            case "CASE_103":
                list.add(new String[]{"EVD_103_COCKTAIL_GLASS", "Lord Croft's Crystal Glass"});
                list.add(new String[]{"EVD_103_POWDER_COMPACT", "Silver Cosmetic Compact"});
                list.add(new String[]{"EVD_103_PORT_DECANTER", "Crystal Wine Decanter"});
                break;
            case "CASE_104":
                list.add(new String[]{"EVD_104_FARADAY_BAG", "Anti-Static RF Shielding Pouch"});
                list.add(new String[]{"EVD_104_FLASH_DRIVE", "Encrypted High-Speed USB"});
                list.add(new String[]{"EVD_104_AIRLOCK_LOG", "Cleanroom Badge Terminal"});
                break;
            default:
                list.add(new String[]{"E01", "E01 – Wine Glass"});
                list.add(new String[]{"E03", "E03 – CCTV Footage"});
                list.add(new String[]{"E05", "E05 – Lab Report"});
                break;
        }
        return list;
    }

    private JRadioButton createStyledRadio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setOpaque(false);
        rb.setFont(Theme.regular(13));
        rb.setForeground(Theme.TEXT_PRIMARY);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JCheckBox createStyledCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setFont(Theme.regular(13));
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFocusPainted(false);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }
}
