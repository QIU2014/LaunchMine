// DownloadDialog.java
package io.github.qiu2014.launchmine.ui;

import javax.swing.*;
import java.awt.*;

public class DownloadDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel fileLabel;
    private JLabel progressLabel;
    private JTextArea logArea;

    public DownloadDialog(JFrame parent) {
        super(parent, "Download Progress", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // 顶部面板 - 当前下载文件
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel titleLabel = new JLabel("Downloading:");
        fileLabel = new JLabel("Preparing to download...");
        fileLabel.setFont(fileLabel.getFont().deriveFont(Font.BOLD));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(fileLabel, BorderLayout.CENTER);

        // 进度条面板
        JPanel progressPanel = new JPanel(new BorderLayout(10, 5));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        progressLabel = new JLabel("0%");
        progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(progressLabel, BorderLayout.EAST);

        // 日志区域
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Download Log"));

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(600, 200));

        logPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加到对话框
        add(topPanel, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }

    public void setCurrentFile(String fileName) {
        fileLabel.setText(fileName);
        logArea.append("Start Downloading: " + fileName + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setProgress(int progress, long downloaded, long total) {
        progressBar.setValue(progress);
        progressLabel.setText(String.format("%d%% (%s/%s)",
                progress,
                formatSize(downloaded),
                formatSize(total)));

        if (progress == 100) {
            logArea.append("Download Complete\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    public void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public void reset() {
        progressBar.setValue(0);
        progressLabel.setText("0%");
        fileLabel.setText("Preparing to Download File...");
        logArea.setText("");
    }
}