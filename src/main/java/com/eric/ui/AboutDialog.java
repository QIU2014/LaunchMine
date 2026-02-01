package com.eric.ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class AboutDialog extends JDialog {
    public AboutDialog(JFrame parentFrame) {
        super(parentFrame, "About Program", true);

        // Don't create Main instance - use static version or get from parent
        String version = getVersionFromParent(parentFrame);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(new Dimension(300, 200));
        setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("LaunchMine", SwingConstants.CENTER));
        panel.add(new JLabel(String.format("Version: %s", version), SwingConstants.CENTER));
        panel.add(new JLabel("Author: qiuerichanru", SwingConstants.CENTER));
        panel.add(new JLabel("© 2026 qiuerichanru", SwingConstants.CENTER));

        JButton licenseBtn = new JButton("License");
        licenseBtn.addActionListener(e -> showLicenseDialog());

        getContentPane().add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(licenseBtn);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private String getVersionFromParent(JFrame parentFrame) {
        // Try to get version from parent if it's a Main instance
        if (parentFrame instanceof com.eric.Main) {
            return ((com.eric.Main) parentFrame).getVersion();
        }
        // Fallback to default version
        return "1.0.0";
    }

    private void showLicenseDialog() {
        JDialog licenseDialog = new JDialog(this, "MIT License", true);
        licenseDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        licenseDialog.setSize(new Dimension(500, 400));
        licenseDialog.setLocationRelativeTo(this);

        JTextArea licenseText = new JTextArea();
        licenseText.setEditable(false);
        licenseText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        licenseText.setMargin(new Insets(10, 10, 10, 10));

        // Read license file
        try (InputStream is = AboutDialog.class.getClassLoader().getResourceAsStream("LICENSE.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            licenseText.setText(content);

        } catch (IOException ex) {
            ex.printStackTrace();
            licenseText.setText("License file not found.\n\n" +
                    "This program is licensed under the MIT License.\n" +
                    "Copyright © 2026 qiuerichanru");
        } catch (NullPointerException ex) {
            licenseText.setText("MIT License\n\n" +
                    "Copyright © 2026 qiuerichanru\n\n" +
                    "Permission is hereby granted...");
        }

        JScrollPane scrollPane = new JScrollPane(licenseText);
        licenseDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> licenseDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);
        licenseDialog.add(buttonPanel, BorderLayout.SOUTH);

        licenseDialog.setVisible(true);
    }
}