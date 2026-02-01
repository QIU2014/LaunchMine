package com.eric;

import com.eric.ui.AboutDialog;
import com.eric.ui.OptionsDialog;
import com.eric.utils.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.*;
import java.awt.desktop.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main class of LaunchMine and also handler of the creation of Main frame
 */
public class Main extends JFrame {
    private UI ui;
    private NetUtils net;
    private JsonUtils json;
    private Instances instances;
    private InstanceUtils instanceUtils;
    private Timer memoryMonitor;
    private PreferencesUtils preferencesHandler;
    private MacOSAppListener macOSListener;
    private static Main main;
    public static final int BUTTON_WIDTH = 120;
    public static final int BUTTON_HEIGHT = 40;
    public static final String VERSION = "1.0.0";
    private static String instanceName = null;
    private Map<String, InstanceInfo> availableInstances = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public Main() {
        // Initialize utilities first
        this.net = new NetUtils();
        this.json = new JsonUtils();
        this.preferencesHandler = new PreferencesUtils();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            setupMacOSIntegration();
        }

        // Download version manifest if it doesn't exist
        downloadVersionManifest();

        // Scan for existing instances FIRST
        scanForInstancesInVersions();

        // Then create UI components (instances are already loaded)
        this.ui = new UI(this);
        // this.instances = new Instances(this, this, ui);

        LaunchUtils.setParentFrame(this);

