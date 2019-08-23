package render.shader;

import render.util.Freeable;
import render.util.VertexFormat;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram implements Freeable {

    private static ShaderProgram boundShader = null;

    private int programId;
    private int vertexShaderId = 0;
    private int fragmentShaderId = 0;
    private VertexFormat vertexFormat;

    // Variables for compilation and descriptive error messages
    private LineNumberMap lineNumberMap;
    private List<String> lines;

    public  ShaderProgram(String vertexShader, String fragmentShader, VertexFormat vertexFormat, String... attribNames) {
        if (vertexFormat.getAttributes().length != attribNames.length) {
            throw new IllegalArgumentException("Wrong number of attribute names for the vertex format");
        }

        this.vertexFormat = vertexFormat;
        programId = glCreateProgram();
        if (programId == 0)
            throw new IllegalStateException("Couldn't create program");

        bindAttribLocations(attribNames);

        if (vertexShader != null)
            vertexShaderId = createShader(readShader(vertexShader), GL_VERTEX_SHADER);
        if (fragmentShader != null)
            fragmentShaderId = createShader(readShader(fragmentShader), GL_FRAGMENT_SHADER);

        link();
        lineNumberMap = null;
        lines = null;
    }




    private void bindAttribLocations(String[] attribNames) {
        for(int i=0; i < attribNames.length;i++) {
            glBindAttribLocation(programId,i,attribNames[i]);
        }

    }

    private String readShader(String shader) {
        StringBuilder str = new StringBuilder();
        LineNumberMap.Builder lnmb = new LineNumberMap.Builder();
        lnmb.addEntry(1, 1, shader);
        lines = new ArrayList<>();
        readShader(shader, str, 1, new HashSet<>(), lnmb);
        lineNumberMap = lnmb.build();
        return str.toString();
    }

    // returns the new global line number
    private int readShader(String shader, StringBuilder str, int globalLine, Set<String> alreadyIncluded, LineNumberMap.Builder lnmb) {
        alreadyIncluded.add(shader);

        InputStream in = ShaderProgram.class.getResourceAsStream("/shaders/" + shader);

        //checks in standard shaders
        if (in == null)
            in = ShaderProgram.class.getResourceAsStream("/shaders/std/" + shader);

        if (in == null)
            throw new IllegalArgumentException("Shader not found: " + shader);

        int localLine = 1;

        try (Scanner scan = new Scanner(in).useDelimiter("\r?\n")) {
            while (scan.hasNext()) {
                String line = scan.next();
                if (line.trim().startsWith("#include ")) {
                    String newShader = line;
                    if (newShader.contains("//"))
                        newShader = newShader.substring(0, newShader.indexOf("//"));
                    newShader = newShader.trim();
                    newShader = newShader.substring("#include ".length());
                    newShader = newShader.trim();

                    if (!alreadyIncluded.contains(newShader)) {
                        lnmb.addEntry(1, globalLine, newShader);
                        globalLine = readShader(newShader, str, globalLine, alreadyIncluded, lnmb);
                        lnmb.addEntry(localLine + 1, globalLine, shader);
                    }
                } else {
                    lines.add(line);
                    str.append(line).append("\n");
                    globalLine++;
                }
                localLine++;
            }
        }

        return globalLine;
    }

    private int createShader(String source, int type) {
        int shaderId = glCreateShader(type);
        if (shaderId == 0)
            throw new IllegalStateException("Couldn't create shader");

        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            int len;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer buf = stack.mallocInt(1);
                glGetShaderiv(shaderId, GL_INFO_LOG_LENGTH, buf);
                len = buf.get(0);
            }
            String log = glGetShaderInfoLog(shaderId, len);
            log = filterLog(log);
            System.err.println(log);
            throw new IllegalStateException("Shader compile error");
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    private void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            int len;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer buf = stack.mallocInt(1);
                glGetProgramiv(programId, GL_INFO_LOG_LENGTH, buf);
                len = buf.get(0);
            }
            String log = glGetProgramInfoLog(programId, len);
            log = filterLog(log);
            System.out.println(log);
            throw new IllegalStateException("Program link error");
        }

        if (vertexShaderId != 0)
            glDetachShader(programId, vertexShaderId);
        if (fragmentShaderId != 0)
            glDetachShader(programId, fragmentShaderId);

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            int len;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer buf = stack.mallocInt(1);
                glGetProgramiv(programId, GL_INFO_LOG_LENGTH, buf);
                len = buf.get(0);
            }
            String log = glGetProgramInfoLog(programId, len);
            log = filterLog(log);
            System.err.println("Warning(s) in shader code:");
            System.err.println(log);
        }
    }

    private String filterLog(String log) {
        Pattern pattern = Pattern.compile("0(?::(\\d+))?\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(log);
        StringBuffer newLog = new StringBuffer();

        while (matcher.find()) {
            String str = matcher.group(1);
            if (str == null)
                str = matcher.group(2);
            int globalLine = Integer.parseInt(str);
            matcher.appendReplacement(newLog, lineNumberMap.getActualFileName(globalLine) + ":" + lineNumberMap.getActualLineNumber(globalLine));
            String toInsert = lines.get(globalLine - 1);
            toInsert += "\n" + toInsert.replaceAll("\\S", "~");
            int newlineIndex = newLog.lastIndexOf("\n");
            if (newlineIndex < 0)
                newLog.insert(0, toInsert + "\n");
            else
                newLog.insert(newlineIndex, toInsert);
        }
        matcher.appendTail(newLog);

        newLog.insert(0, "--------------------------------\n");

        return newLog.toString();
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }



    // Uniform Handling methods

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programId, name);
    }

    public <T> Uniform<T> createUniform(String name, UniformType<T> type) {
        return type.createUniform(name, this);
    }


    public void bind() {
        if(boundShader!=this) {
            glUseProgram(programId);
            boundShader = this;
        }
    }

    public void unbind() {
        if(boundShader == this) {
            glUseProgram(0);
            boundShader = null;
        }
    }

    public static ShaderProgram getBoundShader() {
        return boundShader;
    }

    @Override
    public void free() {
        unbind();
        glDeleteProgram(programId);
    }

}
