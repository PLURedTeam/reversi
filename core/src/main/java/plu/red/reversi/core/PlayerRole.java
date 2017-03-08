package plu.red.reversi.core;

/**
 * Created by daniel on 3/6/17.
 */
public enum PlayerRole {
    NONE, WHITE, BLACK;

    public PlayerRole invert() {
        switch(this) {
            case WHITE: return BLACK;
            case BLACK: return WHITE;
            default: return NONE;
        }
    }
}
