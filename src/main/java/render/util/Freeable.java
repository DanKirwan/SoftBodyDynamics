package render.util;

import java.io.Closeable;

/**
 * An interface for classes containing a <tt>free()</tt> method.
 */
public interface Freeable extends Closeable {
    void free();

    default void close() {
        free();
    }
}
