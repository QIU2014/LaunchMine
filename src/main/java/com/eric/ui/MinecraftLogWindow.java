// MinecraftLogWindow.java
package com.eric.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MinecraftLogWindow extends JFrame {
    private JTextArea logArea;
    private JButton clearButton;
    private JButton copyButton;
    private JScrollPane scrollPane;

    public MinecraftLogWindow() {
        initComponents();
        setTitle("Minecraft 日志");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hideWindow();
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        logArea.setCaretColor(Color.WHITE);

        scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        clearButton = new JButton("清空日志");
        clearButton.addActionListener(e -> clearLog());

        copyButton = new JButton("复制日志");
        copyButton.addActionListener(e -> copyLogToClipboard());

        buttonPanel.add(clearButton);
        buttonPanel.add(copyButton);

        // 添加到窗口
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // 自动滚动到底部
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
        });
    }

    private void copyLogToClipboard() {
        String logText = logArea.getText();
        if (!logText.isEmpty()) {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(logText);
            java.awt.datatransfer.Clipboard clipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            JOptionPane.showMessageDialog(this, "日志已复制到剪贴板",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
        });
    }

    public void hideWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
        });
    }

    public boolean isWindowVisible() {
        return isVisible();
    }
}