package util;

import java.io.*;
import java.util.function.Supplier;

public class GlFileDebugStream implements Supplier<PrintStream> {
    @Override
    public PrintStream get() {

        try {
            return new PrintStream(new FileOutputStream(new File("gl_debug_log.txt")));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
}
