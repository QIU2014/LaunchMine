package com.eric;

import javax.swing.*;
import java.awt.*;

public class Instances extends JFrame {
    private final Main main;
    private final UI ui;
    public Instances(Main main, UI ui) {
        this.main = main;
        this.ui = ui;

        setTitle("Instances");
        setSize(1000,550);
        setLocationRelativeTo(null);
        setResizable(false);

        Container container = getContentPane();
        container.setLayout(new BorderLayout(20, 0));

        JPanel upperButtonPanel = ui.createInstancesUpperButtonPanel();
        JSeparator separator = new JSeparator();
        JPanel middlePanel = ui.createInstancesMiddlePanel();
        container.add(upperButtonPanel, BorderLayout.NORTH);
        container.add(middlePanel, BorderLayout.EAST);

    }

    public void visible() {
        setVisible(true);
    }
}
