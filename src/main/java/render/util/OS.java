package render.util;

import java.util.Locale;

public enum OS {
    WINDOWS,
    LINUX,
    MACOS,
    OTHER;

    public static final OS CURRENT;
    static {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.contains("win") && !os.contains("darwin")) {
            CURRENT = WINDOWS;
        } else if (os.contains("mac") || os.contains("osx") || os.contains("os x")) {
            CURRENT = MACOS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            CURRENT = LINUX;
        } else {
            CURRENT = OTHER;
        }
    }
}
