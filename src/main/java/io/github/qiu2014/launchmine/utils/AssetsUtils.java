package io.github.qiu2014.launchmine.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for assets managing
 */
public class AssetsUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static Logger assetsLogger = LogManager.getLogger();

    /**
     * Download all client libraries from the Minecraft version JSON file
     * @param jsonFilePath Path to the version JSON file (e.g., "1.21.10.json")
     * @param outputDir Directory where downloaded libraries should be saved
     * @throws IOException If there's an error reading the JSON or downloading files
     */
    public static void downloadClientLibraries(String jsonFilePath, String outputDir) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }

        // Create output directory if it doesn't exist
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Parse JSON file
        JsonNode versionJson = objectMapper.readTree(jsonFile);

        // Extract libraries array
        JsonNode libraries = versionJson.get("libraries");
        if (libraries == null || !libraries.isArray()) {
            throw new IOException("No libraries found in JSON");
        }

        List<DownloadTask> downloadTasks = new ArrayList<>();

        // Parse each library entry
        for (JsonNode libNode : libraries) {
            String name = libNode.get("name").asText();

            // Check if library has rules (platform-specific)
            if (libNode.has("rules")) {
                JsonNode rules = libNode.get("rules");
                if (!shouldIncludeLibrary(rules)) {
                    continue; // Skip this library based on rules
                }
            }

            // Get download information
            if (libNode.has("downloads") && libNode.get("downloads").has("artifact")) {
                JsonNode artifact = libNode.get("downloads").get("artifact");
                String url = artifact.get("url").asText();
                String path = artifact.get("path").asText();

                // Create download task
                String outputPath = outputDir + File.separator + path;
                downloadTasks.add(new DownloadTask(url, outputPath, name));
            }

            // Also check for classifiers (natives)
            if (libNode.has("downloads") && libNode.get("downloads").has("classifiers")) {
                JsonNode classifiers = libNode.get("downloads").get("classifiers");
                Iterator<String> classifierNames = classifiers.fieldNames();

                while (classifierNames.hasNext()) {
                    String classifier = classifierNames.next();
                    JsonNode classifierArtifact = classifiers.get(classifier);

                    // Check rules for this specific classifier
                    boolean includeClassifier = true;
                    if (libNode.has("natives")) {
                        JsonNode natives = libNode.get("natives");
                        if (natives.has(classifier)) {}
                    }

                    if (includeClassifier) {
                        String url = classifierArtifact.get("url").asText();
                        String path = classifierArtifact.get("path").asText();

                        String outputPath = outputDir + File.separator + path;
                        downloadTasks.add(new DownloadTask(url, outputPath, name + ":" + classifier));
                    }
                }
            }
        }

        // Download all artifacts
        System.out.println("Downloading " + downloadTasks.size() + " libraries...");
        for (DownloadTask task : downloadTasks) {
            try {
                downloadArtifact(task);
            } catch (IOException e) {
                System.err.println("Failed to download " + task.name + ": " + e.getMessage());
                // Continue with other downloads even if one fails
            }
        }
    }

    /**
     * Download a specific artifact by name
     * @param jsonFilePath Path to the version JSON file
     * @param libraryName Name of the library to download (e.g., "com.google.guava:guava:33.3.1-jre")
     * @param outputDir Directory where downloaded file should be saved
     * @throws IOException If library not found or download fails
     */
    public static void downloadSpecificLibrary(String jsonFilePath, String libraryName, String outputDir) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }

        // Parse JSON file
        JsonNode versionJson = objectMapper.readTree(jsonFile);

        // Find the specific library
        JsonNode libraries = versionJson.get("libraries");
        for (JsonNode libNode : libraries) {
            String name = libNode.get("name").asText();

            if (name.equals(libraryName)) {
                // Check rules
                if (libNode.has("rules")) {
                    JsonNode rules = libNode.get("rules");
                    if (!shouldIncludeLibrary(rules)) {
                        throw new IOException("Library " + libraryName + " is not applicable for this platform");
                    }
                }

                // Get download information
                if (libNode.has("downloads") && libNode.get("downloads").has("artifact")) {
                    JsonNode artifact = libNode.get("downloads").get("artifact");
                    String url = artifact.get("url").asText();
                    String path = artifact.get("path").asText();

                    // Create output directory structure
                    String outputPath = outputDir + File.separator + path;
                    File outputFile = new File(outputPath);
                    outputFile.getParentFile().mkdirs();

                    // Download the artifact
                    System.out.println("Downloading " + name + "...");
                    try {
                        NetUtils.downloadArtifact(url, outputPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Downloaded to: " + outputPath);
                    return;
                }
            }
        }

        throw new IOException("Library not found: " + libraryName);
    }

    /**
     * Download client and server JARs
     * @param jsonFilePath Path to the version JSON file
     * @param outputDir Directory where downloaded files should be saved
     * @throws IOException If download fails
     */
    public static void downloadClientAndServerJars(String jsonFilePath, String outputDir) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }

        // Parse JSON file
        JsonNode versionJson = objectMapper.readTree(jsonFile);

        // Create output directory
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Download client JAR
        if (versionJson.has("downloads") && versionJson.get("downloads").has("client")) {
            JsonNode client = versionJson.get("downloads").get("client");
            String url = client.get("url").asText();
            String sha1 = client.get("sha1").asText();
            long size = client.get("size").asLong();

            String clientPath = outputDir + File.separator + "client.jar";
            System.out.println("Downloading client JAR (" + size + " bytes, SHA1: " + sha1 + ")...");
            try {
                NetUtils.downloadArtifact(url, clientPath);
            } catch (Exception e) {
                assetsLogger.error(e.getStackTrace());
            }
            System.out.println("Client JAR downloaded to: " + clientPath);
        }

        // Download server JAR
        if (versionJson.has("downloads") && versionJson.get("downloads").has("server")) {
            JsonNode server = versionJson.get("downloads").get("server");
            String url = server.get("url").asText();
            String sha1 = server.get("sha1").asText();
            long size = server.get("size").asLong();

            String serverPath = outputDir + File.separator + "server.jar";
            System.out.println("Downloading server JAR (" + size + " bytes, SHA1: " + sha1 + ")...");
            try {
                NetUtils.downloadArtifact(url, serverPath);
            } catch (Exception e) {
                assetsLogger.error(e.getStackTrace());
            }
            System.out.println("Server JAR downloaded to: " + serverPath);
        }
    }

    /**
     * Download assets (resource files)
     * @param jsonFilePath Path to the version JSON file
     * @param outputDir Directory where downloaded assets should be saved
     * @throws IOException If download fails
     */
    public static void downloadAssets(String jsonFilePath, String outputDir) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }

        // Parse JSON file
        JsonNode versionJson = objectMapper.readTree(jsonFile);

        // Get asset index information
        if (versionJson.has("assetIndex")) {
            JsonNode assetIndex = versionJson.get("assetIndex");
            String assetsId = versionJson.get("assets").asText();
            String assetIndexUrl = assetIndex.get("url").asText();
            String assetIndexSha1 = assetIndex.get("sha1").asText();
            long assetIndexSize = assetIndex.get("size").asLong();

            // Create assets directory
            File assetsDir = new File(outputDir + File.separator + "assets");
            assetsDir.mkdirs();

            // Download asset index JSON
            String assetIndexPath = outputDir + File.separator + "indexes" + File.separator + assetsId + ".json";
            new File(assetIndexPath).getParentFile().mkdirs();

            System.out.println("Downloading asset index (" + assetIndexSize + " bytes, SHA1: " + assetIndexSha1 + ")...");
            try {
                NetUtils.downloadArtifact(assetIndexUrl, assetIndexPath);
            } catch (Exception e) {
                assetsLogger.error(e.getStackTrace());
            }
            System.out.println("Note: Asset index downloaded. To download individual assets, ");
            System.out.println("      you would need to parse the asset index JSON and download each file.");
        }
    }

    /**
     * Get all library names from the JSON file
     * @param jsonFilePath Path to the version JSON file
     * @return List of library names
     * @throws IOException If JSON file cannot be read
     */
    public static List<String> getAllLibraryNames(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new IOException("JSON file not found: " + jsonFilePath);
        }

        JsonNode versionJson = objectMapper.readTree(jsonFile);
        JsonNode libraries = versionJson.get("libraries");

        List<String> libraryNames = new ArrayList<>();
        for (JsonNode libNode : libraries) {
            libraryNames.add(libNode.get("name").asText());
        }

        return libraryNames;
    }

    /**
     * Check if a library should be included based on platform rules
     * @param rules JSON array of rules
     * @return true if library should be included, false otherwise
     */
    private static boolean shouldIncludeLibrary(JsonNode rules) {
        if (rules == null || !rules.isArray()) {
            return true;
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        for (JsonNode ruleNode : rules) {
            String action = ruleNode.get("action").asText();

            if (ruleNode.has("os")) {
                JsonNode osRule = ruleNode.get("os");
                boolean matches = true;

                if (osRule.has("name")) {
                    String requiredOs = osRule.get("name").asText().toLowerCase();
                    // Map "osx" to "Mac" for comparison
                    if (requiredOs.equals("osx")) {
                        requiredOs = "mac";
                    }
                    matches = matches && osName.contains(requiredOs);
                }

                if (osRule.has("arch")) {
                    String requiredArch = osRule.get("arch").asText().toLowerCase();
                    matches = matches && osArch.contains(requiredArch);
                }

                if (matches) {
                    return action.equals("allow");
                }
            } else if (ruleNode.has("features")) {
                // For feature-based rules
                // In a real implementation, you'd check Minecraft feature flags
                // For now, we'll assume allow for demo/user-related features
                return action.equals("allow");
            }
        }

        // Default: include if no rules match (for "allow" rules)
        // For "disallow" rules, we would need different logic
        return true;
    }

    /**
     * Helper method to download a single artifact
     * @param task The task to download See {@link AssetsUtils.DownloadTask}
     */
    private static void downloadArtifact(DownloadTask task) throws IOException {
        // Create directory structure
        File outputFile = new File(task.outputPath);
        outputFile.getParentFile().mkdirs();

        assetsLogger.info("Downloading {}...", task.name);
        try {
            NetUtils.downloadArtifact(task.url, task.outputPath);
        } catch (Exception e) {
            assetsLogger.error(e.getStackTrace());
        }
        assetsLogger.info("Downloaded: {}", task.outputPath);
    }

    /**
     * Get version information from JSON
     * @param jsonFilePath Path to the version JSON file
     * @return Version information as a string
     * @throws IOException If JSON file cannot be read
     */
    public static String getVersionInfo(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        JsonNode versionJson = objectMapper.readTree(jsonFile);

        String id = versionJson.get("id").asText();
        String type = versionJson.get("type").asText();
        String mainClass = versionJson.get("mainClass").asText();
        String releaseTime = versionJson.get("releaseTime").asText();

        StringBuilder info = new StringBuilder();
        info.append("Version: ").append(id).append("\n");
        info.append("Type: ").append(type).append("\n");
        info.append("Main Class: ").append(mainClass).append("\n");
        info.append("Release Time: ").append(releaseTime).append("\n");

        if (versionJson.has("javaVersion")) {
            JsonNode javaVersion = versionJson.get("javaVersion");
            String component = javaVersion.get("component").asText();
            int majorVersion = javaVersion.get("majorVersion").asInt();
            info.append("Java Version: ").append(component).append(" (Java ").append(majorVersion).append(")\n");
        }

        return info.toString();
    }

    /**
     * Inner class to represent a download task
     */
    private static class DownloadTask {
        final String url;
        final String outputPath;
        final String name;

        DownloadTask(String url, String outputPath, String name) {
            this.url = url;
            this.outputPath = outputPath;
            this.name = name;
        }
    }


}