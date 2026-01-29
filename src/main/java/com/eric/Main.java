package com.eric;

import com.eric.utils.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JFrame {
    private UI ui;
    private NetUtils net;
    private JsonUtils json;
    private Instances instances;
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

        // Download version manifest if it doesn't exist
        downloadVersionManifest();

        // Validate all installed versions on startup
        validateAllVersionsOnStartup();

        // Scan for existing instances in versions folder
        scanForInstancesInVersions();

        // Then create UI components
        this.ui = new UI(this);
        this.instances = new Instances(this, ui);

        LaunchUtils.setParentFrame(this);
        setTitle("LaunchMine");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        Container container = getContentPane();
        container.setLayout(new BorderLayout(20, 0));

        // Add main content panel (shows instances)
        JPanel mainContent = ui.createMainContentPanel();
        container.add(mainContent, BorderLayout.CENTER);

        // Add button panel on the right
        JPanel buttonPanel = ui.createButtonPanel(instanceName);
        container.add(buttonPanel, BorderLayout.EAST);

        setJMenuBar(ui.createMenuBar());
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
            System.out.println("Created .minecraft directory");
            return;
        }

        // Check the versions folder
        File versionsDir = new File(minecraftDir, "versions");
        if (!versionsDir.exists()) {
            versionsDir.mkdirs();
            System.out.println("Created versions directory");
            return;
        }

        System.out.println("Scanning for instances in: " + versionsDir.getAbsolutePath());

        // List all directories in versions folder
        File[] versionFolders = versionsDir.listFiles(File::isDirectory);
        if (versionFolders == null || versionFolders.length == 0) {
            System.out.println("No version folders found");
            return;
        }

        int validInstances = 0;
        for (File versionFolder : versionFolders) {
            String versionId = versionFolder.getName();

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

    public Instances getInstances() { return instances; }

    public int getButtonWidth() { return BUTTON_WIDTH; }
    public int getButtonHeight() { return BUTTON_HEIGHT; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) {
        Main.instanceName = instanceName;
        if (ui != null) {
            ui.updateInstanceName(instanceName);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Main main = new Main();
            main.setVisible(true);
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