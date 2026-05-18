package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.controllers.DashboardController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class DashboardView extends JPanel {
    private static final int ICON_SIZE = 18;
    private static final Color PAGE_BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(226, 232, 240);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color TEXT_DARK = new Color(30, 41, 59);

    private final JLabel totalLabel;
    private final JLabel approvedLabel;
    private final JLabel pendingLabel;
    private final JLabel regionLabel;
    private final RegionChartPanel chartPanel;
    private DashboardController controller;

    public DashboardView() {
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(PAGE_BG);

        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(createTitlePanel(), BorderLayout.WEST);
        headerPanel.add(createActionPanel(), BorderLayout.EAST);

        totalLabel = createValueLabel();
        approvedLabel = createValueLabel();
        pendingLabel = createValueLabel();
        regionLabel = createValueLabel();

        JPanel statsPanel = new JPanel(new java.awt.GridLayout(1, 4, 16, 16));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Tổng số thí sinh", totalLabel, loadIcon("/icon/plus.png", ICON_SIZE),
                new Color(37, 99, 235)));
        statsPanel.add(createStatCard("Số hồ sơ đã duyệt", approvedLabel, loadIcon("/icon/eye.png", ICON_SIZE),
                new Color(16, 185, 129)));
        statsPanel.add(createStatCard("Số hồ sơ chờ duyệt", pendingLabel, loadIcon("/icon/pencil.png", ICON_SIZE),
                new Color(245, 158, 11)));
        statsPanel.add(createStatCard("Số lượng thí sinh theo khu vực", regionLabel,
                loadIcon("/icon/circle-x.png", ICON_SIZE), new Color(148, 163, 184)));

        chartPanel = new RegionChartPanel();
        JPanel chartCard = createChartCard(chartPanel);

        JPanel topPanel = new JPanel(new BorderLayout(0, 16));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(chartCard, BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);

        JLabel title = new JLabel("Tổng quan tuyển sinh");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Cập nhật nhanh dữ liệu hồ sơ trong hệ thống");
        subtitle.setForeground(TEXT_MUTED);

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createActionPanel() {
        JButton refreshButton = new JButton("Làm mới");
        ImageIcon refreshIcon = tintIcon(loadIcon("/icon/eye.png", ICON_SIZE), TEXT_MUTED);
        if (refreshIcon != null) {
            refreshButton.setIcon(refreshIcon);
        }
        refreshButton.addActionListener(event -> runWithController(DashboardController::onRefresh));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(refreshButton, BorderLayout.EAST);
        return panel;
    }

    public void setController(DashboardController controller) {
        this.controller = controller;
    }

    public void setOverview(int total, int approved, int pending, String regionSummary,
            Map<String, Integer> regionCounts) {
        totalLabel.setText(formatNumber(total));
        approvedLabel.setText(formatNumber(approved));
        pendingLabel.setText(formatNumber(pending));
        regionLabel.setText(asMultiline(regionSummary));
        chartPanel.setData(regionCounts);
    }

    public Window getWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private ImageIcon loadIcon(String path, int size) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        if (scaled instanceof java.awt.image.BufferedImage) {
            return new ImageIcon(scaled);
        }
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(scaled, 0, 0, null);
        return new ImageIcon(image);
    }

    private void runWithController(java.util.function.Consumer<DashboardController> action) {
        if (controller != null) {
            action.accept(controller);
        }
    }

    private ImageIcon tintIcon(ImageIcon icon, Color color) {
        if (icon == null || color == null) {
            return icon;
        }
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();

        int rgb = color.getRGB() & 0x00FFFFFF;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha != 0) {
                    image.setRGB(x, y, (alpha << 24) | rgb);
                }
            }
        }
        return new ImageIcon(image);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, ImageIcon icon, Color iconColor) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel iconLabel = new JLabel();
        if (icon != null) {
            iconLabel.setIcon(tintIcon(icon, iconColor));
        }
        iconLabel.setPreferredSize(new Dimension(28, 28));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(iconLabel, BorderLayout.EAST);

        JPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout(0, 6));
        panel.add(header, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JPanel createChartCard(JPanel chart) {
        JPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Thống kê thí sinh theo khu vực");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private String formatNumber(int value) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(value);
    }

    private String asMultiline(String text) {
        if (text == null || text.isBlank()) {
            return "Chưa có";
        }
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }

    private final class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final class RegionChartPanel extends JPanel {
        private static final int PADDING = 16;
        private static final int AXIS_HEIGHT = 24;
        private static final int LABEL_WIDTH = 70;
        private final List<Color> barColors = List.of(
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(245, 158, 11),
                new Color(99, 102, 241),
                new Color(236, 72, 153),
                new Color(148, 163, 184));
        private Map<String, Integer> data = new LinkedHashMap<>();

        public RegionChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(520, 260));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (data.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.drawString("Chưa có dữ liệu", PADDING, PADDING + 12);
                g2.dispose();
                return;
            }

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            if (entries.size() > 6) {
                entries = entries.subList(0, 6);
            }

            int maxValue = entries.stream().map(Map.Entry::getValue).max(Integer::compareTo).orElse(1);
            int availableWidth = getWidth() - PADDING * 2 - LABEL_WIDTH;
            int availableHeight = getHeight() - PADDING * 2 - AXIS_HEIGHT;
            int barHeight = Math.max(16, availableHeight / entries.size() - 8);
            int y = PADDING;

            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<String, Integer> entry = entries.get(i);
                int barWidth = (int) ((availableWidth - 16) * (entry.getValue() / (double) maxValue));
                g2.setColor(TEXT_MUTED);
                g2.drawString(entry.getKey(), PADDING, y + barHeight - 4);

                g2.setColor(barColors.get(i % barColors.size()));
                g2.fillRoundRect(PADDING + LABEL_WIDTH, y, barWidth, barHeight, 10, 10);

                g2.setColor(TEXT_DARK);
                g2.drawString(formatNumber(entry.getValue()), PADDING + LABEL_WIDTH + barWidth + 8,
                        y + barHeight - 4);
                y += barHeight + 8;
            }

            g2.setColor(CARD_BORDER);
            g2.drawLine(PADDING, getHeight() - PADDING, getWidth() - PADDING, getHeight() - PADDING);
            g2.dispose();
        }
    }
}
