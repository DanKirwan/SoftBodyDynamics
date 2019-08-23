package render.objLoader;

import java.io.IOException;

public class ObjFormatException extends IOException {
    public ObjFormatException(int lineNo, String message) {
        super("Line " + lineNo + ": " + message);
    }
}
