package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.ui.swing.components.*;
import com.mysteryvault.ui.swing.mock.MockTimelineViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.ports.TimelineViewPort;
import com.mysteryvault.ui.swing.theme.Theme;
import com.mysteryvault.ui.swing.viewmodel.TimelineEventViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class TimelinePanel extends JPanel implements com.mysteryvault.ui.swing.navigation.RefreshableScreen {

    private final TimelineViewPort timelinePort;
    private final List<TimelineEventViewModel> events = new ArrayList<>();
    private JPanel timelineCanvas;
    private String currentCaseId = "CASE_101";

    public TimelinePanel(TimelineViewPort timelinePort) {
        this.timelinePort = timelinePort != null ? timelinePort : new MockTimelineViewPort();
        setOpaque(true);
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        this.events.addAll(this.timelinePort.getTimelineForCase(currentCaseId));
        initUI();
    }

    public TimelinePanel() {
        this(new MockTimelineViewPort());
    }

    @Override
    public void refreshScreen(String caseId) {
        this.currentCaseId = caseId != null ? caseId : "CASE_101";
        events.clear();
        events.addAll(timelinePort.getTimelineForCase(this.currentCaseId));
        if (timelineCanvas != null) {
            timelineCanvas.repaint();
        }
    }

    private void initUI() {
        ScreenHeader header = new ScreenHeader("8. INVESTIGATION TIMELINE");
        add(header, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel timelineCard = new GlassPanel(new BorderLayout(0, 16));
        timelineCard.setPreferredSize(new Dimension(820, 520));
        timelineCard.setBorder(BorderFactory.createEmptyBorder(20, 36, 20, 36));

        timelineCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyQualityHints(g2);

                int startX = 140;
                int startY = 32;
                int rowGap = 56;

                int totalH = (events.size() - 1) * rowGap;
                g2.setColor(Theme.BORDER_CARD);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawLine(startX, startY, startX, startY + totalH);

                for (int i = 0; i < events.size(); i++) {
                    TimelineEventViewModel ev = events.get(i);
                    int cy = startY + i * rowGap;

                    g2.setFont(Theme.bold(13));
                    g2.setColor(ev.isHasConflict() ? Theme.RED_CONFLICT : Theme.TEXT_GOLD);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(ev.getTime(), startX - fm.stringWidth(ev.getTime()) - 18, cy + 5);

                    int dotRadius = ev.isHasConflict() ? 7 : 5;
                    Color dotColor = ev.isHasConflict() ? Theme.RED_CONFLICT : Theme.TEAL_ACCENT;

                    g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), 60));
                    g2.fill(new Ellipse2D.Float(startX - dotRadius - 3, cy - dotRadius - 3, (dotRadius + 3) * 2, (dotRadius + 3) * 2));

                    g2.setColor(dotColor);
                    g2.fill(new Ellipse2D.Float(startX - dotRadius, cy - dotRadius, dotRadius * 2, dotRadius * 2));

                    g2.setFont(Theme.regular(13));
                    g2.setColor(Theme.TEXT_PRIMARY);
                    g2.drawString(ev.getDescription(), startX + 24, cy + 5);

                    if (ev.isHasConflict()) {
                        int boxX = startX + 24;
                        int boxY = cy + 14;
                        int boxW = 260;
                        int boxH = 24;

                        RoundRectangle2D box = new RoundRectangle2D.Float(boxX, boxY, boxW, boxH, 6, 6);
                        g2.setColor(Theme.RED_CONFLICT_BG);
                        g2.fill(box);
                        g2.setColor(Theme.RED_CONFLICT);
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.draw(box);

                        g2.setFont(Theme.bold(10));
                        g2.setColor(Theme.RED_CONFLICT);
                        g2.drawString("⚠️  TIMELINE CONFLICT DETECTED", boxX + 12, boxY + 16);
                    }
                }

                g2.dispose();
            }
        };
        timelineCanvas.setOpaque(false);
        timelineCard.add(timelineCanvas, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBtns.setOpaque(false);

        CustomButton btnAdd = new CustomButton("ADD EVENT", CustomButton.ButtonStyle.GOLD);
        btnAdd.addActionListener(e -> {
            timelinePort.addEvent(currentCaseId, "09:50 PM", "Detective arrives on the scene");
            events.clear();
            events.addAll(timelinePort.getTimelineForCase(currentCaseId));
            timelineCanvas.repaint();
        });

        CustomButton btnEdit = new CustomButton("EDIT", CustomButton.ButtonStyle.DARK);
        btnEdit.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Select a timeline event node to adjust timestamp or flag conflict.",
                "Edit Timeline", JOptionPane.INFORMATION_MESSAGE));

        leftBtns.add(btnAdd);
        leftBtns.add(btnEdit);

        CustomButton btnAnalyze = new CustomButton("ANALYZE TIMELINE", CustomButton.ButtonStyle.GOLD);
        btnAnalyze.setPreferredSize(new Dimension(180, 38));
        btnAnalyze.addActionListener(e -> ScreenManager.getInstance().showScreen(ScreenManager.CASE_CONNECTION_ENGINE));

        bottomRow.add(leftBtns, BorderLayout.WEST);
        bottomRow.add(btnAnalyze, BorderLayout.EAST);

        timelineCard.add(bottomRow, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(timelineCard, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }
}