        // Set up window
        setTitle("LaunchMine");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setIconImage(createAppIcon());

        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(e -> new AboutDialog(main));
            desktop.setPreferencesHandler(e -> new OptionsDialog(main));
            desktop.setDefaultMenuBar(ui.createMenuBar());
            desktop.setQuitHandler((e, response) -> quitApplication());
        } catch (Exception e) {
            setupNonMacOSIntegration();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitApplication();
            }
        });

        Container container = getContentPane();
        container.setLayout(new BorderLayout(20, 0));

        // Add main content panel (shows instances)
        JPanel mainContent = ui.createMainContentPanel();
        container.add(mainContent, BorderLayout.CENTER);

        // Add button panel on the right
        JPanel buttonPanel = ui.createButtonPanel(instanceName);
        container.add(buttonPanel, BorderLayout.EAST);

        // Validate versions in background AFTER UI is shown
        SwingUtilities.invokeLater(() -> {
            validateAllVersionsOnStartup();
        });
    }

    private void setupNonMacOSIntegration() {
        setJMenuBar(ui.createMenuBar());
        setupNonMacOSShortcuts();
    }

    private void setupNonMacOSShortcuts() {
        // Set up AFTER UI is initialized
        SwingUtilities.invokeLater(() -> {
            // About shortcut: Ctrl+Shift+A (Windows/Linux)
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_A,
                            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                    "openAbout");

            getRootPane().getActionMap().put("openAbout", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ui != null) {
                        ui.showAboutDialog();
                    }
                }
            });

            // Preferences shortcut: Ctrl+, (Windows/Linux)
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                            KeyEvent.CTRL_DOWN_MASK),
                    "openPreferences");

            getRootPane().getActionMap().put("openPreferences", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ui != null) {
                        ui.showOptionsDialog();
                    }
                }
            });

            // Quit shortcut: Alt+F4 (Windows/Linux) or Ctrl+Q
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                            KeyEvent.CTRL_DOWN_MASK),
                    "quitApplication");

            getRootPane().getActionMap().put("quitApplication", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    quitApplication();
                }
            });

            // Also add Escape key to close dialogs
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    "cancelAction");

            getRootPane().getActionMap().put("cancelAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Close any open dialogs
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        if (window instanceof JDialog && window.isVisible()) {
                            ((JDialog) window).dispose();
                        }
                    }
                }
            });
        });
    }

    private void quitApplication() {
        int confirm = JOptionPane.showConfirmDialog(main,
                "Are you sure you want to exit LaunchMine?",
                "Exit LaunchMine",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void setupMacOSIntegration() {
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            return;
        }

        // Enable system menu bar (standard macOS behavior)
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "LaunchMine");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LaunchMine");
        System.setProperty("apple.awt.textantialiasing", "true");
        setupMacOSShortcuts();
    }

    private void setupMacOSShortcuts() {
        // Set up AFTER UI is initialized
        SwingUtilities.invokeLater(() -> {
            // About shortcut: Cmd+Shift+A (standard for custom About in Help menu)
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_A,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK),
                    "openAbout");

            getRootPane().getActionMap().put("openAbout", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ui != null) {
                        ui.showAboutDialog();
                    }
                }
            });

            // Preferences shortcut: Cmd+, (standard macOS shortcut)
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                    "openPreferences");

            getRootPane().getActionMap().put("openPreferences", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ui != null) {
                        ui.showOptionsDialog();
                    }
                }
            });
        });
    }

    private Image createAppIcon() {
        BufferedImage icon = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();

        // Draw background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(41, 128, 185),
                256, 256, new Color(52, 152, 219));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, 256, 256, 50, 50);

        // Draw "LM" text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 100));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "LM";
        int x = (256 - fm.stringWidth(text)) / 2;
        int y = (256 - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);

        g2d.dispose();
        return icon;
    }

    private void setupMacOSHandlers() {
        // The About menu item in the macOS application menu
        // will trigger this when clicked
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {}
        });

        // Set up global key listener for Cmd+, (Preferences)
        getRootPane().registerKeyboardAction(
                e -> ui.showOptionsDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set up global key listener for Cmd+Q (Quit)
        getRootPane().registerKeyboardAction(
                e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to quit LaunchMine?",
                            "Quit LaunchMine",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void refreshMenuBar() {
        SwingUtilities.invokeLater(() -> {
            setJMenuBar(ui.createMenuBar());
            revalidate();
            repaint();
        });
    }

    private void startMemoryMonitoring() {
        memoryMonitor = new Timer(30000, e -> checkMemoryUsage()); // Every 30 seconds
        memoryMonitor.start();
    }

    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        double usagePercentage = (double) usedMemory / maxMemory * 100;

        if (usagePercentage > 80) {
            System.out.println("High memory usage: " + usagePercentage + "%");
            onLowMemory();
        }
    }

    public void cleanup() {
        if (instanceUtils.getVersionsCache() != null) {
            instanceUtils.getVersionsCache().clear();
            InstanceUtils.setVersionsCache(null);
        }

        // Clear other caches
        availableInstances.clear();

        // Help garbage collection
        System.gc();
    }

    // Call this when switching views or on low memory
    public void onLowMemory() {
        cleanup();
    }

    private void initializeBackgroundTasks() {
        // Initialize utilities
        this.net = new NetUtils();
        this.json = new JsonUtils();

        // Download version manifest in background
        new Thread(() -> {
            downloadVersionManifest();
            scanForInstancesInVersions();
            validateAllVersionsOnStartup();

            SwingUtilities.invokeLater(() -> {
                if (ui != null) {
                    // 更新实例列表
                    refreshInstances();
                }
            });
        }).start();
    }

    public void refreshUIInstanceList() {
        if (ui != null && ui.instanceList != null) {
            SwingUtilities.invokeLater(() -> {
                DefaultListModel<String> model = (DefaultListModel<String>) ui.instanceList.getModel();
                model.clear();

                List<String> instances = getAvailableInstances();
                if (!instances.isEmpty()) {
                    for (String instance : instances) {
                        model.addElement(instance);
                    }
                } else {
                    model.addElement("No instances found");
                }

                ui.instanceList.revalidate();
                ui.instanceList.repaint();
            });
        }
    }

    private void buildUIComponents() {
        Container container = getContentPane();

        // Add main content panel
        JPanel mainContent = ui.createMainContentPanel();
        container.add(mainContent, BorderLayout.CENTER);

        // Add button panel
        JPanel buttonPanel = ui.createButtonPanel(instanceName);
        container.add(buttonPanel, BorderLayout.EAST);

        // Add menu bar
        setJMenuBar(ui.createMenuBar());

        // Refresh the UI
        revalidate();
        repaint();
    }


    /**
     * Download the version manifest on startup if it doesn't exist
     */
    private void downloadVersionManifest() {
        try {
            if (!new File("version_manifest_v2.json").exists()) {
                System.out.println("Downloading version manifest...");
                InstanceUtils.updateVersionManifest();
                System.out.println("Version manifest downloaded successfully!");
            } else {
                System.out.println("Version manifest already exists.");
            }
        } catch (Exception e) {
            System.err.println("Failed to download version manifest: " + e.getMessage());

            SwingUtilities.invokeLater(() -> {
                int choice = JOptionPane.showConfirmDialog(null,
                        "Failed to download version list. Would you like to retry?\n" +
                                "Error: " + e.getMessage(),
                        "Download Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    downloadVersionManifest();
                }
            });
        }
    }

    /**
     * Scan for valid Minecraft instances in the versions folder
     */
    private void scanForInstancesInVersions() {
        File minecraftDir = new File("./.minecraft");

        if (!minecraftDir.exists()) {
            minecraftDir.mkdirs();
            System.out.println("DEBUG: Created .minecraft directory");
            return;
        }

        // Check the versions folder
        File versionsDir = new File(minecraftDir, "versions");
        if (!versionsDir.exists()) {
            versionsDir.mkdirs();
            System.out.println("DEBUG: Created versions directory");
            return;
        }

        System.out.println("DEBUG: Scanning for instances in: " + versionsDir.getAbsolutePath());

        // List all directories in versions folder
        File[] versionFolders = versionsDir.listFiles(File::isDirectory);
        if (versionFolders == null || versionFolders.length == 0) {
            System.out.println("DEBUG: No version folders found");
            return;
        }

        System.out.println("DEBUG: Found " + versionFolders.length + " version folders");

        int validInstances = 0;
        for (File versionFolder : versionFolders) {
            String versionId = versionFolder.getName();
            System.out.println("DEBUG: Checking folder: " + versionId);

            // Check for version JSON file
            File versionJsonFile = new File(versionFolder, versionId + ".json");
            if (!versionJsonFile.exists()) {
                System.out.println("Skipping " + versionId + ": No JSON file found");
                continue;
            }

            // Check for client JAR file
            File clientJarFile = new File(versionFolder, versionId + ".jar");

            try {
                // Parse the JSON to get version details
                JsonNode versionJson = objectMapper.readTree(versionJsonFile);

                // Create instance info
                InstanceInfo instanceInfo = new InstanceInfo();
                instanceInfo.id = versionId;
                instanceInfo.type = versionJson.has("type") ? versionJson.get("type").asText() : "unknown";
                instanceInfo.mainClass = versionJson.has("mainClass") ? versionJson.get("mainClass").asText() : "";
                instanceInfo.assets = versionJson.has("assets") ? versionJson.get("assets").asText() : "";
                instanceInfo.jsonPath = versionJsonFile.getAbsolutePath();
                instanceInfo.jarPath = clientJarFile.getAbsolutePath();
                instanceInfo.hasJar = clientJarFile.exists();
                instanceInfo.releaseTime = versionJson.has("releaseTime") ? versionJson.get("releaseTime").asText() : "";

                // Determine display name
                String displayName;
                switch (instanceInfo.type) {
                    case "release":
                        displayName = "Minecraft " + versionId;
                        break;
                    case "snapshot":
                        displayName = "Snapshot " + versionId;
                        break;
                    case "old_beta":
                        displayName = "Beta " + versionId;
                        break;
                    case "old_alpha":
                        displayName = "Alpha " + versionId;
                        break;
                    default:
                        displayName = versionId;
                }

                // Add to available instances
                availableInstances.put(displayName, instanceInfo);
                validInstances++;

                System.out.println("Found instance: " + displayName +
                        " (JAR: " + (instanceInfo.hasJar ? "Yes" : "No") + ")");

            } catch (IOException e) {
                System.err.println("Error parsing JSON for " + versionId + ": " + e.getMessage());
            }
        }

        System.out.println("Found " + validInstances + " valid instance(s)");

        refreshUIInstanceList();
    }

    /**
     * Validate all installed versions on startup
     */
    private void validateAllVersionsOnStartup() {
        new Thread(() -> {
            try {
                LaunchUtils.validateAllVersionsOnStartup();
            } catch (Exception e) {
                System.err.println("Error during version validation: " + e.getMessage());
            }
        }).start(); // Run in background thread to not block UI startup
    }

    /**
     * Get list of available instance display names
     */
    public List<String> getAvailableInstances() {
        return new ArrayList<>(availableInstances.keySet());
    }

    /**
     * Get instance info by display name
     */
    public InstanceInfo getInstanceInfo(String displayName) {
        return availableInstances.get(displayName);
    }

    /**
     * Add a new instance
     */
    public void addInstance(String versionId, String type) {
        String displayName;
        switch (type) {
            case "release":
                displayName = "Minecraft " + versionId;
                break;
            case "snapshot":
                displayName = "Snapshot " + versionId;
                break;
            case "old_beta":
                displayName = "Beta " + versionId;
                break;
            case "old_alpha":
                displayName = "Alpha " + versionId;
                break;
            default:
                displayName = versionId;
        }

        if (!availableInstances.containsKey(displayName)) {
            InstanceInfo instanceInfo = new InstanceInfo();
            instanceInfo.id = versionId;
            instanceInfo.type = type;
            instanceInfo.displayName = displayName;
            availableInstances.put(displayName, instanceInfo);
            System.out.println("Added instance: " + displayName);
        }
    }

    /**
     * Remove an instance
     */
    public void removeInstance(String displayName) {
        availableInstances.remove(displayName);
        System.out.println("Removed instance: " + displayName);
    }

    /**
     * Check if an instance exists
     */
    public boolean hasInstance(String displayName) {
        return availableInstances.containsKey(displayName);
    }

    /**
     * Refresh the instance list
     */
    public void refreshInstances() {
        availableInstances.clear();
        scanForInstancesInVersions();
    }

    public UI getLaunchUI() { return ui; }
    public void setLaunchUI(UI ui) { this.ui = ui; }

    public NetUtils getNetUtils() { return net; }
    public void setNetUtils(NetUtils net) { this.net = net; }

    public JsonUtils getJsonUtils() { return json; }
    public void setJsonUtils(JsonUtils json) { this.json = json; }

    public Instances getInstances() {
        if (instances == null) {
            instances = new Instances(this, ui);
        }
        return instances;
    }

    public int getButtonWidth() { return BUTTON_WIDTH; }
    public int getButtonHeight() { return BUTTON_HEIGHT; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) {
        Main.instanceName = instanceName;
        if (ui != null) {
            ui.updateInstanceName(instanceName);
        }
    }

    public String getVersion() { return VERSION; }

    public PreferencesUtils getPreferencesHandler() { return preferencesHandler; }

    static void main(String[] args) {
        boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");

        if (isMacOS) {
            System.setProperty("apple.awt.application.name", "LaunchMine");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "LaunchMine");
            System.setProperty("apple.awt.textantialiasing", "true");
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Additional macOS-specific UI settings
                if (isMacOS) {
                    // Force the menu bar to appear
                    UIManager.put("MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI");
                }

                main = new Main();
                main.setVisible(true);

                // Set menu bar after window is created
                main.setJMenuBar(main.ui.createMenuBar());
                main.revalidate();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start LaunchMine: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Inner class to hold instance information
     */
    public static class InstanceInfo {
        public String id;
        public String type;
        public String displayName;
        public String mainClass;
        public String assets;
        public String jsonPath;
        public String jarPath;
        public boolean hasJar;
        public String releaseTime;

        @Override
        public String toString() {
            return displayName != null ? displayName : id;
        }

        public String getStatus() {
            if (hasJar) {
                return "Ready to play";
            } else {
                return "Missing JAR file";
            }
        }
    }
}