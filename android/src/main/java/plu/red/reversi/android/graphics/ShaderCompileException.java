package plu.red.reversi.android.graphics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class ShaderCompileException extends RuntimeException {

    public static final Pattern linePattern = Pattern.compile("^\\d+:(\\d+):");

    private int line;
    private int column;

    private String sourceCode;

    public ShaderCompileException(String message, String source) {
        super(message);

        sourceCode = source;

        Matcher lineMatcher = linePattern.matcher(message);

        if(lineMatcher.find()) {
            line = Integer.parseInt(lineMatcher.group(1));
            column = 0;
        }
        else {
            System.err.println("Line matcher could not match erro msg:");
            System.err.println(message);
        }
    }

    @Override
    public String getMessage() {
        if(line != 0) {
            String[] lines = sourceCode.split("\n");

            String s = super.getMessage() + "\n";

            for(int i = 0;i < lines.length;i++) {
                if(i + 1 == line)
                    s += ">>> ";
                else
                    s += "    ";

                s += lines[i] + "\n";
            }

            return s;
        }
        else {
            return super.getMessage() +
                    "\n" +
                    sourceCode;

        }
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
