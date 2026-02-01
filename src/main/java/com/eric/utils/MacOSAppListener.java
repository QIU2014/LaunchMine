package com.eric.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Simplified macOS application menu handler
 */
public class MacOSAppListener {

    private JFrame mainFrame;
    private ActionListener aboutListener;
    private ActionListener preferencesListener;
    private ActionListener quitListener;

    public MacOSAppListener(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void setAboutListener(ActionListener listener) {
        this.aboutListener = listener;
    }

    public void setPreferencesListener(ActionListener listener) {
        this.preferencesListener = listener;
    }

    public void setQuitListener(ActionListener listener) {
        this.quitListener = listener;
    }

    /**
     * Try to register macOS application event handlers
     */
    public void register() {
        if (!isMacOS()) {
            return;
        }

        try {
            // Try Java 9+ Taskbar API first
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.USER_ATTENTION)) {
                    // We have some macOS integration
                    System.out.println("macOS Taskbar API available");
                }
            }

            // Try to use Apple's eawt API via reflection
            setupAppleEAWT();

        } catch (Exception e) {
            System.err.println("macOS integration not available: " + e.getMessage());
        }
    }

    private void setupAppleEAWT() {
        try {
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            Object application = applicationClass.getMethod("getApplication").invoke(null);

            // Try to enable About and Preferences menu items
            try {
                Method setAboutHandler = applicationClass.getMethod("setAboutHandler",
                        Class.forName("com.apple.eawt.AboutHandler"));
                setAboutHandler.invoke(application, createAboutHandler());
            } catch (NoSuchMethodException e) {
                // Old API or not available
            }

            try {
                Method setPreferencesHandler = applicationClass.getMethod("setPreferencesHandler",
                        Class.forName("com.apple.eawt.PreferencesHandler"));
                setPreferencesHandler.invoke(application, createPreferencesHandler());
            } catch (NoSuchMethodException e) {
                // Old API or not available
            }

            try {
                Method setQuitHandler = applicationClass.getMethod("setQuitHandler",
                        Class.forName("com.apple.eawt.QuitHandler"));
                setQuitHandler.invoke(application, createQuitHandler());
            } catch (NoSuchMethodException e) {
                // Old API or not available
            }

            System.out.println("macOS EAWT handlers registered");

        } catch (ClassNotFoundException e) {
            System.out.println("Apple EAWT classes not found (newer Java version)");
        } catch (Exception e) {
            System.err.println("Failed to setup Apple EAWT: " + e.getMessage());
        }
    }

    private Object createAboutHandler() throws ClassNotFoundException {
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { Class.forName("com.apple.eawt.AboutHandler") },
                (proxy, method, args) -> {
                    if (method.getName().equals("handleAbout") && aboutListener != null) {
                        aboutListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "About"));
                    }
                    return null;
                });
    }

    private Object createPreferencesHandler() throws ClassNotFoundException {
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { Class.forName("com.apple.eawt.PreferencesHandler") },
                (proxy, method, args) -> {
                    if (method.getName().equals("handlePreferences") && preferencesListener != null) {
                        preferencesListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Preferences"));
                    }
                    return null;
                });
    }

    private Object createQuitHandler() throws ClassNotFoundException {
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { Class.forName("com.apple.eawt.QuitHandler") },
                (proxy, method, args) -> {
                    if (method.getName().equals("handleQuitRequestWith") && quitListener != null) {
                        quitListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Quit"));
                    }
                    return null;
                });
    }

    private boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}