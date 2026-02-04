package io.github.qiu2014.launchmine.utils;

import io.github.qiu2014.launchmine.Main;
import io.github.qiu2014.launchmine.ui.DownloadDialog;
import io.github.qiu2014.launchmine.ui.MinecraftLogWindow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LaunchUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static DownloadDialog downloadDialog = null;
    private static MinecraftLogWindow logWindow = null;
    private static JFrame parentFrame = null;
    private static Logger launchUtilsLogger = LogManager.getLogger();

    // 设置父窗口（在主UI中调用）
    public static void setParentFrame(JFrame frame) {
        parentFrame = frame;
    }

    // 获取或创建下载对话框
    private static DownloadDialog getDownloadDialog() {
        if (downloadDialog == null && parentFrame != null) {
            downloadDialog = new DownloadDialog(parentFrame);
        }
        return downloadDialog;
    }

    // 获取或创建日志窗口
    private static MinecraftLogWindow getLogWindow() {
        if (logWindow == null) {
            logWindow = new MinecraftLogWindow();
        }
        return logWindow;
    }

    /**
     * Launch a Minecraft instance
     */
    public static Process launchMinecraft(String versionId, String playerName,
                                          int memoryMB, int width, int height) throws Exception {

        String versionJsonPath = String.format("./.minecraft/versions/%s/%s.json", versionId, versionId);
        File versionJsonFile = new File(versionJsonPath);

        if (!versionJsonFile.exists()) {
            throw new FileNotFoundException("Version JSON not found: " + versionJsonPath);
        }

        launchUtilsLogger.info("=== STARTING MINECRAFT LAUNCH ===");
        launchUtilsLogger.info("Version: " + versionId);
        launchUtilsLogger.info("Player: " + playerName);
        launchUtilsLogger.info("Memory: " + memoryMB + "MB");
        launchUtilsLogger.info("Resolution: " + width + "x" + height);

        // 显示下载对话框
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.reset();
                dialog.setVisible(true);
            }
        });

        // Parse version JSON
        JsonNode versionJson = objectMapper.readTree(versionJsonFile);

        // Log version details
        launchUtilsLogger.info("Main Class: {}", versionJson.get("mainClass").asText());
        launchUtilsLogger.info("Assets: {}", versionJson.get("assets").asText());
        launchUtilsLogger.info("Type: {}", versionJson.get("type").asText());

        // Ensure all files are downloaded and valid
        validateAndDownloadFiles(versionJson, versionId);

        // 关闭下载对话框
        SwingUtilities.invokeLater(() -> {
            if (downloadDialog != null && downloadDialog.isVisible()) {
                downloadDialog.setVisible(false);
            }
        });

        // Build launch command
        List<String> command = buildLaunchCommand(versionJson, versionId,
                playerName, memoryMB, width, height);

        // Log the full command
        launchUtilsLogger.info("=== LAUNCH COMMAND ===");
        for (int i = 0; i < command.size(); i++) {
            // System.out.printf("%3d: %s\n", i, command.get(i));
            launchUtilsLogger.info("{}: {}\n", i, command.get(i));
        }
        launchUtilsLogger.info("======================");

        // Check if Java exists
        String javaPath = command.getFirst();
        File javaFile = new File(javaPath);
        if (!javaFile.exists()) {
            throw new FileNotFoundException("Java executable not found: " + javaPath);
        }
        launchUtilsLogger.info("Using Java: {}", javaPath);

        // Check classpath
        String classpath = "";
        for (String arg : command) {
            if (arg.startsWith("-cp") || arg.equals("${classpath}")) {
                classpath = buildClasspath(versionJson, versionId);
                break;
            }
        }
        launchUtilsLogger.info("Classpath length: {} chars", classpath.length());
        launchUtilsLogger.info("Classpath (first 500 chars): {}...", classpath.substring(0, Math.min(500, classpath.length())));

        // Create natives directory
        String nativesDir = String.format("./.minecraft/versions/%s/natives", versionId);
        new File(nativesDir).mkdirs();
        launchUtilsLogger.info("Natives directory: {}", nativesDir);

        // Extract native libraries if needed
        extractNativeLibraries(versionJson, versionId);

        // 显示Minecraft日志窗口
        SwingUtilities.invokeLater(() -> {
            MinecraftLogWindow window = getLogWindow();
            window.clearLog();
            window.showWindow();
            window.appendLog("=== 开始启动 Minecraft ===");
            window.appendLog("版本: " + versionId);
            window.appendLog("玩家: " + playerName);
            window.appendLog("内存: " + memoryMB + "MB");
            window.appendLog("分辨率: " + width + "x" + height);
            window.appendLog("");
        });

        // Start the process
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File("."));

        // Set environment variables
        Map<String, String> env = processBuilder.environment();
        env.put("game_directory", "./.minecraft");
        env.put("assets_root", "./.minecraft/assets");

        // Redirect output
        processBuilder.redirectErrorStream(true);

        launchUtilsLogger.info("Starting Minecraft process...");

        // 在日志窗口添加启动信息
        SwingUtilities.invokeLater(() -> {
            getLogWindow().appendLog("启动Minecraft进程...");
        });

        Process process = processBuilder.start();

        // Start a thread to read output and send to log window
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[MC] " + line);
                    final String logLine = line;
                    SwingUtilities.invokeLater(() -> {
                        MinecraftLogWindow window = getLogWindow();
                        if (window != null && window.isWindowVisible()) {
                            window.appendLog(logLine);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    MinecraftLogWindow window = getLogWindow();
                    if (window != null && window.isWindowVisible()) {
                        window.appendLog("读取输出时出错: " + e.getMessage());
                    }
                });
            }

            // 进程结束后，在日志窗口添加退出信息
            SwingUtilities.invokeLater(() -> {
                MinecraftLogWindow window = getLogWindow();
                if (window != null && window.isWindowVisible()) {
                    try {
                        int exitCode = process.exitValue();
                        window.appendLog("\n=== Minecraft 已退出 ===");
                        window.appendLog("退出代码: " + exitCode);
                    } catch (IllegalThreadStateException e) {
                        // 进程仍在运行
                    }
                }
            });
        }).start();

        // Wait a bit to see if process starts
        boolean started = process.waitFor(3, TimeUnit.SECONDS);
        if (started && process.exitValue() != 0) {
            int exitCode = process.exitValue();
            launchUtilsLogger.error("Process exited immediately with code: {}", exitCode);

            // 在日志窗口显示错误
            SwingUtilities.invokeLater(() -> {
                MinecraftLogWindow window = getLogWindow();
                if (window != null && window.isWindowVisible()) {
                    window.appendLog("\n!!! Minecraft 启动失败 !!!");
                    window.appendLog("退出代码: " + exitCode);
                }
            });

            throw new IOException("Minecraft failed to start (exit code: " + exitCode + ")");
        }

        launchUtilsLogger.info("Minecraft process started successfully");

        // 在日志窗口显示成功信息
        SwingUtilities.invokeLater(() -> {
            MinecraftLogWindow window = getLogWindow();
            if (window != null && window.isWindowVisible()) {
                window.appendLog("Minecraft进程启动成功");
            }
        });

        return process;
    }

    /**
     * Validate and download all required files with progress dialog
     */
    private static void validateAndDownloadFiles(JsonNode versionJson, String versionId) throws Exception {
        System.out.println("Validating files for version: " + versionId);

        // 在下载对话框显示验证信息
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.addLog("验证版本文件: " + versionId);
            }
        });

        // Download client JAR
        if (versionJson.has("downloads") && versionJson.get("downloads").has("client")) {
            JsonNode client = versionJson.get("downloads").get("client");
            String url = client.get("url").asText();
            String sha1 = client.get("sha1").asText();
            long size = client.get("size").asLong();

            String jarPath = String.format("./.minecraft/versions/%s/%s.jar", versionId, versionId);
            validateOrDownloadFile(url, jarPath, sha1, size, "Client JAR");
        }

        // Download libraries
        if (versionJson.has("libraries")) {
            JsonNode libraries = versionJson.get("libraries");
            int totalLibraries = 0;
            int currentLibrary = 0;

            // 先计算需要下载的库数量
            for (JsonNode library : libraries) {
                if (library.has("rules") && !shouldIncludeLibrary(library.get("rules"))) {
                    continue;
                }
                if (library.has("downloads") && library.get("downloads").has("artifact")) {
                    totalLibraries++;
                }
            }

            // 下载库文件
            for (JsonNode library :libraries) {
                // Check rules
                if (library.has("rules") && !shouldIncludeLibrary(library.get("rules"))) {
                    continue;
                }
                if (library.has("downloads") && library.get("downloads").has("artifact")) {
                    currentLibrary++;
                    JsonNode artifact = library.get("downloads").get("artifact");
                    String url = artifact.get("url").asText();
                    String path = artifact.get("path").asText();
                    String sha1 = artifact.get("sha1").asText();
                    long size = artifact.get("size").asLong();

                    String filePath = "./.minecraft/libraries/" + path;
                    validateOrDownloadFile(url, filePath, sha1, size,
                            String.format("Library (%d/%d): %s", currentLibrary, totalLibraries, library.get("name").asText()));
                    }

                // Handle natives
                if (library.has("downloads") && library.get("downloads").has("classifiers")) {
                    JsonNode classifiers = library.get("downloads").get("classifiers");

                    // Find the appropriate classifier for current OS
                    String classifier = getNativeClassifier();
                    if (classifier != null && classifiers.has(classifier)) {
                        JsonNode nativeArtifact = classifiers.get(classifier);
                        String url = nativeArtifact.get("url").asText();
                        String path = nativeArtifact.get("path").asText();
                        String sha1 = nativeArtifact.get("sha1").asText();
                        long size = nativeArtifact.get("size").asLong();

                        String filePath = "./.minecraft/libraries/" + path;
                        validateOrDownloadFile(url, filePath, sha1, size, "Native: " + library.get("name").asText());
                    }
                }
            }
        }

        // Download assets
        if (versionJson.has("assetIndex")) {
            JsonNode assetIndex = versionJson.get("assetIndex");
            String assetIndexId = versionJson.get("assets").asText();
            String url = assetIndex.get("url").asText();
            String sha1 = assetIndex.get("sha1").asText();
            long size = assetIndex.get("size").asLong();

            String assetIndexPath = "./.minecraft/assets/indexes/" + assetIndexId + ".json";
            validateOrDownloadFile(url, assetIndexPath, sha1, size, "Asset Index");

            // Parse asset index and download assets
            downloadAssets(assetIndexPath);
        }

        launchUtilsLogger.info("All files validated and ready!");

        // 在下载对话框显示完成信息
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.addLog("所有文件验证完成，准备启动Minecraft...");
            }
        });
    }

    /**
     * Download assets from asset index with progress
     */
    private static void downloadAssets(String assetIndexPath) throws Exception {
        File assetIndexFile = new File(assetIndexPath);
        if (!assetIndexFile.exists()) {
            return;
        }

        JsonNode assetIndex = objectMapper.readTree(assetIndexFile);
        if (!assetIndex.has("objects")) {
            return;
        }

        JsonNode objects = assetIndex.get("objects");
        int totalAssets = objects.size();
        int downloaded = 0;
        int skipped = 0;

        launchUtilsLogger.info("Checking {} assets...", totalAssets);

        // 在下载对话框显示资产检查信息
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.addLog("检查 " + totalAssets + " 个游戏资源...");
            }
        });

        int currentAsset = 0;
        for (String key : (Iterable<String>) () -> objects.fieldNames()) {
            currentAsset++;
            JsonNode asset = objects.get(key);
            String hash = asset.get("hash").asText();
            long size = asset.get("size").asLong();

            // Asset URL format: first 2 chars of hash as directory, then full hash
            String url = "https://resources.download.minecraft.net/" +
                    hash.substring(0, 2) + "/" + hash;
            String filePath = "./.minecraft/assets/objects/" + hash.substring(0, 2) + "/" + hash;

            if (!isFileValid(filePath, hash, size)) {
                launchUtilsLogger.info("Downloading asset: {} ({})", key, hash.substring(0, 8));

                // 在下载对话框显示当前下载的资产
                final String assetName = key;
                int finalCurrentAsset = currentAsset;
                SwingUtilities.invokeLater(() -> {
                    DownloadDialog dialog = getDownloadDialog();
                    if (dialog != null) {
                        dialog.setCurrentFile(String.format("资源 (%d/%d): %s",
                                finalCurrentAsset, totalAssets,
                                assetName.substring(Math.max(0, assetName.length() - 30))));
                    }
                });

                // Create directory if it doesn't exist
                File assetFile = new File(filePath);
                assetFile.getParentFile().mkdirs();

                try {
                    // 使用带进度回调的下载方法
                    NetUtils.downloadArtifact(url, filePath, new NetUtils.DownloadProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            int progress = (int) ((downloaded * 100) / total);
                            SwingUtilities.invokeLater(() -> {
                                DownloadDialog dialog = getDownloadDialog();
                                if (dialog != null) {
                                    dialog.setProgress(progress, downloaded, total);
                                }
                            });
                        }

                        @Override
                        public void onFileChanged(String fileName) {
                            // 不需要在这里处理，因为我们在上面已经设置了
                        }
                    });

                    downloaded++;

                    // Verify download
                    if (!isFileValid(filePath, hash, size)) {
                        launchUtilsLogger.error("Warning: Downloaded asset failed validation: {}", key);
                        assetFile.delete(); // Delete corrupted file
                    }
                } catch (Exception e) {
                    launchUtilsLogger.error("Failed to download asset {}: {}", key, e.getMessage());
                }
            } else {
                skipped++;
            }
        }

        if (downloaded > 0) {
            launchUtilsLogger.info("Downloaded {} new assets", downloaded);
        }
        if (skipped > 0) {
            launchUtilsLogger.info("Skipped {} existing assets", skipped);
        }
    }

    /**
     * Validate or download a single file with progress dialog
     */
    private static void validateOrDownloadFile(String url, String filePath,
                                               String expectedSha1, long expectedSize, String description) throws Exception {

        File file = new File(filePath);

        if (isFileValid(filePath, expectedSha1, expectedSize)) {
            launchUtilsLogger.info("✓ {} is valid", description);

            // 在下载对话框显示验证信息
            SwingUtilities.invokeLater(() -> {
                DownloadDialog dialog = getDownloadDialog();
                if (dialog != null) {
                    dialog.addLog("✓ " + description + " 已验证");
                }
            });
            return;
        }

        launchUtilsLogger.info("Downloading {}...", description);

        // 在下载对话框显示下载信息
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.setCurrentFile(description);
                dialog.addLog("开始下载: " + description);
            }
        });

        // Create parent directory
        file.getParentFile().mkdirs();

        // Download the file with progress callback
        NetUtils.downloadArtifact(url, filePath, new NetUtils.DownloadProgressCallback() {
            @Override
            public void onProgress(long downloaded, long total) {
                int progress = (int) ((downloaded * 100) / total);
                SwingUtilities.invokeLater(() -> {
                    DownloadDialog dialog = getDownloadDialog();
                    if (dialog != null) {
                        dialog.setProgress(progress, downloaded, total);
                    }
                });
            }

            @Override
            public void onFileChanged(String fileName) {
                // 不需要处理，因为我们已经设置了description
            }
        });

        // Verify the downloaded file
        if (!isFileValid(filePath, expectedSha1, expectedSize)) {
            throw new IOException("Downloaded file failed validation: " + description);
        }

        launchUtilsLogger.info("✓ {} downloaded successfully", description);

        // 在下载对话框显示完成信息
        SwingUtilities.invokeLater(() -> {
            DownloadDialog dialog = getDownloadDialog();
            if (dialog != null) {
                dialog.addLog("✓ " + description + " 下载完成");
            }
        });
    }

    private static void extractNativeLibraries(JsonNode versionJson, String versionId) throws Exception {
        String nativesDir = String.format("./.minecraft/versions/%s/natives", versionId);
        File nativesDirectory = new File(nativesDir);
        nativesDirectory.mkdirs();

        // Clear old natives
        File[] oldFiles = nativesDirectory.listFiles();
        if (oldFiles != null) {
            for (File file : oldFiles) {
                file.delete();
            }
        }

        if (!versionJson.has("libraries")) {
            return;
        }

        JsonNode libraries = versionJson.get("libraries");
        for (JsonNode library : libraries) {
            // Check rules
            if (library.has("rules") && !shouldIncludeLibrary(library.get("rules"))) {
                continue;
            }

            // Check for natives
            if (library.has("natives")) {
                JsonNode natives = library.get("natives");
                String osKey = getOSKey();

                if (natives.has(osKey) && library.has("downloads") &&
                        library.get("downloads").has("classifiers")) {

                    String nativeClassifier = natives.get(osKey).asText().replace("${arch}", getArch());
                    JsonNode classifiers = library.get("downloads").get("classifiers");

                    if (classifiers.has(nativeClassifier)) {
                        JsonNode nativeArtifact = classifiers.get(nativeClassifier);
                        String path = nativeArtifact.get("path").asText();
                        String nativePath = "./.minecraft/libraries/" + path;

                        File nativeFile = new File(nativePath);
                        if (nativeFile.exists()) {
                            launchUtilsLogger.info("Extracting native: {}", nativeFile.getName());
                            extractJar(nativeFile, nativesDirectory);
                        }
                    }
                }
            }
        }
    }

    private static void extractJar(File jarFile, File outputDir) throws Exception {
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                File outputFile = new File(outputDir, entry.getName());
                outputFile.getParentFile().mkdirs();

                try (java.io.InputStream is = jar.getInputStream(entry);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    // Helper method to get OS key for natives
    private static String getOSKey() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac")) {
            return "osx";
        } else if (os.contains("nix") || os.contains("nux")) {
            return "linux";
        }
        return "unknown";
    }

    // Helper method to get architecture
    private static String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("64")) {
            return "64";
        } else if (arch.contains("86")) {
            return "32";
        }
        return arch;
    }


    /**
     * Check if a file exists and has correct SHA1 hash
     */
    private static boolean isFileValid(String filePath, String expectedSha1, long expectedSize) {
        File file = new File(filePath);

        if (!file.exists()) {
            return false;
        }

        // Check file size first (faster than SHA1 calculation)
        long actualSize = file.length();
        if (actualSize != expectedSize) {
            launchUtilsLogger.info("File size mismatch: {} (expected: {}, got: {})", filePath, expectedSize, actualSize);
            return false;
        }

        // Only calculate SHA1 for small to medium files, or if size matches
        // For very large files, we might skip SHA1 check for performance
        if (expectedSize > 100 * 1024 * 1024) { // > 100MB
            launchUtilsLogger.info("Skipping SHA1 check for large file: {}", filePath);
            return true; // Trust the size check for large files
        }

        try {
            String actualSha1 = calculateSHA1(file);
            if (!actualSha1.equalsIgnoreCase(expectedSha1)) {
                launchUtilsLogger.warn("SHA1 mismatch for: {} (expected: {}, got: {})", filePath, expectedSha1, actualSha1);
                return false;
            }
            return true;
        } catch (Exception e) {
            launchUtilsLogger.error("Error calculating SHA1 for {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * Calculate SHA1 hash of a file
     */
    private static String calculateSHA1(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[8192];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
        }

        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Build the launch command
     */
    private static List<String> buildLaunchCommand(JsonNode versionJson, String versionId,
                                                   String playerName, int memoryMB, int width, int height) {

        List<String> command = new ArrayList<>();

        // Java executable (use system Java)
        String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        command.add(javaPath);

        // JVM arguments - memory first
        command.add("-Xmx" + memoryMB + "M");
        command.add("-Xms" + Math.min(1024, memoryMB) + "M"); // Start with 1GB or less

        // Add JVM arguments from version JSON
        if (versionJson.has("arguments") && versionJson.get("arguments").has("jvm")) {
            JsonNode jvmArgs = versionJson.get("arguments").get("jvm");
            for (JsonNode arg : jvmArgs) {
                if (arg.isTextual()) {
                    String argStr = replacePlaceholders(arg.asText(), versionJson, versionId, playerName, width, height);
                    if (!argStr.isEmpty()) {
                        command.add(argStr);
                    }
                } else if (arg.isObject()) {
                    if (shouldIncludeArgument(arg)) {
                        JsonNode value = arg.get("value");
                        if (value.isArray()) {
                            for (JsonNode v : value) {
                                String argStr = replacePlaceholders(v.asText(), versionJson, versionId, playerName, width, height);
                                if (!argStr.isEmpty()) {
                                    command.add(argStr);
                                }
                            }
                        } else {
                            String argStr = replacePlaceholders(value.asText(), versionJson, versionId, playerName, width, height);
                            if (!argStr.isEmpty()) {
                                command.add(argStr);
                            }
                        }
                    }
                }
            }
        } else {
            // Legacy JVM arguments
            String nativesDir = String.format("./.minecraft/versions/%s/natives", versionId);
            command.add("-Djava.library.path=" + nativesDir);
            command.add("-cp");
            command.add(buildClasspath(versionJson, versionId)); // 使用新的buildClasspath方法
        }

        // Main class
        String mainClass = versionJson.get("mainClass").asText();
        command.add(mainClass);

        // Game arguments
        if (versionJson.has("arguments") && versionJson.get("arguments").has("game")) {
            JsonNode gameArgs = versionJson.get("arguments").get("game");
            for (JsonNode arg : gameArgs) {
                if (arg.isTextual()) {
                    String argStr = replacePlaceholders(arg.asText(), versionJson, versionId, playerName, width, height);
                    if (!argStr.isEmpty() && !argStr.equals("--demo") && !argStr.equals("--width") && !argStr.equals("--height")) {
                        command.add(argStr);
                    }
                } else if (arg.isObject()) {
                    if (shouldIncludeArgument(arg)) {
                        JsonNode value = arg.get("value");
                        if (value.isArray()) {
                            for (JsonNode v : value) {
                                String argStr = replacePlaceholders(v.asText(), versionJson, versionId, playerName, width, height);
                                if (!argStr.isEmpty() && !argStr.equals("--demo") && !argStr.equals("--width") && !argStr.equals("--height")) {
                                    command.add(argStr);
                                }
                            }
                        } else {
                            String argStr = replacePlaceholders(value.asText(), versionJson, versionId, playerName, width, height);
                            if (!argStr.isEmpty() && !argStr.equals("--demo") && !argStr.equals("--width") && !argStr.equals("--height")) {
                                command.add(argStr);
                            }
                        }
                    }
                }
            }
        } else {
            // Legacy argument format
            command.add("--username");
            command.add(playerName);
            command.add("--version");
            command.add(versionId);
            command.add("--gameDir");
            command.add("./.minecraft");
            command.add("--assetsDir");
            command.add("./.minecraft/assets");
            command.add("--assetIndex");
            command.add(versionJson.get("assets").asText());
            command.add("--uuid");
            command.add("00000000-0000-0000-0000-000000000000");
            command.add("--accessToken");
            command.add("0");
            command.add("--userType");
            command.add("mojang");
            command.add("--versionType");
            command.add("release");
            if (width > 0 && height > 0) {
                command.add("--width");
                command.add(String.valueOf(width));
                command.add("--height");
                command.add(String.valueOf(height));
            }
        }

        return command;
    }


    /**
     * Replace placeholders in arguments
     */
    private static String replacePlaceholders(String str, JsonNode versionJson, String versionId,
                                              String playerName, int width, int height) {

        String assetsIndex = versionJson.has("assets") ? versionJson.get("assets").asText() : versionId;

        String result = str
                .replace("${auth_player_name}", playerName)
                .replace("${version_name}", versionId)
                .replace("${game_directory}", "./.minecraft")
                .replace("${assets_root}", "./.minecraft/assets")
                .replace("${assets_index_name}", assetsIndex)
                .replace("${auth_uuid}", java.util.UUID.randomUUID().toString())
                .replace("${auth_access_token}", "0")
                .replace("${user_type}", "mojang")
                .replace("${version_type}", "release")
                .replace("${resolution_width}", String.valueOf(width))
                .replace("${resolution_height}", String.valueOf(height))
                .replace("${classpath}", buildClasspath(versionJson, versionId))
                .replace("${natives_directory}", String.format("./.minecraft/versions/%s/natives", versionId))
                .replace("${launcher_name}", "LaunchMine")
                .replace("${launcher_version}", Main.VERSION)
                .replace("${quickPlayMultiplayer}","");


        // Handle any remaining ${variable} patterns
        result = result.replaceAll("\\$\\{[^}]*\\}", "");

        return result;
    }

    /**
     * Build the classpath for launching - 根据version.json中的libraries信息构建
     */
    private static String buildClasspath(JsonNode versionJson, String versionId) {
        StringBuilder classpath = new StringBuilder();

        // Add client JAR
        classpath.append(String.format("./.minecraft/versions/%s/%s.jar", versionId, versionId));

        // Add libraries from version.json
        if (versionJson.has("libraries")) {
            JsonNode libraries = versionJson.get("libraries");
            for (JsonNode library : libraries) {
                // Check rules
                if (library.has("rules") && !shouldIncludeLibrary(library.get("rules"))) {
                    continue;
                }

                if (library.has("downloads") && library.get("downloads").has("artifact")) {
                    JsonNode artifact = library.get("downloads").get("artifact");
                    String path = artifact.get("path").asText();

                    File libFile = new File("./.minecraft/libraries/" + path);
                    if (libFile.exists() && libFile.getName().endsWith(".jar")) {
                        classpath.append(File.pathSeparator).append(libFile.getAbsolutePath());
                    }
                }

                // 处理有分类器的库（如natives）
                if (library.has("downloads") && library.get("downloads").has("classifiers")) {
                    // 对于classpath，我们通常只需要主artifact，不需要natives
                    // natives会在-Djava.library.path中指定
                }
            }
        }

        // 如果从version.json中没有找到足够的库，尝试从libraries目录中添加
        if (classpath.toString().split(File.pathSeparator).length < 10) {
            launchUtilsLogger.warn("Few libraries found in version.json, scanning libraries directory...");
            addFallbackLibraries(classpath);
        }

        return classpath.toString();
    }

    /**
     * 备用方法：从libraries目录添加库文件
     */
    private static void addFallbackLibraries(StringBuilder classpath) {
        File librariesDir = new File("./.minecraft/libraries");
        if (!librariesDir.exists()) {
            return;
        }

        int addedCount = 0;
        // 递归查找所有jar文件
        addedCount = findAndAddJars(librariesDir, classpath);
        launchUtilsLogger.info("Added {} libraries from fallback scan", addedCount);
    }

    /**
     * 递归查找并添加jar文件
     */
    private static int findAndAddJars(File dir, StringBuilder classpath) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;

        for (File file : files) {
            if (file.isDirectory()) {
                count += findAndAddJars(file, classpath);
            } else if (file.getName().endsWith(".jar")) {
                // 跳过native库（通常包含"natives-"在文件名中）
                if (!file.getName().contains("natives-")) {
                    classpath.append(File.pathSeparator).append(file.getAbsolutePath());
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get native classifier for current OS
     */
    private static String getNativeClassifier() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        if (os.contains("win")) {
            if (arch.contains("64")) {
                return "natives-windows";
            } else {
                return "natives-windows-32";
            }
        } else if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                return "natives-macos-arm64";
            } else {
                return "natives-macos";
            }
        } else if (os.contains("nix") || os.contains("nux")) {
            if (arch.contains("64")) {
                return "natives-linux";
            } else {
                return "natives-linux-32";
            }
        }

        return null;
    }

    /**
     * Check if library should be included based on rules
     */
    private static boolean shouldIncludeLibrary(JsonNode rules) {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        for (JsonNode rule : rules) {
            String action = rule.get("action").asText();

            if (rule.has("os")) {
                JsonNode osRule = rule.get("os");
                boolean matches = true;

                if (osRule.has("name")) {
                    String requiredOs = osRule.get("name").asText().toLowerCase();
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
            }
        }

        return true;
    }

    /**
     * Check if argument should be included
     */
    private static boolean shouldIncludeArgument(JsonNode arg) {
        if (!arg.has("rules")) {
            return true;
        }

        JsonNode rules = arg.get("rules");
        return shouldIncludeLibrary(rules);
    }

    /**
     * Validate all files for a version on startup
     */
    public static void validateVersionOnStartup(String versionId) {
        try {
            String versionJsonPath = String.format("./.minecraft/versions/%s/%s.json", versionId, versionId);
            File versionJsonFile = new File(versionJsonPath);

            if (!versionJsonFile.exists()) {
                launchUtilsLogger.info("Version {} not found, skipping validation", versionId);
                return;
            }

            JsonNode versionJson = objectMapper.readTree(versionJsonFile);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        validateAndDownloadFiles(versionJson, versionId);
                    } catch (Exception e) {
                        launchUtilsLogger.error(e.getMessage());
                    }
                }
            }).start();


        } catch (Exception e) {
            launchUtilsLogger.error("Error validating version {}: {}", versionId, e.getMessage());
        }
    }

    /**
     * Validate all installed versions on startup
     */
    public static void validateAllVersionsOnStartup() {
        File versionsDir = new File("./.minecraft/versions");
        if (!versionsDir.exists() || !versionsDir.isDirectory()) {
            return;
        }

        File[] versionFolders = versionsDir.listFiles(File::isDirectory);
        if (versionFolders == null) {
            return;
        }

        launchUtilsLogger.info("Validating all installed versions...");

        for (File versionFolder : versionFolders) {
            String versionId = versionFolder.getName();
            File versionJson = new File(versionFolder, versionId + ".json");

            if (versionJson.exists()) {
                launchUtilsLogger.info("Validating: " + versionId);
                validateVersionOnStartup(versionId);
            }
        }

        launchUtilsLogger.info("Version validation complete!");
    }
}