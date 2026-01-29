package com.eric;

import com.eric.utils.InstanceUtils;
import com.eric.utils.JsonUtils;
import com.eric.utils.LaunchUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.io.File;

public class UI {
    private final Main main;
    private JLabel instanceNameLabel;
    private JButton startInstanceBtn, killInstanceBtn, editInstanceBtn, folderInstanceBtn, copyInstanceBtn, delInstanceBtn, confirmBtn;
    private JList<String> instanceList;
    private Map<String, Object> selectedVersion;

    // Color scheme for modern look
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);     // Blue
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);   // Light Blue
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);      // Green
    private static final Color WARNING_COLOR = new Color(231, 76, 60);      // Red
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light Gray
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(52, 73, 94);          // Dark Gray/Blue
    private static final Color DISABLED_COLOR = new Color(189, 195, 199);   // Gray

    public UI(Main main) {
        this.main = main;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(new MatteBorder(0, 0, 2, 0, SECONDARY_COLOR));

        JMenu fileMenu = createStyledMenu("File");
        JMenu toolsMenu = createStyledMenu("Tools");
        JMenu helpMenu = createStyledMenu("Help");

        JMenuItem instancesItem = createStyledMenuItem("Instances", "icons/instances.png");
        JMenuItem exitItem = createStyledMenuItem("Exit", "icons/exit.png");
        JMenuItem optionsItem = createStyledMenuItem("Options", "icons/settings.png");
        JMenuItem aboutItem = createStyledMenuItem("About", "icons/info.png");

        instancesItem.addActionListener(e -> main.getInstances().visible());
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(main,
                    "Are you sure you want to exit?", "Exit LaunchMine",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        optionsItem.addActionListener(e -> showOptionsDialog());
        aboutItem.addActionListener(e -> showAboutDialog());

        fileMenu.add(instancesItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);
        toolsMenu.add(optionsItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text, String iconPath) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(TEXT_COLOR);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Add hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(236, 240, 241));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });

        return item;
    }

    public JPanel createInstancesUpperButtonPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(PANEL_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create New Instance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        JLabel nameLabel = new JLabel("Instance Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(nameLabel, gbc);

        JTextField nameTextField = new JTextField(20);
        nameTextField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameTextField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPanel.add(nameTextField, gbc);

        JButton createBtn = createStyledButton("Create Instance", ACCENT_COLOR);
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        contentPanel.add(createBtn, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    public JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(PANEL_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("My Minecraft Instances");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Instance list
        List<String> instances = main.getAvailableInstances();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (!instances.isEmpty()) {
            for (String instance : instances) {
                listModel.addElement(instance);
            }
        } else {
            listModel.addElement("No instances found");
        }

        instanceList = new JList<>(listModel);
        instanceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instanceList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instanceList.setBackground(new Color(250, 250, 250));

        // Custom cell renderer for instances
        instanceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);

                String displayName = (String) value;

                // Style based on whether it's an actual instance or placeholder
                if (instances.isEmpty() || displayName.equals("No instances found")) {
                    label.setForeground(DISABLED_COLOR);
                    label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    label.setText("  " + displayName);
                } else {
                    // Get instance info for detailed display
                    Main.InstanceInfo instanceInfo = main.getInstanceInfo(displayName);

                    if (instanceInfo != null) {
                        // Show different icons based on version type
                        Color typeColor;
                        switch (instanceInfo.type) {
                            case "release":
                                typeColor = ACCENT_COLOR;
                                label.setIcon(new CircleIcon(8, typeColor));
                                break;
                            case "snapshot":
                                typeColor = new Color(241, 196, 15); // Yellow
                                label.setIcon(new TriangleIcon(8, typeColor));
                                break;
                            case "old_beta":
                                typeColor = new Color(155, 89, 182); // Purple
                                label.setIcon(new SquareIcon(8, typeColor));
                                break;
                            case "old_alpha":
                                typeColor = new Color(230, 126, 34); // Orange
                                label.setIcon(new DiamondIcon(8, typeColor));
                                break;
                            default:
                                typeColor = SECONDARY_COLOR;
                                label.setIcon(new CircleIcon(8, typeColor));
                        }

                        // Show status indicator
                        String status = instanceInfo.getStatus();
                        Color statusColor = instanceInfo.hasJar ? new Color(46, 204, 113) : new Color(231, 76, 60);

                        label.setText("<html><b>" + displayName + "</b><br>" +
                                "<font size='2' color='" + String.format("#%02x%02x%02x",
                                statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue()) +
                                "'>" + status + "</font></html>");
                    } else {
                        label.setText("  " + displayName);
                        label.setIcon(new CircleIcon(8, SECONDARY_COLOR));
                    }

                    label.setForeground(TEXT_COLOR);
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    label.setIconTextGap(10);
                }

                // Custom selection background
                if (isSelected && !instances.isEmpty()) {
                    label.setBackground(new Color(236, 240, 241));
                    label.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(SECONDARY_COLOR, 1),
                            new EmptyBorder(2, 2, 2, 2)
                    ));
                } else {
                    label.setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                }

                return label;
            }
        });

        // Add selection listener
        instanceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !instances.isEmpty()) {
                String selectedInstance = instanceList.getSelectedValue();
                if (selectedInstance != null && !selectedInstance.equals("No instances found")) {
                    main.setInstanceName(selectedInstance);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(instanceList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomPanel.setBackground(PANEL_BACKGROUND);

        JButton createInstanceBtn = createStyledButton("+ Create New Instance", ACCENT_COLOR);
        createInstanceBtn.addActionListener(e -> {
            main.getInstances().visible();
        });

        JButton refreshBtn = createStyledButton("⟳ Refresh", SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> {
            // Refresh the instance list
            main.refreshInstances();

            DefaultListModel<String> model = (DefaultListModel<String>) instanceList.getModel();
            model.clear();

            List<String> updatedInstances = main.getAvailableInstances();
            if (!updatedInstances.isEmpty()) {
                for (String instance : updatedInstances) {
                    model.addElement(instance);
                }
            } else {
                model.addElement("No instances found");
            }

            JOptionPane.showMessageDialog(main,
                    "Instance list refreshed! Found " + updatedInstances.size() + " instance(s)",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        bottomPanel.add(createInstanceBtn);
        bottomPanel.add(refreshBtn);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    public JPanel createButtonPanel(String instanceName) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(PANEL_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Instance name label
        this.instanceNameLabel = new JLabel(instanceName != null ? instanceName : "No Instance Selected");
        instanceNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        instanceNameLabel.setForeground(PRIMARY_COLOR);
        instanceNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instanceNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        contentPanel.add(instanceNameLabel);

        // Separator
        JSeparator splitter = new JSeparator(SwingConstants.HORIZONTAL);
        splitter.setForeground(new Color(220, 220, 220));
        splitter.setMaximumSize(new Dimension(200, 1));
        contentPanel.add(splitter);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Action buttons
        this.startInstanceBtn = createStyledButton("▶ Start", ACCENT_COLOR);
        this.killInstanceBtn = createStyledButton("⏹ Kill", WARNING_COLOR);
        this.editInstanceBtn = createStyledButton("✎ Edit", SECONDARY_COLOR);
        this.folderInstanceBtn = createStyledButton("📁 Folder", SECONDARY_COLOR);
        this.copyInstanceBtn = createStyledButton("⎘ Copy", SECONDARY_COLOR);
        this.delInstanceBtn = createStyledButton("🗑 Delete", WARNING_COLOR);

        // Set button sizes
        Dimension btnSize = new Dimension(main.getButtonWidth(), 35);
        startInstanceBtn.setMaximumSize(btnSize);
        killInstanceBtn.setMaximumSize(btnSize);
        editInstanceBtn.setMaximumSize(btnSize);
        folderInstanceBtn.setMaximumSize(btnSize);
        copyInstanceBtn.setMaximumSize(btnSize);
        delInstanceBtn.setMaximumSize(btnSize);

        // Initially disable buttons (will be enabled when instance is selected)
        startInstanceBtn.setEnabled(false);
        killInstanceBtn.setEnabled(false);
        editInstanceBtn.setEnabled(false);
        folderInstanceBtn.setEnabled(false);
        copyInstanceBtn.setEnabled(false);
        delInstanceBtn.setEnabled(false);

        // Add buttons to panel
        contentPanel.add(startInstanceBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(killInstanceBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(editInstanceBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(folderInstanceBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(copyInstanceBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(delInstanceBtn);

        // Add tooltips
        startInstanceBtn.setToolTipText("Start the selected Minecraft instance");
        killInstanceBtn.setToolTipText("Force stop the running instance");
        editInstanceBtn.setToolTipText("Edit instance settings");
        folderInstanceBtn.setToolTipText("Open instance folder");
        copyInstanceBtn.setToolTipText("Create a copy of this instance");
        delInstanceBtn.setToolTipText("Delete this instance");

        // Add action listeners
        addButtonListeners();

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private void addButtonListeners() {
        startInstanceBtn.addActionListener(e -> {
            String instance = main.getInstanceName();
            if (instance != null) {
                startMinecraftInstance(instance);
            }
        });

        killInstanceBtn.addActionListener(e -> {
            // TODO: Implement process termination
            JOptionPane.showMessageDialog(main,
                    "Kill functionality not yet implemented",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        editInstanceBtn.addActionListener(e -> {
            String instance = main.getInstanceName();
            if (instance != null) {
                editInstance(instance);
            }
        });

        folderInstanceBtn.addActionListener(e -> {
            String instance = main.getInstanceName();
            if (instance != null) {
                openInstanceFolder(instance);
            }
        });

        copyInstanceBtn.addActionListener(e -> {
            String instance = main.getInstanceName();
            if (instance != null) {
                copyInstance(instance);
            }
        });

        delInstanceBtn.addActionListener(e -> {
            String instance = main.getInstanceName();
            if (instance != null) {
                deleteInstance(instance);
            }
        });
    }

    private void startMinecraftInstance(String displayName) {
        Main.InstanceInfo instanceInfo = main.getInstanceInfo(displayName);
        if (instanceInfo == null) {
            JOptionPane.showMessageDialog(main,
                    "Instance not found: " + displayName,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask for player name
        String playerName = JOptionPane.showInputDialog(main,
                "Enter player name:",
                "Player Name",
                JOptionPane.QUESTION_MESSAGE);

        if (playerName == null || playerName.trim().isEmpty()) {
            return; // User cancelled
        }

        // Show launch options dialog
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        optionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        optionsPanel.add(new JLabel("Memory (MB):"));
        JSpinner memorySpinner = new JSpinner(new SpinnerNumberModel(2048, 512, 16384, 256));
        optionsPanel.add(memorySpinner);

        optionsPanel.add(new JLabel("Window Width:"));
        JTextField widthField = new JTextField("854");
        optionsPanel.add(widthField);

        optionsPanel.add(new JLabel("Window Height:"));
        JTextField heightField = new JTextField("480");
        optionsPanel.add(heightField);

        int result = JOptionPane.showConfirmDialog(main, optionsPanel,
                "Launch Options - " + displayName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Launch in a separate thread to avoid freezing UI
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(main,
                            "Launching " + displayName + "...\n" +
                                    "This may take a moment while files are validated.",
                            "Launching",
                            JOptionPane.INFORMATION_MESSAGE);
                });

                // Parse options
                int memoryMB = (Integer) memorySpinner.getValue();
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());

                // Launch Minecraft
                Process process = LaunchUtils.launchMinecraft(
                        instanceInfo.id,
                        playerName.trim(),
                        memoryMB,
                        width,
                        height
                );

                // Update UI to show that instance is running
                SwingUtilities.invokeLater(() -> {
                    killInstanceBtn.setEnabled(true);
                    startInstanceBtn.setEnabled(false);

                    // Store process reference (you might want to add this to Main class)
                    // main.setRunningProcess(process);
                });

                // Wait for process to exit
                int exitCode = process.waitFor();

                SwingUtilities.invokeLater(() -> {
                    killInstanceBtn.setEnabled(false);
                    startInstanceBtn.setEnabled(true);

                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(main,
                                displayName + " exited normally.",
                                "Game Ended",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(main,
                                displayName + " exited with code: " + exitCode,
                                "Game Ended",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });

            } catch (NumberFormatException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(main,
                                "Invalid number format: " + e.getMessage(),
                                "Launch Error",
                                JOptionPane.ERROR_MESSAGE));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(main,
                                "Failed to launch Minecraft:\n" + e.getMessage(),
                                "Launch Error",
                                JOptionPane.ERROR_MESSAGE));
                e.printStackTrace();
            }
        }).start();
    }


    private void editInstance(String displayName) {
        Main.InstanceInfo instanceInfo = main.getInstanceInfo(displayName);
        if (instanceInfo != null) {
            // Create an edit dialog with instance details
            JPanel editPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            editPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            editPanel.add(new JLabel("Version ID:"));
            editPanel.add(new JLabel(instanceInfo.id));

            editPanel.add(new JLabel("Type:"));
            editPanel.add(new JLabel(instanceInfo.type));

            editPanel.add(new JLabel("Main Class:"));
            editPanel.add(new JLabel(instanceInfo.mainClass));

            editPanel.add(new JLabel("Assets:"));
            editPanel.add(new JLabel(instanceInfo.assets));

            editPanel.add(new JLabel("Status:"));
            JLabel statusLabel = new JLabel(instanceInfo.getStatus());
            statusLabel.setForeground(instanceInfo.hasJar ? ACCENT_COLOR : WARNING_COLOR);
            editPanel.add(statusLabel);

            JOptionPane.showMessageDialog(main, editPanel,
                    "Instance Details: " + displayName,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openInstanceFolder(String displayName) {
        Main.InstanceInfo instanceInfo = main.getInstanceInfo(displayName);
        if (instanceInfo != null && instanceInfo.jsonPath != null) {
            File jsonFile = new File(instanceInfo.jsonPath);
            File versionFolder = jsonFile.getParentFile();

            try {
                // Open folder in system file explorer
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(versionFolder);
                } else {
                    JOptionPane.showMessageDialog(main,
                            "Cannot open folder. Path: " + versionFolder.getAbsolutePath(),
                            "Instance Folder",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(main,
                        "Error opening folder: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyInstance(String instanceName) {
        Main.InstanceInfo instanceInfo = main.getInstanceInfo(instanceName);
        if (instanceInfo == null) {
            return;
        }

        String newName = JOptionPane.showInputDialog(main,
                "Enter new instance name:",
                "Copy Instance",
                JOptionPane.QUESTION_MESSAGE);

        if (newName != null && !newName.trim().isEmpty()) {
            // Use the existing version ID and type from the original instance
            main.addInstance(instanceInfo.id, instanceInfo.type);

            // TODO: Actually copy the instance files
            JOptionPane.showMessageDialog(main,
                    "Copied " + instanceName + " to " + newName,
                    "Copy Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteInstance(String instanceName) {
        int confirm = JOptionPane.showConfirmDialog(main,
                "Are you sure you want to delete '" + instanceName + "'?",
                "Delete Instance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            main.removeInstance(instanceName);
            main.setInstanceName(null);
            // TODO: Actually delete instance files

            JOptionPane.showMessageDialog(main,
                    "Deleted instance: " + instanceName,
                    "Delete Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgColor.darker(), 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.brighter());
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });

        // Disabled state styling
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                if (!c.isEnabled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    super.paint(g2, c);
                    g2.dispose();
                } else {
                    super.paint(g, c);
                }
            }
        });

        return button;
    }

    public void updateInstanceName(String instanceName) {
        if (instanceNameLabel != null) {
            instanceNameLabel.setText(instanceName != null ? instanceName : "No Instance Selected");

            // Enable/disable buttons based on instance selection
            boolean hasInstance = instanceName != null && !instanceName.isEmpty();
            startInstanceBtn.setEnabled(hasInstance);
            editInstanceBtn.setEnabled(hasInstance);
            folderInstanceBtn.setEnabled(hasInstance);
            copyInstanceBtn.setEnabled(hasInstance);
            delInstanceBtn.setEnabled(hasInstance);
            killInstanceBtn.setEnabled(false); // Only enabled when instance is running
        }

        if (instanceList != null && instanceName != null) {
            instanceList.setSelectedValue(instanceName, true);
        }
    }

    public JPanel createInstancesMiddlePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(PANEL_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Select Minecraft Version");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Try to load versions
        List<Map<String, Object>> versions = null;
        try {
            versions = JsonUtils.readVersionListFromFile("version_manifest_v2.json");
        } catch (Exception e) {
            System.err.println("Error loading versions: " + e.getMessage());
        }

        DefaultListModel<Map<String, Object>> listModel = new DefaultListModel<>();

        if (versions != null && !versions.isEmpty()) {
            for (Map<String, Object> item : versions) {
                listModel.addElement(item);
            }
        } else {
            // Show error message if no versions loaded
            JLabel errorLabel = new JLabel("<html><center>No versions available.<br>"
                    + "Please check your internet connection and try again.</center></html>");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorLabel.setForeground(DISABLED_COLOR);
            errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            contentPanel.add(errorLabel, BorderLayout.CENTER);

            // Add refresh button
            JButton refreshBtn = createStyledButton("⟳ Refresh List", SECONDARY_COLOR);
            refreshBtn.addActionListener(e -> {
                try {
                    InstanceUtils.updateVersionManifest();
                    JOptionPane.showMessageDialog(main,
                            "Version list refreshed successfully!",
                            "Refresh Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(main,
                            "Failed to refresh: " + ex.getMessage(),
                            "Refresh Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            errorPanel.add(refreshBtn, BorderLayout.SOUTH);
            contentPanel.add(errorPanel, BorderLayout.CENTER);
        }

        if (versions != null && !versions.isEmpty()) {
            JList<Map<String, Object>> versionList = new JList<>(listModel);
            versionList.setCellRenderer(new VersionListCellRenderer());
            versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            versionList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            versionList.setBackground(new Color(250, 250, 250));

            JScrollPane scrollPane = new JScrollPane(versionList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            scrollPane.getViewport().setBackground(Color.WHITE);

            contentPanel.add(scrollPane, BorderLayout.CENTER);

            // Bottom panel with OK button
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(PANEL_BACKGROUND);

            this.confirmBtn = createStyledButton("Select Version", ACCENT_COLOR);
            confirmBtn.setPreferredSize(new Dimension(120, 35));
            confirmBtn.setEnabled(false);

            versionList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    selectedVersion = versionList.getSelectedValue();
                    confirmBtn.setEnabled(selectedVersion != null);
                }
            });

            confirmBtn.addActionListener(e -> {
                if (selectedVersion != null) {
                    // Process selected version
                    String versionId = (String) selectedVersion.get("id");
                    String type = (String) selectedVersion.get("type");

                    // Show confirmation dialog
                    int choice = JOptionPane.showConfirmDialog(main,
                            "Download and setup version " + versionId + " (" + type + ")?",
                            "Confirm Version Selection",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (choice == JOptionPane.YES_OPTION) {
                        // Download the version in a separate thread to avoid UI freeze
                        new Thread(() -> {
                            try {
                                InstanceUtils.downloadVersionJson(versionId);
                                SwingUtilities.invokeLater(() ->
                                        JOptionPane.showMessageDialog(main,
                                                "Version " + versionId + " downloaded successfully!",
                                                "Download Complete",
                                                JOptionPane.INFORMATION_MESSAGE));
                            } catch (Exception ex) {
                                SwingUtilities.invokeLater(() ->
                                        JOptionPane.showMessageDialog(main,
                                                "Error downloading version: " + ex.getMessage(),
                                                "Download Error",
                                                JOptionPane.ERROR_MESSAGE));
                            }
                        }).start();
                    }
                }
            });

            bottomPanel.add(confirmBtn);
            contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        }

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    // Custom cell renderer for version list
    private class VersionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);

            @SuppressWarnings("unchecked")
            Map<String, Object> version = (Map<String, Object>) value;

            String id = (String) version.get("id");
            String type = (String) version.get("type");
            String releaseTime = (String) version.get("releaseTime");

            // Format the display text
            label.setText("<html><b>" + id + "</b> (" + type + ")<br>" +
                    "<font size='2' color='#7f8c8d'>Released: " + releaseTime + "</font></html>");

            // Set icon based on version type
            Color typeColor;
            switch (type.toLowerCase()) {
                case "release":
                    typeColor = ACCENT_COLOR;
                    break;
                case "snapshot":
                    typeColor = new Color(241, 196, 15); // Yellow
                    break;
                case "old_beta":
                    typeColor = new Color(155, 89, 182); // Purple
                    break;
                case "old_alpha":
                    typeColor = new Color(230, 126, 34); // Orange
                    break;
                default:
                    typeColor = SECONDARY_COLOR;
            }

            // Create a colored circle icon
            label.setIcon(new CircleIcon(8, typeColor));
            label.setIconTextGap(10);

            // Custom selection background
            if (isSelected) {
                label.setBackground(new Color(236, 240, 241)); // Light gray
                label.setForeground(TEXT_COLOR);
                label.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(SECONDARY_COLOR, 1),
                        new EmptyBorder(2, 2, 2, 2)
                ));
            } else {
                label.setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            }

            return label;
        }
    }

    // Simple circle icon for version types
    private static class CircleIcon implements Icon {
        private final int size;
        private final Color color;

        public CircleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private void showOptionsDialog() {
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        optionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        optionsPanel.add(new JLabel("Memory (MB):"));
        JSpinner memorySpinner = new JSpinner(new SpinnerNumberModel(2048, 1024, 16384, 256));
        optionsPanel.add(memorySpinner);

        optionsPanel.add(new JLabel("Window Width:"));
        JTextField widthField = new JTextField("854");
        optionsPanel.add(widthField);

        optionsPanel.add(new JLabel("Window Height:"));
        JTextField heightField = new JTextField("480");
        optionsPanel.add(heightField);

        optionsPanel.add(new JLabel("Java Path:"));
        JTextField javaPathField = new JTextField(System.getProperty("java.home"));
        optionsPanel.add(javaPathField);

        int result = JOptionPane.showConfirmDialog(main, optionsPanel,
                "LaunchMine Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Save options here
            JOptionPane.showMessageDialog(main,
                    "Options saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAboutDialog() {
        String aboutText = "<html><center>" +
                "<h2>LaunchMine</h2>" +
                "<p>Version 1.0.0</p>" +
                "<p>A custom Minecraft launcher built with Java Swing</p>" +
                "<br>" +
                "<p><small>Created by Eric</small></p>" +
                "</center></html>";

        JOptionPane.showMessageDialog(main, aboutText,
                "About LaunchMine", JOptionPane.INFORMATION_MESSAGE);
    }
    private static class TriangleIcon implements Icon {
        private final int size;
        private final Color color;

        public TriangleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int[] xPoints = {x, x + size, x + size/2};
            int[] yPoints = {y + size, y + size, y};
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    // Square icon for betas
    private static class SquareIcon implements Icon {
        private final int size;
        private final Color color;

        public SquareIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRect(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    // Diamond icon for alphas
    private static class DiamondIcon implements Icon {
        private final int size;
        private final Color color;

        public DiamondIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int[] xPoints = {x + size/2, x + size, x + size/2, x};
            int[] yPoints = {y, y + size/2, y + size, y + size/2};
            g2.fillPolygon(xPoints, yPoints, 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}