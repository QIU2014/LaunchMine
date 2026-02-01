package com.eric.utils;

import com.eric.Main;

import javax.swing.*;

public class PreferencesUtils {
    private Main main;
    public void savePreferences(JSpinner memorySpinner, JTextField widthField,
                                 JTextField heightField, JCheckBox autoUpdateCheck,
                                 JTextField javaPathField) {
        JOptionPane.showMessageDialog(main, "Preferences saved!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
