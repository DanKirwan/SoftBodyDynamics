package render.util;

import java.util.Locale;

public enum JDKType {

    ORACLE,
    OPENJDK;

    public static final JDKType CURRENT;
    static {
        String jdkType = System.getProperty("java.vm.name").toLowerCase(Locale.ENGLISH);
        if (jdkType.contains("openjdk")) {
            CURRENT = OPENJDK;
        } else {
            CURRENT = ORACLE;
        }
    }

}
