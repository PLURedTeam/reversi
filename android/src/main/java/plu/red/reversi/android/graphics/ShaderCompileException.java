package plu.red.reversi.android.graphics;

/**
 * Created by daniel on 3/19/17.
 * Copyright 13013 Inc. All Rights Reserved.
 */

public class ShaderCompileException extends RuntimeException {

    private int line;
    private int column;

    public ShaderCompileException(String message) {
        super(message);

        // TODO: Parse out line and column from the error message
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
