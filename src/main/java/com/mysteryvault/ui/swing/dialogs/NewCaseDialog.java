package com.mysteryvault.ui.swing.dialogs;

import com.mysteryvault.ui.swing.components.CustomButton;
import com.mysteryvault.ui.swing.components.GlassPanel;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Module 13 Dialog for drafting a new case investigation.
 * Owned by Member 4.
 */
public class NewCaseDialog extends JDialog {

    private final JTextField txtTitle;
    private final JComboBox<String> cmbDifficulty;
    private final JTextArea txtSynopsis;
    private final JTextField txtDuration;
    private Consumer<CaseCardViewModel> onCaseCreated;

    public NewCaseDialog(Frame owner) {
        super(owner, "Draft New Mystery Case", true);
        setUndecorated(true);
        setSize(480, 440);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        GlassPanel content = new GlassPanel(new BorderLayout(0, 16));
        content.setCornerRadius(16);
        content.setBackgroundColor(new Color(17, 22, 34, 250));
        content.setBorderColor(Theme.BORDER_GOLD);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header Title
        JLabel lblHeader = new JLabel("DRAFT NEW INVESTIGATION", SwingConstants.CENTER);
        lblHeader.setFont(Theme.titleFont(16));
        lblHeader.setForeground(Theme.TEXT_GOLD);
        content.add(lblHeader, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 10));
        form.setOpaque(false);

        txtTitle = createStyledTextField("The Vanishing Heiress");
        form.add(createFieldRow("Case Title:", txtTitle));

        String[] diffs = {"Casual (30 Mins)", "Detective (45 Mins)", "Master (60 Mins)"};
        cmbDifficulty = new JComboBox<>(diffs);
        cmbDifficulty.setFont(Theme.regular(12));
        cmbDifficulty.setBackground(new Color(25, 34, 50));
        cmbDifficulty.setForeground(Theme.TEXT_PRIMARY);
        form.add(createFieldRow("Difficulty:", cmbDifficulty));

        txtDuration = createStyledTextField("45 Mins");
        form.add(createFieldRow("Est. Duration:", txtDuration));

        txtSynopsis = new JTextArea("A high-stakes disappearance at the Grand Victorian Hotel...");
        txtSynopsis.setFont(Theme.regular(11));
        txtSynopsis.setForeground(Theme.TEXT_SECONDARY);
        txtSynopsis.setBackground(new Color(25, 34, 50));
        txtSynopsis.setLineWrap(true);
        txtSynopsis.setWrapStyleWord(true);
        txtSynopsis.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        form.add(createFieldRow("Synopsis:", new JScrollPane(txtSynopsis)));

        content.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);

        CustomButton btnCancel = new CustomButton("CANCEL", CustomButton.ButtonStyle.DARK);
        btnCancel.addActionListener(e -> dispose());

        CustomButton btnCreate = new CustomButton("CREATE CASE", CustomButton.ButtonStyle.GOLD);
        btnCreate.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            if (title.isEmpty()) title = "Untitled Case";
            CaseCardViewModel newCase = new CaseCardViewModel(
                    "CASE_" + System.currentTimeMillis(),
                    title,
                    txtDuration.getText().trim(),
                    "NEW",
                    "IN_PROGRESS",
                    "case_prototype.png",
                    false
            );
            if (onCaseCreated != null) {
                onCaseCreated.accept(newCase);
            }
            dispose();
        });

        btnRow.add(btnCancel);
        btnRow.add(btnCreate);
        content.add(btnRow, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    public void setOnCaseCreated(Consumer<CaseCardViewModel> onCaseCreated) {
        this.onCaseCreated = onCaseCreated;
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(100, 26));
        l.setFont(Theme.bold(12));
        l.setForeground(Theme.TEXT_SECONDARY);
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(Theme.regular(12));
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setBackground(new Color(25, 34, 50));
        tf.setCaretColor(Theme.TEXT_GOLD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }
}
