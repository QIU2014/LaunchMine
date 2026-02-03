/*
* (C) Copyright qiuerichanru 2026 - All Rights Reserved
*
* This notice and attribution to qiuerichanru may not be removed.
 */
package io.github.qiu2014.launchmine.utils;

/**
* Signals ".minecraft" folder is missing
* @see Exception
* @author Qiu Hanru
 */

public class MissingMinecraftFolderException extends RuntimeException {

    /**
    * Constructs a MissingMinecraftFolderException with the specified information.
    * A detail message is a String that describes this particular exception.
    * @param s the detail message
    * @param className the name of the resource class
    * @param folder the folder of the missing folder
     */

    public MissingMinecraftFolderException(String s, String className, String folder) {
        super(s);
        this.className = className;
        this.folder = folder;
    }

    /**
     * Constructs a {@code MissingResourceException} with
     * {@code message}, {@code className}, {@code key},
     * and {@code cause}. This constructor is package private for
     * use by {@code ResourceBundle.getBundle}.
     *
     * @param message
     *        the detail message
     * @param className
     *        the name of the resource class
     * @param folder
     *        the folder of the missing folder
     * @param cause
     *        the cause (which is saved for later retrieval by the
     *        {@link Throwable#getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent
     *        or unknown.)
     */

    public MissingMinecraftFolderException(String message, String className, String folder, Throwable cause) {
        super(message, cause);
        this.className = className;
        this.folder = folder;
    }

    /**
     * Gets parameter passed by constructor.
     *
     * @return the name of the resource class
     */

    public String getClassName() { return className; }

    /**
     * Gets parameter passed by constructor.
     *
     * @return the key for the missing resource
     */

    public String getFolder() { return folder; }

    private String className;
    private String folder;
}
