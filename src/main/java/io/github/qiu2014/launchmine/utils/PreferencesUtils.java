package io.github.qiu2014.launchmine.utils;

import io.github.qiu2014.launchmine.Main;

import javax.swing.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;

public class PreferencesUtils {
    private Main main;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode rootNode = objectMapper.createObjectNode();
    private ObjectNode minecraftNode = objectMapper.createObjectNode();

    private int memory;
    private int minecraftWidth;
    private int minecraftHeight;
    private boolean autoUpdateCheck;
    private String javaPath;

    /**
     * Load the settings
     * @throws IOException
     */
    public PreferencesUtils() throws IOException {
        File file = new File("options.json");
        if (!file.exists()) {
            loadDefaultSettings();
        }
        loadSettings();
    }

    private void loadDefaultSettings() throws IOException {
        rootNode.put("memory", 2048);
        rootNode.put("autoUpdateCheck", true);
        rootNode.put("width", 854);
        rootNode.put("height", 480);
        rootNode.put("javaPath", System.getProperty("java.home"));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("options.json"), rootNode);
    }

    /**
     *
     * @param memory The selection of memorySpinner
     * @param width The value of widthField
     * @param height The value of heightField
     * @param autoUpdateCheck Whether if we check updates automatically or not
     * @param javaPath The path of java executable
     * @throws IOException
     */
    public void savePreferences(int memory, int width,
                                 int height, boolean autoUpdateCheck,
                                 String javaPath) throws IOException {
        rootNode.put("memory", memory);
        rootNode.put("autoUpdateCheck", autoUpdateCheck);
        rootNode.put("width", width);
        rootNode.put("height", height);
        rootNode.put("javaPath", javaPath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("options.json"), rootNode);
        JOptionPane.showMessageDialog(main, "Preferences saved!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSettings() throws IOException {
        JsonNode jsonNode = objectMapper.readTree(new File("options.json"));
        this.memory = jsonNode.get("memory").asInt();
        this.minecraftWidth = jsonNode.get("width").asInt();
        this.minecraftHeight = jsonNode.get("height").asInt();
        this.autoUpdateCheck = jsonNode.get("autoUpdateCheck").asBoolean();
        this.javaPath = jsonNode.get("javaPath").asText();
    }

    public int getMemory() {
        return memory;
    }

    public int getMinecraftWidth() {
        return minecraftWidth;
    }

    public int getMinecraftHeight() {
        return minecraftHeight;
    }

    public boolean getIsAutoUpdateCheck() {
        return autoUpdateCheck;
    }

    public String getJavaPath() {
        return javaPath;
    }
}
