package io.github.qiu2014.launchmine;

import javax.swing.*;
import java.awt.*;

public class Instances extends JDialog {
    private final UI ui;
    public Instances(JFrame parentFrame, UI ui) {
        this.ui = ui;

        super(parentFrame, "Instances", false);

        setType(Type.UTILITY);
        setTitle("Instances");
        setSize(1000,550);
        setLocationRelativeTo(parentFrame);
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
