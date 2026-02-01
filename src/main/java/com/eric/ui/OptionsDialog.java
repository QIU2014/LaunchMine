package com.eric.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.eric.utils.PreferencesUtils;
import com.eric.Main;

public class OptionsDialog extends JDialog {
    private Main main;
    public OptionsDialog(Main main) {
        this.main = main;
        JDialog optionsDialog = new JDialog(main, "Preferences", true);

        // Style for macOS
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            optionsDialog.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        }

        optionsDialog.setSize(500, 400);
        optionsDialog.setLocationRelativeTo(main);

        // Create content
        JTabbedPane tabbedPane = new JTabbedPane();

        // General tab
        JPanel generalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        generalPanel.add(new JLabel("Default Memory (MB):"), gbc);

        gbc.gridx = 1;
        JSpinner memorySpinner = new JSpinner(new SpinnerNumberModel(2048, 512, 16384, 256));
        generalPanel.add(memorySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        generalPanel.add(new JLabel("Default Window Width:"), gbc);

        gbc.gridx = 1;
        JTextField widthField = new JTextField("854", 10);
        generalPanel.add(widthField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        generalPanel.add(new JLabel("Default Window Height:"), gbc);

        gbc.gridx = 1;
        JTextField heightField = new JTextField("480", 10);
        generalPanel.add(heightField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JCheckBox autoUpdateCheck = new JCheckBox("Check for updates automatically");
        autoUpdateCheck.setSelected(true);
        generalPanel.add(autoUpdateCheck, gbc);

        tabbedPane.addTab("General", new JScrollPane(generalPanel));

        // Java tab
        JPanel javaPanel = new JPanel(new BorderLayout());
        javaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel javaPathLabel = new JLabel("Java Installation Path:");
        JTextField javaPathField = new JTextField(System.getProperty("java.home"));
        JButton browseJavaBtn = new JButton("Browse...");

        JPanel javaPathPanel = new JPanel(new BorderLayout(5, 0));
        javaPathPanel.add(javaPathField, BorderLayout.CENTER);
        javaPathPanel.add(browseJavaBtn, BorderLayout.EAST);

        javaPanel.add(javaPathLabel, BorderLayout.NORTH);
        javaPanel.add(javaPathPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Java", javaPanel);

        // Add tabs to dialog
        optionsDialog.add(tabbedPane, BorderLayout.CENTER);

        // macOS-style buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JButton applyBtn = new JButton("Apply");

        // Use macOS-style button order (Cancel, Apply, Save)
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            buttonPanel.add(cancelBtn);
            buttonPanel.add(applyBtn);
            buttonPanel.add(saveBtn);
        } else {
            buttonPanel.add(saveBtn);
            buttonPanel.add(applyBtn);
            buttonPanel.add(cancelBtn);
        }

        optionsDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Add button actions
        saveBtn.addActionListener(e -> {
            main.getPreferencesHandler().savePreferences(memorySpinner, widthField, heightField, autoUpdateCheck, javaPathField);
            optionsDialog.dispose();
        });

        applyBtn.addActionListener(e -> {
            main.getPreferencesHandler().savePreferences(memorySpinner, widthField, heightField, autoUpdateCheck, javaPathField);
        });

        cancelBtn.addActionListener(e -> optionsDialog.dispose());

        browseJavaBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(optionsDialog) == JFileChooser.APPROVE_OPTION) {
                javaPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        optionsDialog.setVisible(true);
    }
}
