package com.eric.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String VERSION_MANIFEST_FILE = "version_manifest_v2.json";

    // Cache for version list
    private static List<VersionInfo> versionsCache = null;

    /**
     * Download or update the version manifest from Mojang's server
     */
    public static void updateVersionManifest() {
        System.out.println("Downloading version manifest from: " + VERSION_MANIFEST_URL);
        try {
            NetUtils.downloadArtifact(VERSION_MANIFEST_URL, VERSION_MANIFEST_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Version manifest saved to: " + VERSION_MANIFEST_FILE);

        // Clear cache so it will be reloaded
        versionsCache = null;
    }

    /**
     * Get all available versions
     */
    public static List<VersionInfo> getVersions() throws IOException {
        if (versionsCache == null) {
            loadVersionsFromManifest();
        }
        return new ArrayList<>(versionsCache);
    }

    /**
     * Get versions by type (release, snapshot, etc.)
     */
    public static List<VersionInfo> getVersionsByType(String type) throws IOException {
        List<VersionInfo> filtered = new ArrayList<>();
        for (VersionInfo version : getVersions()) {
            if (version.type.equalsIgnoreCase(type)) {
                filtered.add(version);
            }
        }
        return filtered;
    }

    /**
     * Get a specific version by ID
     */
    public static VersionInfo getVersion(String versionId) throws IOException {
        for (VersionInfo version : getVersions()) {
            if (version.id.equals(versionId)) {
                return version;
            }
        }
        return null;
    }

    /**
     * Download the JSON file for a specific version
     */
    public static void downloadVersionJson(VersionInfo version) throws IOException {
        String versionId = version.id;
        String url = version.url;

        // Create version directory
        String versionDir = String.format("./.minecraft/versions/%s", versionId);
        String filePath = versionDir + "/" + versionId + ".json";

        File dir = new File(versionDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("Downloading version JSON for " + versionId + "...");
        try {
            NetUtils.downloadArtifact(url, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Version JSON saved to: " + filePath);
    }

    /**
     * Download the JSON file for a specific version by ID
     */
    public static void downloadVersionJson(String versionId) throws IOException {
        VersionInfo version = getVersion(versionId);
        if (version == null) {
            throw new IOException("Version not found: " + versionId);
        }
        downloadVersionJson(version);
    }

    /**
     * Process a selected version - download its JSON and optionally its assets
     */
    public static void processSelectedVersion(VersionInfo version, boolean downloadAssets) throws IOException {
        System.out.println("Processing version: " + version.id + " (" + version.type + ")");

        // 1. Download version JSON
        downloadVersionJson(version);

        // 2. Parse the downloaded version JSON to get more info
        String versionJsonPath = String.format("./.minecraft/versions/%s/%s.json", version.id, version.id);
        File versionJsonFile = new File(versionJsonPath);

        if (!versionJsonFile.exists()) {
            throw new IOException("Version JSON not found: " + versionJsonPath);
        }

        JsonNode versionDetails = objectMapper.readTree(versionJsonFile);

        // 3. Download client JAR if it exists
        if (versionDetails.has("downloads") && versionDetails.get("downloads").has("client")) {
            JsonNode client = versionDetails.get("downloads").get("client");
            String clientUrl = client.get("url").asText();
            String clientPath = String.format("./.minecraft/versions/%s/%s.jar", version.id, version.id);

            System.out.println("Downloading client JAR...");
            try {
                NetUtils.downloadArtifact(clientUrl, clientPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Client JAR saved to: " + clientPath);
        }

        // 4. Download assets if requested
        if (downloadAssets) {
            downloadAssetsForVersion(versionDetails, version.id);
        }

        System.out.println("Version " + version.id + " processing completed!");
    }

    /**
     * Download assets for a version
     */
    private static void downloadAssetsForVersion(JsonNode versionDetails, String versionId) throws IOException {
        if (versionDetails.has("assetIndex")) {
            JsonNode assetIndex = versionDetails.get("assetIndex");
            String assetIndexUrl = assetIndex.get("url").asText();
            String assetIndexId = versionDetails.get("assets").asText();

            // Create assets directories
            String indexesDir = "./.minecraft/assets/indexes";
            String objectsDir = "./.minecraft/assets/objects";
            new File(indexesDir).mkdirs();
            new File(objectsDir).mkdirs();

            // Download asset index
            String assetIndexPath = indexesDir + "/" + assetIndexId + ".json";
            System.out.println("Downloading asset index...");
            try {
                NetUtils.downloadArtifact(assetIndexUrl, assetIndexPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Note: Asset index downloaded. To download all assets,");
            System.out.println("      you would need to parse the index and download each file.");
        }
    }

    /**
     * Load versions from the manifest file
     */
    private static void loadVersionsFromManifest() throws IOException {
        File manifestFile = new File(VERSION_MANIFEST_FILE);

        // If manifest doesn't exist, download it
        if (!manifestFile.exists()) {
            System.out.println("Version manifest not found. Downloading...");
            updateVersionManifest();
        }

        // Read the manifest
        JsonNode manifest = objectMapper.readTree(manifestFile);
        JsonNode versionsArray = manifest.get("versions");

        versionsCache = new ArrayList<>();

        for (JsonNode versionNode : versionsArray) {
            VersionInfo info = new VersionInfo(
                    versionNode.get("id").asText(),
                    versionNode.get("type").asText(),
                    versionNode.get("url").asText(),
                    versionNode.get("time").asText(),
                    versionNode.get("releaseTime").asText()
            );
            versionsCache.add(info);
        }

        System.out.println("Loaded " + versionsCache.size() + " versions from manifest");
    }

    /**
     * Get the latest release version
     */
    public static VersionInfo getLatestRelease() throws IOException {
        File manifestFile = new File(VERSION_MANIFEST_FILE);
        if (!manifestFile.exists()) {
            updateVersionManifest();
        }

        JsonNode manifest = objectMapper.readTree(manifestFile);
        JsonNode latest = manifest.get("latest");
        String latestReleaseId = latest.get("release").asText();

        return getVersion(latestReleaseId);
    }

    /**
     * Get the latest snapshot version
     */
    public static VersionInfo getLatestSnapshot() throws IOException {
        File manifestFile = new File(VERSION_MANIFEST_FILE);
        if (!manifestFile.exists()) {
            updateVersionManifest();
        }

        JsonNode manifest = objectMapper.readTree(manifestFile);
        JsonNode latest = manifest.get("latest");
        String latestSnapshotId = latest.get("snapshot").asText();

        return getVersion(latestSnapshotId);
    }

    /**
     * Check if a version is already downloaded
     */
    public static boolean isVersionDownloaded(String versionId) {
        String versionJsonPath = String.format("./.minecraft/versions/%s/%s.json", versionId, versionId);
        String clientJarPath = String.format("./.minecraft/versions/%s/%s.jar", versionId, versionId);

        File jsonFile = new File(versionJsonPath);
        File jarFile = new File(clientJarPath);

        return jsonFile.exists() && jarFile.exists();
    }

    /**
     * Get the download progress of a version
     */
    public static String getVersionDownloadStatus(String versionId) {
        String versionJsonPath = String.format("./.minecraft/versions/%s/%s.json", versionId, versionId);
        String clientJarPath = String.format("./.minecraft/versions/%s/%s.jar", versionId, versionId);

        File jsonFile = new File(versionJsonPath);
        File jarFile = new File(clientJarPath);

        if (jsonFile.exists() && jarFile.exists()) {
            return "Fully downloaded";
        } else if (jsonFile.exists()) {
            return "JSON only (missing JAR)";
        } else if (jarFile.exists()) {
            return "JAR only (missing JSON)";
        } else {
            return "Not downloaded";
        }
    }

    /**
     * Inner class to hold version information
     */
    public static class VersionInfo {
        public final String id;
        public final String type;
        public final String url;
        public final String time;
        public final String releaseTime;

        public VersionInfo(String id, String type, String url, String time, String releaseTime) {
            this.id = id;
            this.type = type;
            this.url = url;
            this.time = time;
            this.releaseTime = releaseTime;
        }

        @Override
        public String toString() {
            return id + " (" + type + ") - Released: " + releaseTime;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getReleaseTime() {
            return releaseTime;
        }
    }
}